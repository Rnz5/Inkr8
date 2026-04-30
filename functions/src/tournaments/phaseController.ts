import {onSchedule} from "firebase-functions/v2/scheduler";
import {db} from "../firebase/admin";
import {FieldValue} from "firebase-admin/firestore";

async function getRandomWords(count: number): Promise<string[]> {
  const randomOffset = Math.random();
  const snapshot = await db.collection("words")
    .where("isActive", "==", true)
    .where("randomIndex", ">=", randomOffset)
    .limit(count)
    .get();

  const results: string[] = [];
  snapshot.docs.forEach((doc) => {
    const word = doc.data().word as string;
    if (word) results.push(word);
  });

  if (results.length < count) {
    const remaining = count - results.length;
    const secondSnapshot = await db.collection("words")
      .where("isActive", "==", true)
      .where("randomIndex", "<", randomOffset)
      .limit(remaining)
      .get();

    secondSnapshot.docs.forEach((doc) => {
      const word = doc.data().word as string;
      if (word) results.push(word);
    });
  }
  return results.sort(() => Math.random() - 0.5);
}

async function getRandomThemeAndTopic(): Promise<{
  themeId: string;
  themeName: string;
  topicId: string;
  topicName: string;
}> {
  const themesSnapshot = await db.collection("themes").get();
  const themes = themesSnapshot.docs;
  if (themes.length === 0) throw new Error("No themes found");

  const randomTheme = themes[Math.floor(Math.random() * themes.length)];
  const themeData = randomTheme.data();

  const topicsSnapshot = await db.collection("topics")
    .where("themeId", "==", randomTheme.id)
    .get();

  const topics = topicsSnapshot.docs;
  if (topics.length === 0) throw new Error("No topics found for selected theme");

  const randomTopic = topics[Math.floor(Math.random() * topics.length)];
  const topicData = randomTopic.data();

  return {
    themeId: randomTheme.id,
    themeName: themeData.name ?? "Unknown Theme",
    topicId: randomTopic.id,
    topicName: topicData.name ?? "Unknown Topic",
  };
}

interface TournamentUpdates {
  updatedAt: FieldValue;
  status?: string;
  nextPhaseCheckAt?: number | FieldValue;
  themeId?: string;
  themeName?: string;
  topicId?: string;
  topicName?: string;
  words?: string[];
  [key: string]: string | number | FieldValue | string[] | undefined;
}

export const tournamentPhaseController = onSchedule("every 15 minutes", async () => {
  const now = Date.now();

  const snapshot = await db.collection("tournaments")
    .where("status", "in", ["ENROLLING", "ACTIVE"])
    .where("nextPhaseCheckAt", "<=", now)
    .orderBy("nextPhaseCheckAt", "asc")
    .limit(20)
    .get();

  if (snapshot.empty) return;

  for (const doc of snapshot.docs) {
    const data = doc.data();
    const updates: TournamentUpdates = {
      updatedAt: FieldValue.serverTimestamp(),
    };

    if (data.status === "ENROLLING" && now >= data.enrollmentDeadline) {
      const gameContent = await getRandomThemeAndTopic();
      const gameWords = await getRandomWords(10);

      updates.status = "ACTIVE";
      updates.nextPhaseCheckAt = data.submissionDeadline;
      updates.themeId = gameContent.themeId;
      updates.themeName = gameContent.themeName;
      updates.topicId = gameContent.topicId;
      updates.topicName = gameContent.topicName;
      updates.words = gameWords;
    } else if (data.status === "ACTIVE" && now >= data.submissionDeadline) {
      updates.status = "COMPLETED";
      updates.nextPhaseCheckAt = FieldValue.delete();
    }

    if (updates.status) {
      await doc.ref.update(updates);
    }
  }
});
