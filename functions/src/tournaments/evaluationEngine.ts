import {onDocumentUpdated} from "firebase-functions/v2/firestore";
import {db, FieldValue} from "../firebase/admin";
import {calculateRewardPercentages} from "../utils/tournamentRewards";
import {OPENAI_API_KEY, evaluateWithR8} from "../r8/evaluateWithR8";

type EvaluatedSubmission = {
  authorId: string;
  score: number;
  feedback: string;
  submissionRef: FirebaseFirestore.DocumentReference;
};

export const tournamentEvaluationEngine = onDocumentUpdated(
  {
    document: "tournaments/{tournamentId}",
    region: "us-central1",
    secrets: [OPENAI_API_KEY],
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

      const apiKey = OPENAI_API_KEY.value();

      const evaluated: EvaluatedSubmission[] = [];

      for (const doc of submissionsSnapshot.docs) {
        const data = doc.data();

        const result = await evaluateWithR8({
          apiKey,
          content: data.content ?? "",
          gamemode: after.gamemode ?? "STANDARD",
          requiredWords: Array.isArray(after.requiredWords) ?
            after.requiredWords : [],
          themeName: after.themeName ?? null,
          topicName: after.topicName ?? null,
        });

        evaluated.push({
          authorId: data.authorId,
          score: result.finalScore,
          feedback: result.feedback,
          submissionRef: doc.ref,
        });
      }

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
            feedback: entry.feedback,
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
        completedAt: Date.now(),
      });

      await batch.commit();
    }
  }
);
