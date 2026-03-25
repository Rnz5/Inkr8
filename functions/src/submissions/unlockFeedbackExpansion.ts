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

      if (submission.playmode !== "PRACTICE") {
        throw new HttpsError(
          "failed-precondition",
          "Feedback expansion is only available for practice submissions."
        );
      }

      if (submission.status !== "EVALUATED") {
        throw new HttpsError(
          "failed-precondition",
          "Submission is not evaluated yet."
        );
      }

      const alreadyExpanded = submission.evaluation?.isExpanded === true;
      if (alreadyExpanded) {
        throw new HttpsError(
          "failed-precondition",
          "Feedback is already expanded."
        );
      }

      const currentMerit = user?.merit ?? 0;
      if (currentMerit < EXPAND_FEEDBACK_COST) {
        throw new HttpsError("failed-precondition", "Not enough Merit.");
      }

      tx.update(userRef, {
        merit: currentMerit - EXPAND_FEEDBACK_COST,
      });

      tx.update(submissionRef, {
        "evaluation.isExpanded": true,
      });
    });

    return {
      success: true,
      cost: EXPAND_FEEDBACK_COST,
    };
  }
);