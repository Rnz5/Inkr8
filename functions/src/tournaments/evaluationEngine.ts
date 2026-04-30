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

function meritToLiquid(current: number, earned: number, cap: number): number {
  if (current + earned > cap) return Math.max(0, cap - current);
  return earned;
}

function meritToHold(current: number, earned: number, cap: number): number {
  const liquid = meritToLiquid(current, earned, cap);
  return earned - liquid;
}

export const tournamentEvaluationEngine = onDocumentUpdated(
  {
    document: "tournaments/{tournamentId}",
    region: "us-central1",
    secrets: [OPENAI_API_KEY],
    timeoutSeconds: 540,
    memory: "512MiB",
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

      for (let i = 0; i < evaluated.length; i++) {
        const entry = evaluated[i];
        const rank = i + 1;
        const rewardPercent = rewardPercentages[i] ?? 0;
        const reward = Math.floor(after.prizePool * rewardPercent);

        if (reward <= 0) {
          await entry.submissionRef.update({
            evaluation: {
              finalScore: entry.score,
              feedback: entry.feedback,
              meritEarned: 0,
              rankLeaderboard: rank,
            },
            status: "EVALUATED",
          });
          continue;
        }

        const userRef = db.collection("users").doc(entry.authorId);

        await db.runTransaction(async (tx) => {
          const userSnap = await tx.get(userRef);
          if (!userSnap.exists) return;

          const userData = userSnap.data() || {};
          const currentMerit = userData.merit ?? 0;
          const meritCap = userData.meritCap ?? 50000;

          const liquid = meritToLiquid(currentMerit, reward, meritCap);
          const hold = meritToHold(currentMerit, reward, meritCap);

          tx.update(userRef, {
            merit: FieldValue.increment(liquid),
            meritHold: FieldValue.increment(hold),
          });

          tx.update(entry.submissionRef, {
            evaluation: {
              finalScore: entry.score,
              feedback: entry.feedback,
              meritEarned: reward,
              meritToHold: hold,
              rankLeaderboard: rank,
            },
            status: "EVALUATED",
          });
        });
      }

      const totalRevenue = after.entranceFee * after.playersCount;
      const hostProfit = totalRevenue - after.prizePool - after.systemFee;

      if (hostProfit > 0) {
        const hostRef = db.collection("users").doc(after.creatorId);
        await db.runTransaction(async (tx) => {
          const hostSnap = await tx.get(hostRef);
          if (!hostSnap.exists) return;

          const hostData = hostSnap.data() || {};
          const currentMerit = hostData.merit ?? 0;
          const meritCap = hostData.meritCap ?? 50000;

          const liquid = meritToLiquid(currentMerit, hostProfit, meritCap);
          const hold = meritToHold(currentMerit, hostProfit, meritCap);

          tx.update(hostRef, {
            merit: FieldValue.increment(liquid),
            meritHold: FieldValue.increment(hold),
          });
        });
      }

      await tournamentRef.update({
        status: "COMPLETED",
        completedAt: Date.now(),
      });
    }
  }
);
