import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db} from "../firebase/admin";

type MeritAction =
  | "PURCHASE_EXAMPLE_SENTENCE"
  | "ENTER_RANKED"
  | "REWARD_PRACTICE"
  | "REWARD_RANKED"
  | "CHANGE_USERNAME";

function validateUsername(username: string): string | null {
  if (username.length < 2) return "Username must be at least 2 characters.";
  if (username.length > 20) return "Username must be at most 20 characters.";

  const allowedRegex = /^[A-Za-z0-9_.,*{}\[\]()√]+$/;
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

    await db.runTransaction(async (tx) => {
      const userSnap = await tx.get(userRef);

      if (!userSnap.exists) {
        throw new HttpsError("not-found", "User not found.");
      }

      const user = userSnap.data();
      const currentMerit = user?.merit ?? 0;
      const rankedWinStreak = user?.rankedWinStreak ?? 0;
      const rankedLossStreak = user?.rankedLossStreak ?? 0;
      const reputation = user?.reputation ?? 0;

      switch (action) {
      case "PURCHASE_EXAMPLE_SENTENCE": {
        const cost = 25;

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        tx.update(userRef, {
          merit: currentMerit - cost,
        });
        break;
      }

      case "ENTER_RANKED": {
        let modifier = 1.0;

        if (rankedWinStreak > 0) {
          modifier += Math.min(rankedWinStreak * 0.05, 0.75);
        }
        if (rankedLossStreak > 0) {
          modifier -= Math.min(rankedLossStreak * 0.05, 0.4);
        }

        let repModifier = 1.0;

        if (reputation >= 900) repModifier = 0.80;
        else if (reputation >= 700) repModifier = 0.88;
        else if (reputation >= 400) repModifier = 0.94;
        else if (reputation >= 200) repModifier = 0.97;
        else if (reputation <= -900) repModifier = 1.40;
        else if (reputation <= -700) repModifier = 1.30;
        else if (reputation <= -400) repModifier = 1.20;
        else if (reputation <= -200) repModifier = 1.12;

        modifier *= repModifier;

        const baseCost = 100;
        const cost = Math.max(Math.floor(baseCost * modifier), 1);

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        tx.update(userRef, {
          merit: currentMerit - cost,
          currentlyInRanked: true,
          rankedSessionStartedAt: Date.now(),
        });
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
          throw new HttpsError("failed-precondition", "Current username not found.");
        }

        if (normalizedOldUsername === normalizedNewUsername) {
          throw new HttpsError("failed-precondition", "That is already your username.");
        }

        if (currentMerit < cost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        const oldUsernameRef = db.collection("usernames").doc(normalizedOldUsername);
        const newUsernameRef = db.collection("usernames").doc(normalizedNewUsername);
        const newUsernameSnap = await tx.get(newUsernameRef);

        if (newUsernameSnap.exists) {
          throw new HttpsError("already-exists", "That username is already taken.");
        }

        tx.delete(oldUsernameRef);

        tx.set(newUsernameRef, {
          userId: uid,
          username: rawNewUsername,
          normalized: normalizedNewUsername,
          createdAt: Date.now(),
        });

        tx.update(userRef, {
          name: rawNewUsername,
          merit: currentMerit - cost,
        });
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
    });

    return {success: true};
  }
);