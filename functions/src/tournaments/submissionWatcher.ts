
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {db} from "../firebase/admin";

export const submissionCompletionWatcher = onDocumentCreated(
  {
    document: "tournaments/{tournamentId}/submissions/{submissionId}",
    region: "us-central1",
  },
  async (event) => {
    const tournamentId = event.params.tournamentId;

    const tournamentRef = db.collection("tournaments").doc(tournamentId);

    const tournamentSnapshot = await tournamentRef.get();
    const tournament = tournamentSnapshot.data();

    if (!tournament) return;

    // Only care during ACTIVE phase
    if (tournament.status !== "ACTIVE") return;

    const submissionsSnapshot = await tournamentRef
      .collection("submissions")
      .get();

    const submissionsCount = submissionsSnapshot.size;

    // All players submitted → start evaluation early
    if (submissionsCount === tournament.playersCount) {
      await tournamentRef.update({
        status: "EVALUATING",
      });
    }
  }
);
