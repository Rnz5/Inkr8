import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db} from "../firebase/admin";

const EXPAND_FEEDBACK_COST = 50;

export const unlockFeedbackExpansion = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "You must be signed in.");
    }

    const submissionId = request.data?.submissionId as string | undefined;
    if (!submissionId) {
      throw new HttpsError("invalid-argument", "Missing submissionId.");
    }

    const submissionRef = db.collection("submissions").doc(submissionId);
    const userRef = db.collection("users").doc(uid);

    let appliedCost = 0;

    await db.runTransaction(async (tx) => {
      const submissionSnap = await tx.get(submissionRef);
      const userSnap = await tx.get(userRef);

      if (!submissionSnap.exists) {
        throw new HttpsError("not-found", "Submission not found.");
      }

      if (!userSnap.exists) {
        throw new HttpsError("not-found", "User not found.");
      }

      const submission = submissionSnap.data();
      const user = userSnap.data();

      if (!submission) {
        throw new HttpsError("not-found", "Submission data not found.");
      }

      if (submission.authorId !== uid) {
        throw new HttpsError("permission-denied", "This submission is not yours.");
      }

      const isPhilosopher = user?.isPhilosopher === true;

      if (submission.playmode !== "PRACTICE" && !isPhilosopher) {
        throw new HttpsError(
          "failed-precondition",
          "Feedback expansion is only available for practice submissions for standard users."
        );
      }

      if (submission.status !== "EVALUATED") {
        throw new HttpsError(
          "failed-precondition",
          "Submission is not evaluated yet."
        );
      }

      const alreadyExpanded = submission.evaluation?.feedbackUnlocked === true;
      if (alreadyExpanded) {
        throw new HttpsError(
          "failed-precondition",
          "Feedback is already expanded."
        );
      }

      appliedCost = isPhilosopher ? 0 : EXPAND_FEEDBACK_COST;

      const currentMerit = user?.merit ?? 0;
      let newBalance = currentMerit;

      if (appliedCost > 0) {
        if (currentMerit < appliedCost) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        newBalance = currentMerit - appliedCost;
        tx.update(userRef, {
          merit: newBalance,
        });

        const txRef = userRef.collection("meritTransactions").doc();
        tx.set(txRef, {
          amount: -appliedCost,
          reason: "EXPAND_FEEDBACK",
          timestamp: Date.now(),
          balanceAfter: newBalance,
        });
      }

      tx.update(submissionRef, {
        "evaluation.feedbackUnlocked": true,
      });
    });

    return {
      success: true,
      message: "Feedback expansion unlocked.",
      cost: appliedCost,
    };
  }
);
