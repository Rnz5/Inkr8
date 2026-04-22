import {db} from "../firebase/admin";

export async function pruneOldSubmissions(authorId: string) {
  const MAX_SUBMISSIONS_LIMIT = 10;
  const dbRef = db;

  try {
    const querySnapshot = await dbRef.collection("submissions")
      .where("authorId", "==", authorId)
      .where("isSaved", "==", false)
      .orderBy("timestamp", "desc")
      .get();

    if (querySnapshot.size > MAX_SUBMISSIONS_LIMIT) {
      const docsToDelete = querySnapshot.docs.slice(MAX_SUBMISSIONS_LIMIT);
      const batch = dbRef.batch();
      docsToDelete.forEach((doc) => {
        batch.delete(doc.ref);
      });
      await batch.commit();
      console.log(`Pruned ${docsToDelete.length} old submissions for user ${authorId}`);
    }
  } catch (error) {
    console.error("Error pruning submissions:", error);
  }
}
