import {onSchedule} from "firebase-functions/v2/scheduler";
import {db, FieldValue} from "../firebase/admin";

export const meritReleaseController = onSchedule("every 3 hours", async () => {
  const usersWithHold = await db.collection("users")
    .where("meritHold", ">", 0)
    .get();

  if (usersWithHold.empty) {
    console.log("meritReleaseController: No users with merit in hold.");
    return;
  }

  for (const doc of usersWithHold.docs) {
    await db.runTransaction(async (tx) => {
      const userSnap = await tx.get(doc.ref);
      if (!userSnap.exists) return;

      const data = userSnap.data() || {};
      const merit = data.merit ?? 0;
      const meritHold = data.meritHold ?? 0;
      const meritCap = data.meritCap ?? 50000;

      if (merit < meritCap) {
        const remainingCapSpace = meritCap - merit;
        const releaseAmount = Math.min(300, meritHold, remainingCapSpace);

        if (releaseAmount > 0) {
          const newMerit = merit + releaseAmount;
          tx.update(doc.ref, {
            merit: newMerit,
            meritHold: FieldValue.increment(-releaseAmount),
          });

          const txRef = doc.ref.collection("meritTransactions").doc();
          tx.set(txRef, {
            amount: releaseAmount,
            reason: "MERIT_RELEASE",
            timestamp: Date.now(),
            balanceAfter: newMerit,
          });
        }
      }
    });
  }
});
