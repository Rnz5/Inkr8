import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import {db, FieldValue} from "../firebase/admin";
import {calculateRewardPercentages} from "../utils/tournamentRewards";

type EvaluatedSubmission = {
  authorId: string;
  score: number;
  submissionRef: FirebaseFirestore.DocumentReference;
};

export const tournamentEvaluationEngine = onDocumentUpdated(
  {
    document: "tournaments/{tournamentId}",
    region: "us-central1",
  },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();

    if (!before || !after) return;

    if (before.status !== "EVALUATING" && after.status === "EVALUATING") {
      const tournamentId = event.params.tournamentId;
      const tournamentRef = db.collection("tournaments").doc(tournamentId);

      const submissionsSnapshot = await tournamentRef
        .collection("submissions")
        .get();

      const submissionsCount = submissionsSnapshot.size;

      if (submissionsCount < after.minPlayers) {
        await tournamentRef.update({
          status: "CANCELLED",
          cancelledAt: Date.now(),
        });
        return;
      }

      const evaluated: EvaluatedSubmission[] = submissionsSnapshot.docs.map(
        (doc: FirebaseFirestore.QueryDocumentSnapshot) => {
          const data = doc.data();
          const score = Math.random() * 100;

          return {
            authorId: data.authorId,
            score,
            submissionRef: doc.ref,
          };
        }
      );

      evaluated.sort((a, b) => b.score - a.score);

      const rewardPercentages = calculateRewardPercentages(evaluated.length);

      const batch = db.batch();

      evaluated.forEach((entry, index) => {
        const rank = index + 1;
        const rewardPercent = rewardPercentages[index] ?? 0;
        const reward = Math.floor(after.prizePool * rewardPercent);

        batch.update(entry.submissionRef, {
          evaluation: {
            finalScore: entry.score,
            feedback: "R8 has spoken.",
            meritEarned: reward,
            rankLeaderboard: rank,
          },
          status: "EVALUATED",
        });

        const userRef = db.collection("users").doc(entry.authorId);
        batch.update(userRef, {
          merit: FieldValue.increment(reward),
        });
      });

      const totalRevenue = after.entranceFee * after.playersCount;
      const hostProfit = totalRevenue - after.prizePool - after.systemFee;

      if (hostProfit > 0) {
        const hostRef = db.collection("users").doc(after.creatorId);
        batch.update(hostRef, {
          merit: FieldValue.increment(hostProfit),
        });
      }

      batch.update(tournamentRef, {
        status: "COMPLETED",
      });

      await batch.commit();
    }
  }
);
