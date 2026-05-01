import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import {db} from "../firebase/admin";

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
      if (after.refunded === true) return;

      const tournamentId = event.params.tournamentId;
      const tournamentRef = db.collection("tournaments").doc(tournamentId);

      const enrollmentsSnapshot = await tournamentRef.collection("enrollments").get();

      for (const doc of enrollmentsSnapshot.docs) {
        const userId = doc.id;
        const userRef = db.collection("users").doc(userId);

        await db.runTransaction(async (tx) => {
          const userSnap = await tx.get(userRef);
          if (!userSnap.exists) return;

          const currentMerit = userSnap.data()?.merit ?? 0;
          const refundAmount = after.entranceFee;
          const newBalance = currentMerit + refundAmount;

          tx.update(userRef, {
            merit: newBalance,
          });

          const txRef = userRef.collection("meritTransactions").doc();
          tx.set(txRef, {
            amount: refundAmount,
            reason: "REFUND_TOURNAMENT",
            timestamp: Date.now(),
            balanceAfter: newBalance,
          });
        });
      }

      const hostRef = db.collection("users").doc(after.creatorId);
      await db.runTransaction(async (tx) => {
        const hostSnap = await tx.get(hostRef);
        if (!hostSnap.exists) return;

        const currentMerit = hostSnap.data()?.merit ?? 0;
        const refundAmount = after.prizePool;
        const newBalance = currentMerit + refundAmount;

        tx.update(hostRef, {
          merit: newBalance,
        });

        const txRef = hostRef.collection("meritTransactions").doc();
        tx.set(txRef, {
          amount: refundAmount,
          reason: "REFUND_TOURNAMENT",
          timestamp: Date.now(),
          balanceAfter: newBalance,
        });
      });

      await tournamentRef.update({
        refunded: true,
      });
    }
  }
);
