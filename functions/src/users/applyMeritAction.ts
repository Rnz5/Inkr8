import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db} from "../firebase/admin";
import {calculateRankedEntryCost} from "../utils/meritCalculator";
import {onRankedAbandoned} from "../utils/reputationManager";

type MeritAction =
  | "PURCHASE_EXAMPLE_SENTENCE"
  | "PURCHASE_REPUTATION_VIEW"
  | "ENTER_RANKED"
  | "ABANDON_RANKED"
  | "REWARD_PRACTICE"
  | "REWARD_RANKED"
  | "CHANGE_USERNAME"
  | "SAVE_SUBMISSION"
  | "EXPAND_MERIT_CAP";

function validateUsername(username: string): string | null {
  if (username.length < 2) return "Username must be at least 2 characters.";
  if (username.length > 20) return "Username must be at most 20 characters.";

  const allowedRegex = /^[A-Za-z0-9_.,*{}[\]()√]+$/;
  if (!allowedRegex.test(username)) {
    return "Username contains invalid characters.";
  }

  const hasLetterOrDigit = /[A-Za-z0-9]/.test(username);
  if (!hasLetterOrDigit) {
    return "Username must contain at least one letter or number.";
  }

  return null;
}

export const applyMeritAction = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "You must be signed in.");
    }

    const action = request.data?.action as MeritAction | undefined;
    if (!action) {
      throw new HttpsError("invalid-argument", "Missing action.");
    }

    const userRef = db.collection("users").doc(uid);
    let updatedFields: Record<string, unknown> = {};

    await db.runTransaction(async (tx) => {
      const userSnap = await tx.get(userRef);

      if (!userSnap.exists) {
        throw new HttpsError("not-found", "User not found.");
      }

      const user = userSnap.data();
      const currentMerit = user?.merit ?? 0;
      const meritCap = user?.meritCap ?? 50000;
      let rankedWinStreak = user?.rankedWinStreak ?? 0;
      let rankedLossStreak = user?.rankedLossStreak ?? 0;
      let reputation = user?.reputation ?? 0;

      let meritDelta = 0;
      let reason = "";

      switch (action) {
      case "EXPAND_MERIT_CAP": {
        const cost = Math.floor(meritCap * 0.25);
        const expansionAmount = 10000;

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Insufficient Merit for expansion.");
        }

        meritDelta = -cost;
        reason = "EXPAND_MERIT_CAP";
        updatedFields = {
          merit: currentMerit - cost,
          meritCap: meritCap + expansionAmount,
        };
        tx.update(userRef, updatedFields);
        break;
      }

      case "PURCHASE_EXAMPLE_SENTENCE": {
        const cost = 25;

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        meritDelta = -cost;
        reason = "PURCHASE_EXAMPLE_SENTENCE";
        updatedFields = {
          merit: currentMerit - cost,
        };
        tx.update(userRef, updatedFields);
        break;
      }

      case "PURCHASE_REPUTATION_VIEW": {
        const cost = 500;

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        meritDelta = -cost;
        reason = "PURCHASE_REPUTATION_VIEW";
        updatedFields = {
          merit: currentMerit - cost,
        };
        tx.update(userRef, updatedFields);
        break;
      }

      case "SAVE_SUBMISSION": {
        const submissionId = request.data?.submissionId;
        if (!submissionId) {
          throw new HttpsError("invalid-argument", "Missing submissionId.");
        }


        const savedSubsQuery = db.collection("submissions")
          .where("authorId", "==", uid)
          .where("isSaved", "==", true);
        const savedSubsSnap = await tx.get(savedSubsQuery);
        const savedCount = savedSubsSnap.size;

        const cost = 2000 + Math.floor(savedCount / 3) * 200;

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        const subRef = db.collection("submissions").doc(submissionId);
        const subSnap = await tx.get(subRef);

        if (!subSnap.exists) {
          throw new HttpsError("not-found", "Submission not found.");
        }

        const subData = subSnap.data();
        if (subData?.authorId !== uid) {
          throw new HttpsError("permission-denied", "You are not the author.");
        }

        if (subData?.isSaved === true) {
          throw new HttpsError("already-exists", "Submission is already saved.");
        }

        meritDelta = -cost;
        reason = "SAVE_SUBMISSION";
        updatedFields = {
          merit: currentMerit - cost,
        };
        tx.update(userRef, updatedFields);

        tx.update(subRef, {
          isSaved: true,
        });
        break;
      }

      case "ENTER_RANKED": {
        if (user?.currentlyInRanked) {
          reputation = onRankedAbandoned(reputation);
          rankedLossStreak += 1;
          rankedWinStreak = 0;
        }

        const cost = calculateRankedEntryCost(
          rankedWinStreak,
          rankedLossStreak,
          reputation
        );

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        const rankedSessionStartedAt = Date.now();
        meritDelta = -cost;
        reason = "ENTER_RANKED";
        updatedFields = {
          merit: currentMerit - cost,
          currentlyInRanked: true,
          rankedSessionStartedAt: rankedSessionStartedAt,
          reputation: reputation,
          rankedLossStreak: rankedLossStreak,
          rankedWinStreak: rankedWinStreak,
        };
        tx.update(userRef, updatedFields);
        break;
      }

      case "ABANDON_RANKED": {
        if (!user?.currentlyInRanked) {
          throw new HttpsError("failed-precondition", "No active ranked session to abandon.");
        }

        updatedFields = {
          reputation: onRankedAbandoned(reputation),
          rankedLossStreak: rankedLossStreak + 1,
          rankedWinStreak: 0,
          currentlyInRanked: false,
          rankedSessionStartedAt: null,
        };
        tx.update(userRef, updatedFields);
        break;
      }

      case "CHANGE_USERNAME": {
        const cost = 1000;
        const rawNewUsername = String(request.data?.newUsername ?? "").trim();
        const normalizedNewUsername = rawNewUsername.toLowerCase();

        const validationError = validateUsername(rawNewUsername);
        if (validationError) {
          throw new HttpsError("invalid-argument", validationError);
        }

        const oldUsername = String(user?.name ?? "").trim();
        const normalizedOldUsername = oldUsername.toLowerCase();

        if (!oldUsername) {
          throw new HttpsError("failed-precondition",
            "Current username not found.");
        }

        if (normalizedOldUsername === normalizedNewUsername) {
          throw new HttpsError("failed-precondition",
            "That is already your username.");
        }

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition",
            "Not enough Merit.");
        }

        const oldUsernameRef = db.collection("usernames")
          .doc(normalizedOldUsername);
        const newUsernameRef = db.collection("usernames")
          .doc(normalizedNewUsername);
        const newUsernameSnap = await tx.get(newUsernameRef);

        if (newUsernameSnap.exists) {
          throw new HttpsError("already-exists",
            "That username is already taken.");
        }

        tx.delete(oldUsernameRef);

        tx.set(newUsernameRef, {
          userId: uid,
          username: rawNewUsername,
          normalized: normalizedNewUsername,
          createdAt: Date.now(),
        });

        meritDelta = -cost;
        reason = "CHANGE_USERNAME";
        updatedFields = {
          name: rawNewUsername,
          merit: currentMerit - cost,
        };
        tx.update(userRef, updatedFields);
        break;
      }

      case "REWARD_PRACTICE":
      case "REWARD_RANKED": {
        throw new HttpsError(
          "failed-precondition",
          "This action must be applied by a trusted backend flow."
        );
      }

      default:
        throw new HttpsError("invalid-argument", "Unsupported action.");
      }

      if (meritDelta !== 0) {
        const txRef = userRef.collection("meritTransactions").doc();
        tx.set(txRef, {
          amount: meritDelta,
          reason: reason,
          timestamp: Date.now(),
          balanceAfter: currentMerit + meritDelta,
        });
      }
    });

    return {
      success: true,
      updatedFields,
    };
  }
);
