import {onSchedule} from "firebase-functions/v2/scheduler";
import {db} from "../firebase/admin";

export const dailyStatsSnapshot = onSchedule(
  {
    schedule: "0 1 * * *",
    region: "us-central1",
    timeoutSeconds: 540,
    memory: "512MiB",
  },
  async () => {
    const now = new Date();
    const startOfToday = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate()));
    const startOfYesterday = new Date(startOfToday.getTime() - 24 * 60 * 60 * 1000);

    const startTime = startOfYesterday.getTime();
    const endTime = startOfToday.getTime();

    const dateId = startOfYesterday.toISOString().split("T")[0];

    try {
      const submissionsSnap = await db.collection("submissions")
        .where("timestamp", ">=", startTime)
        .where("timestamp", "<", endTime)
        .get();

      let totalSubmissions = 0;
      let rankedSubmissions = 0;
      let practiceSubmissions = 0;
      let totalScore = 0;
      let evaluatedCount = 0;
      const activeUsersSet = new Set<string>();
      const themeScores: Record<string, { total: number; count: number }> = {};

      submissionsSnap.docs.forEach((doc) => {
        const data = doc.data();
        totalSubmissions++;

        const playmode = data.playmode || "PRACTICE";
        if (playmode === "RANKED") {
          rankedSubmissions++;
        } else if (playmode === "PRACTICE") {
          practiceSubmissions++;
        }

        if (data.authorId) {
          activeUsersSet.add(data.authorId);
        }

        if (data.status === "EVALUATED" && data.evaluation?.finalScore !== undefined) {
          const score = Number(data.evaluation.finalScore);
          totalScore += score;
          evaluatedCount++;

          if (data.gamemodeName === "ON_TOPIC" && data.themeId) {
            if (!themeScores[data.themeId]) {
              themeScores[data.themeId] = {total: 0, count: 0};
            }
            themeScores[data.themeId].total += score;
            themeScores[data.themeId].count++;
          }
        }
      });

      const averageScore = evaluatedCount > 0 ? Number((totalScore / evaluatedCount).toFixed(2)) : 0;

      let hardestTheme = "N/A";
      let minAvg = Infinity;
      for (const themeId in themeScores) {
        if (Object.prototype.hasOwnProperty.call(themeScores, themeId)) {
          const avg = themeScores[themeId].total / themeScores[themeId].count;
          if (avg < minAvg) {
            minAvg = avg;
            hardestTheme = themeId;
          }
        }
      }

      const transactionsSnap = await db.collectionGroup("meritTransactions")
        .where("timestamp", ">=", startTime)
        .where("timestamp", "<", endTime)
        .get();

      let totalMeritEarned = 0;
      let totalMeritSpent = 0;

      transactionsSnap.docs.forEach((doc) => {
        const amt = Number(doc.data().amount || 0);
        if (amt > 0) {
          totalMeritEarned += amt;
        } else if (amt < 0) {
          totalMeritSpent += Math.abs(amt);
        }
      });

      const tournamentsSnap = await db.collection("tournaments")
        .where("status", "==", "COMPLETED")
        .where("completedAt", ">=", startTime)
        .where("completedAt", "<", endTime)
        .get();

      const tournamentsCompleted = tournamentsSnap.size;

      const snapshotData = {
        date: dateId,
        totalSubmissions,
        rankedSubmissions,
        practiceSubmissions,
        averageScore,
        totalMeritEarned,
        totalMeritSpent,
        tournamentsCompleted,
        activeUsers: activeUsersSet.size,
        hardestTheme,
        createdAt: Date.now(),
      };

      await db.collection("stats").doc("daily").collection("records").doc(dateId).set(snapshotData);

      console.log(`Daily snapshot completed for ${dateId}:`, snapshotData);
    } catch (error) {
      console.error(`Failed to generate daily snapshot for ${dateId}:`, error);
    }
  }
);
