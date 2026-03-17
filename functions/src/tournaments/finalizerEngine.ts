import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import {db, FieldValue} from "../firebase/admin";

export const tournamentFinalizerEngine = onDocumentUpdated(
  {
    document: "tournaments/{tournamentId}",
    region: "us-central1",
  },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();

    if (!before || !after) return;

    if (before.status !== "COMPLETED" && after.status === "COMPLETED") {
      const tournamentId = event.params.tournamentId;

      const submissions = await db
        .collection("tournaments")
        .doc(tournamentId)
        .collection("submissions")
        .get();

      const batch = db.batch();

      submissions.docs.forEach((doc) => {
        const data = doc.data();
        const userRef = db.collection("users").doc(data.authorId);

        const meritEarned = data.evaluation?.meritEarned || 0;
        const rank = data.evaluation?.rankLeaderboard;

        batch.update(userRef, {
          tournamentsPlayed: FieldValue.increment(1),
          totalMeritEarned: FieldValue.increment(meritEarned),
        });

        if (rank === 1) {
          batch.update(userRef, {
            tournamentsWon: FieldValue.increment(1),
          });
        }
      });

      await batch.commit();
    }
  }
);
