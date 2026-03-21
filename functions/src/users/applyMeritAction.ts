import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db} from "../firebase/admin";

type MeritAction =
  | "PURCHASE_EXAMPLE_SENTENCE"
  | "ENTER_RANKED"
  | "REWARD_PRACTICE"
  | "REWARD_RANKED";

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
