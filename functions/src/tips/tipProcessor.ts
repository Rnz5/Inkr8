
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {db} from "../firebase/admin";

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

    // Prevent tipping yourself
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

    // check if recipient submitted in this tournament
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

    if (!duplicateTipQuery.empty) {
      console.log("Duplicate tip attempt");
      return;
    }

    try {
      await db.runTransaction(async (tx: FirebaseFirestore.Transaction) => {
        const tipperDoc = await tx.get(tipperRef);
        const recipientDoc = await tx.get(recipientRef);

        if (!tipperDoc.exists) throw new Error("Tipper not found");
        if (!recipientDoc.exists) throw new Error("Recipient not found");

        const tipperMerit = tipperDoc.data()?.merit ?? 0;
        const recipientMerit = recipientDoc.data()?.merit ?? 0;

        if (tipperMerit < amount) {
          throw new Error("Insufficient merit");
        }

        tx.update(tipperRef, {
          merit: tipperMerit - amount,
        });

        tx.update(recipientRef, {
          merit: recipientMerit + amount,
        });

        tx.update(tipRef, {
          processed: true,
        });
      });

      console.log("Tip processed successfully");
    } catch (err) {
      console.error("Tip transaction failed:", err);
    }
  }
);
