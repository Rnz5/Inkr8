
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {db, FieldValue} from "../firebase/admin";

function meritToLiquid(current: number, earned: number, cap: number): number {
  if (current + earned > cap) return Math.max(0, cap - current);
  return earned;
}

function meritToHold(current: number, earned: number, cap: number): number {
  const liquid = meritToLiquid(current, earned, cap);
  return earned - liquid;
}

export const onTipCreated = onDocumentCreated(
  {
    document: "tournaments/{tournamentId}/tips/{tipId}",
    region: "us-central1",
  },
  async (event) => {
    const tip = event.data?.data();
    if (!tip) return;

    const tournamentId = event.params.tournamentId;

    const {tipperId, recipientId, amount, processed} = tip;

    if (!tipperId || !recipientId || !amount) {
      console.log("Invalid tip data");
      return;
    }

    if (tipperId === recipientId) {
      console.log("User attempted to tip themselves");
      return;
    }

    const ALLOWED_AMOUNTS = [100, 150, 200];

    if (!ALLOWED_AMOUNTS.includes(amount)) {
      console.log("Invalid tip amount:", amount);
      return;
    }

    if (processed === true) return;

    if (!event.data) return;
    const tipRef = event.data.ref;

    const tipperRef = db.collection("users").doc(tipperId);
    const recipientRef = db.collection("users").doc(recipientId);

    const submissionQuery = await db
      .collection("tournaments")
      .doc(tournamentId)
      .collection("submissions")
      .where("authorId", "==", recipientId)
      .limit(1)
      .get();

    if (submissionQuery.empty) {
      console.log("Recipient did not submit in this tournament");
      return;
    }

    const duplicateTipQuery = await db
      .collection("tournaments")
      .doc(tournamentId)
      .collection("tips")
      .where("tipperId", "==", tipperId)
      .where("recipientId", "==", recipientId)
      .limit(1)
      .get();

    if (!duplicateTipQuery.empty && duplicateTipQuery.docs[0].id !== event.data.id) {
      console.log("Duplicate tip attempt");
      return;
    }

    try {
      await db.runTransaction(async (tx: FirebaseFirestore.Transaction) => {
        const tipperDoc = await tx.get(tipperRef);
        const recipientDoc = await tx.get(recipientRef);

        if (!tipperDoc.exists) throw new Error("Tipper not found");
        if (!recipientDoc.exists) throw new Error("Recipient not found");

        const tipperData = tipperDoc.data() || {};
        const recipientData = recipientDoc.data() || {};

        const tipperMerit = tipperData.merit ?? 0;
        const recipientMerit = recipientData.merit ?? 0;
        const meritCap = recipientData.meritCap ?? 50000;

        if (tipperMerit < amount) {
          throw new Error("Insufficient merit");
        }

        const liquid = meritToLiquid(recipientMerit, amount, meritCap);
        const hold = meritToHold(recipientMerit, amount, meritCap);

        const newTipperBalance = tipperMerit - amount;
        const newRecipientBalance = recipientMerit + liquid;

        tx.update(tipperRef, {
          merit: newTipperBalance,
        });

        tx.update(recipientRef, {
          merit: newRecipientBalance,
          meritHold: FieldValue.increment(hold),
        });

        tx.update(tipRef, {
          processed: true,
        });

        const tipperTxRef = tipperRef.collection("meritTransactions").doc();
        tx.set(tipperTxRef, {
          amount: -amount,
          reason: "TIP_SENT",
          timestamp: Date.now(),
          balanceAfter: newTipperBalance,
        });

        if (liquid !== 0) {
          const recipientTxRef = recipientRef.collection("meritTransactions").doc();
          tx.set(recipientTxRef, {
            amount: liquid,
            reason: "TIP_RECEIVED",
            timestamp: Date.now(),
            balanceAfter: newRecipientBalance,
          });
        }
      });

      console.log("Tip processed successfully");
    } catch (err) {
      console.error("Tip transaction failed:", err);
    }
  }
);
