import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as admin from "firebase-admin";

export const onSubmissionCreated = onDocumentCreated(
  "submissions/{submissionId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      console.error("No snapshot found");
      return;
    }

    const newData = snapshot.data();
    const authorId = newData.authorId;

    if (!authorId) {
      console.error("No authorId found in submission");
      return;
    }

    const db = admin.firestore();
    const userRef = db.collection("users").doc(authorId);

    try {
      await db.runTransaction(async (transaction) => {
        const userDoc = await transaction.get(userRef);
        if (!userDoc.exists) {
          console.error(`User ${authorId} does not exist`);
          return;
        }

        const userData = userDoc.data() || {};
        const currentBestScore = userData.bestScore || 0;
        const currentSubmissionsCount = userData.submissionsCount || 0;

        const evaluation = newData.evaluation || {};
        const newScore = evaluation.finalScore || 0;

        transaction.update(userRef, {
          submissionsCount: currentSubmissionsCount + 1,
          bestScore: Math.max(currentBestScore, newScore),
        });
      });

      const MAX_SUBMISSIONS_LIMIT = 10;
      const querySnapshot = await db.collection("submissions")
        .where("authorId", "==", authorId)
        .where("isSaved", "==", false)
        .orderBy("timestamp", "desc")
        .get();

      if (querySnapshot.size > MAX_SUBMISSIONS_LIMIT) {
        const docsToDelete = querySnapshot.docs.slice(MAX_SUBMISSIONS_LIMIT);
        const batch = db.batch();
        docsToDelete.forEach((doc) => {
          batch.delete(doc.ref);
        });
        await batch.commit();
        console.log(`Pruned ${docsToDelete.length} old submissions for user ${authorId}`);
      }
    } catch (error) {
      console.error("Error processing submission creation:", error);
    }
  }
);
