import {db} from "../firebase/admin";

export async function pruneOldSubmissions(authorId: string) {
  const MAX_SUBMISSIONS_LIMIT = 10;
  const dbRef = db;

  try {
    const latestSubmissions = await dbRef.collection("submissions")
      .where("authorId", "==", authorId)
      .where("isSaved", "==", false)
      .orderBy("timestamp", "desc")
      .limit(MAX_SUBMISSIONS_LIMIT)
      .get();

    if (latestSubmissions.size < MAX_SUBMISSIONS_LIMIT) {
      return;
    }

    const oldestKeptDoc = latestSubmissions.docs[latestSubmissions.size - 1];
    const cutoffTimestamp = oldestKeptDoc.data().timestamp;

    const querySnapshot = await dbRef.collection("submissions")
      .where("authorId", "==", authorId)
      .where("isSaved", "==", false)
      .where("timestamp", "<", cutoffTimestamp)
      .limit(100)
      .get();

    if (!querySnapshot.empty) {
      const batch = dbRef.batch();
      querySnapshot.docs.forEach((doc) => {
        batch.delete(doc.ref);
      });
      await batch.commit();
      console.log(`[Maintenance] Pruned ${querySnapshot.size} old submissions for user ${authorId}`);
    }
  } catch (error: unknown) {
    const err = error as { code?: number; message?: string };
    if (err.code === 9 || (err.message && err.message.includes("index"))) {
      console.error(`[INDEX_REQUIRED] pruneOldSubmissions failed for ${authorId}. Please ensure composite index (authorId ASC, isSaved ASC, timestamp DESC) exists.`);
      console.error(`Full error: ${err.message ?? "Unknown error"}`);
    } else {
      console.error(`[Maintenance Error] Failed to prune submissions for ${authorId}:`, error);
    }
  }
}
