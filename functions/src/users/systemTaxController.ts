import {onSchedule} from "firebase-functions/v2/scheduler";
import {db} from "../firebase/admin";
import {adjustReputation} from "../utils/reputationManager";

/**
 * the weekly tax system runs every Sunday at 00:00
 * it retires 1% of the user's max merit cap :-)
 * ensures merit does not drop below zero and excludes R8
 */
export const weeklyTaxProcessor = onSchedule(
  {
    schedule: "0 0 * * 0",
    region: "us-central1",
    timeoutSeconds: 540,
    memory: "512MiB",
  },
  async () => {
    const usersSnap = await db.collection("users").select("merit", "meritCap").get();

    if (usersSnap.empty) {
      console.log("weeklyTaxProcessor: No users found.");
      return;
    }

    const writer = db.bulkWriter();

    writer.onWriteError((error) => {
      console.error(`Error in weekly tax write for ${error.documentRef.path}:`, error.message);
      return false;
    });

    usersSnap.docs.forEach((doc) => {
      if (doc.id === "R8") return;

      const data = doc.data();
      const currentMerit = data.merit ?? 0;
      const meritCap = data.meritCap ?? 50000;
      const taxAmount = Math.floor(meritCap * 0.01);

      const newMerit = Math.max(0, currentMerit - taxAmount);
      const actualTax = currentMerit - newMerit;

      if (newMerit !== currentMerit) {
        writer.update(doc.ref, {
          merit: newMerit,
        });

        const txRef = doc.ref.collection("meritTransactions").doc();
        writer.set(txRef, {
          amount: -actualTax,
          reason: "WEEKLY_TAX",
          timestamp: Date.now(),
          balanceAfter: newMerit,
        });
      }
    });

    await writer.close();
    console.log(`Weekly tax processed for ${usersSnap.size} users.`);
  }
);

/**
 * daily debt penalty: runs every day at 00:05
 * decreases reputation for users with negative merit
 */
export const dailyDebtPenaltyProcessor = onSchedule(
  {
    schedule: "5 0 * * *",
    region: "us-central1",
    timeoutSeconds: 300,
  },
  async () => {
    const usersInDebt = await db.collection("users")
      .where("merit", "<", 0)
      .select("reputation")
      .get();

    if (usersInDebt.empty) {
      console.log("dailyDebtPenaltyProcessor: No users in debt.");
      return;
    }

    const writer = db.bulkWriter();

    usersInDebt.docs.forEach((doc) => {
      const data = doc.data();
      const currentRep = data.reputation ?? 0;
      const newRep = adjustReputation(currentRep, -5);

      writer.update(doc.ref, {
        reputation: newRep,
      });
    });

    await writer.close();
    console.log(`Daily debt penalty processed for ${usersInDebt.size} users.`);
  }
);
