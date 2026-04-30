import {onSchedule} from "firebase-functions/v2/scheduler";
import {db, FieldValue} from "../firebase/admin";
import {onRankedAbandoned} from "../utils/reputationManager";

export const rankedSessionCleaner = onSchedule("every 15 minutes", async () => {
  const sixtyMinutesAgo = Date.now() - (60 * 60 * 1000);

  try {
    const abandonedSessions = await db.collection("users")
      .where("currentlyInRanked", "==", true)
      .where("rankedSessionStartedAt", "<=", sixtyMinutesAgo)
      .get();

    if (abandonedSessions.empty) {
      return;
    }

    const batch = db.batch();

    abandonedSessions.docs.forEach((doc) => {
      const userData = doc.data();
      const currentReputation = userData.reputation ?? 0;
      const newReputation = onRankedAbandoned(currentReputation);

      batch.update(doc.ref, {
        currentlyInRanked: false,
        rankedSessionStartedAt: FieldValue.delete(),
        reputation: newReputation,
      });

      console.log(`Cleaned up abandoned ranked session for user: ${doc.id}. Reputation reduced from ${currentReputation} to ${newReputation}`);
    });

    await batch.commit();
  } catch (error) {
    console.error("Error in rankedSessionCleaner:", error);
  }
});
