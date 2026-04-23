import {onSchedule} from "firebase-functions/v2/scheduler";
import {db} from "../firebase/admin";

/**
 * returns a random words using randomIndex pattern
 *
 * @param {number} count
 * @return {Promise<string[]>}
 */
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

/**
 * returns random theme and random topic from the theme
 *
 * @return {Promise<{
 *   themeId: string;
 *   themeName: string;
 *   topicId: string;
 *   topicName: string;
 * }>}
 */
async function getRandomThemeAndTopic(): Promise<{
  themeId: string;
  themeName: string;
  topicId: string;
  topicName: string;
}> {
  const themesSnapshot = await db.collection("themes").get();
  const themes = themesSnapshot.docs;

  if (themes.length === 0) {
    throw new Error("No themes found");
  }

  const randomTheme = themes[Math.floor(Math.random() * themes.length)];
  const themeData = randomTheme.data();

  const topicsSnapshot = await db
    .collection("topics")
    .where("themeId", "==", randomTheme.id)
    .get();

  const topics = topicsSnapshot.docs;

  if (topics.length === 0) {
    throw new Error("No topics found for selected theme");
  }

  const randomTopic = topics[Math.floor(Math.random() * topics.length)];
  const topicData = randomTopic.data();

  return {
    themeId: randomTheme.id,
    themeName: themeData.name ?? "Unknown Theme",
    topicId: randomTopic.id,
    topicName: topicData.name ?? "Unknown Topic",
  };
}

export const tournamentPhaseController = onSchedule(
  {
    schedule: "every 2 minutes",
    region: "us-central1",
  },
  async () => {
    const countSnapshot = await db
      .collection("tournaments")
      .where("status", "in", ["ENROLLING", "ACTIVE"])
      .count()
      .get();

    if (countSnapshot.data().count === 0) {
      return;
    }

    const now = Date.now();

    const snapshot = await db
      .collection("tournaments")
      .where("status", "in", ["ENROLLING", "ACTIVE"])
      .get();

    const batch = db.batch();

    for (const doc of snapshot.docs) {
      const tournament = doc.data();
      const ref = doc.ref;

      if (
        tournament.status === "ENROLLING" &&
        now > tournament.enrollmentDeadline
      ) {
        if (tournament.playersCount >= tournament.minPlayers) {
          const gamemode = tournament.gamemode ?? "STANDARD";

          if (gamemode === "ON_TOPIC") {
            const prompt = await getRandomThemeAndTopic();
            const words = await getRandomWords(2);

            batch.update(ref, {
              status: "ACTIVE",
              requiredWords: words,
              themeId: prompt.themeId,
              themeName: prompt.themeName,
              topicId: prompt.topicId,
              topicName: prompt.topicName,
            });
          } else {
            const words = await getRandomWords(4);

            batch.update(ref, {
              status: "ACTIVE",
              requiredWords: words,
              themeId: null,
              themeName: null,
              topicId: null,
              topicName: null,
            });
          }
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
    }

    await batch.commit();
  }
);
