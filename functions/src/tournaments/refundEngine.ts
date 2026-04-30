import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import {db, FieldValue} from "../firebase/admin";

export const tournamentRefundEngine = onDocumentUpdated(
  {
    document: "tournaments/{tournamentId}",
    region: "us-central1",
  },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();

    if (!before || !after) return;

    if (before.status !== "CANCELLED" && after.status === "CANCELLED") {
      // prevent double refunds
      if (after.refunded === true) return;

      const tournamentId = event.params.tournamentId;
      const tournamentRef = db.collection("tournaments").doc(tournamentId);

      const batch = db.batch();

      // refund players
      const enrollmentsSnapshot = await tournamentRef
        .collection("enrollments")
        .get();

      enrollmentsSnapshot.docs.forEach((doc: FirebaseFirestore.QueryDocumentSnapshot) => {
        const userId = doc.id;
        const userRef = db.collection("users").doc(userId);

        batch.update(userRef, {
          merit: FieldValue.increment(after.entranceFee),
        });
      });

      // refund host
      const hostRef = db.collection("users").doc(after.creatorId);

      batch.update(hostRef, {
        merit: FieldValue.increment(after.prizePool),
      });

      // refunded
      batch.update(tournamentRef, {
        refunded: true,
      });

      await batch.commit();
    }
  }
);
