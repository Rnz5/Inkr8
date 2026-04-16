import {onSchedule} from "firebase-functions/v2/scheduler";
import * as admin from "firebase-admin";

export const pruneOldTournaments = onSchedule("every 12 hours", async () => {
  const db = admin.firestore();
  const now = Date.now();
  const TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000;

  try {
    const cancelledSnapshot = await db.collection("tournaments")
      .where("status", "==", "CANCELLED")
      .get();

    const cancelledBatch = db.batch();
    cancelledSnapshot.docs.forEach((doc) => {
      cancelledBatch.delete(doc.ref);
    });

    if (cancelledSnapshot.size > 0) {
      await cancelledBatch.commit();
      console.log(`Pruned ${cancelledSnapshot.size} cancelled tournaments.`);
    }

    const completedSnapshot = await db.collection("tournaments")
      .where("status", "==", "COMPLETED")
      .get();

    const completedBatch = db.batch();
    let completedPrunedCount = 0;

    completedSnapshot.docs.forEach((doc) => {
      const data = doc.data();
      const timestamp = data.completedAt || data.createdAt || 0;

      if (now - timestamp > TWENTY_FOUR_HOURS_MS) {
        completedBatch.delete(doc.ref);
        completedPrunedCount++;
      }
    });

    if (completedPrunedCount > 0) {
      await completedBatch.commit();
      console.log(`Pruned ${completedPrunedCount} completed tournaments.`);
    }
  } catch (error) {
    console.error("Error pruning old tournaments:", error);
  }
});
