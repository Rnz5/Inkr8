import {onSchedule} from "firebase-functions/v2/scheduler";
import {db} from "../firebase/admin";

export const tournamentPhaseController = onSchedule(
  {
    schedule: "every 2 minutes",
    region: "us-central1",
  },
  async () => {
    const now = Date.now();

    const snapshot = await db
      .collection("tournaments")
      .where("status", "in", ["ENROLLING", "ACTIVE"])
      .get();

    const batch = db.batch();

    snapshot.docs.forEach((doc: FirebaseFirestore.QueryDocumentSnapshot) => {
      const tournament = doc.data();
      const ref = doc.ref;

      if (
        tournament.status === "ENROLLING" &&
        now > tournament.enrollmentDeadline
      ) {
        if (tournament.playersCount >= tournament.minPlayers) {
          batch.update(ref, {status: "ACTIVE"});
        } else {
          batch.update(ref, {
            status: "CANCELLED",
            cancelledAt: now,
          });
        }
      }

      if (
        tournament.status === "ACTIVE" &&
        now > tournament.submissionDeadline
      ) {
        batch.update(ref, {status: "EVALUATING"});
      }
    });

    await batch.commit();
  }
);
