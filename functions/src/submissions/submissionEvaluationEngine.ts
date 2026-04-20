import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {db, FieldValue} from "../firebase/admin";
import {OPENAI_API_KEY, evaluateWithR8} from "../r8/evaluateWithR8";
import {calculateMerit} from "../utils/meritCalculator";
import {calculateNewRating} from "../utils/ratingCalculator";
import {onRankedCompleted} from "../utils/reputationManager";

export const submissionEvaluationEngine = onDocumentCreated(
  {
    document: "submissions/{submissionId}",
    region: "us-central1",
    secrets: [OPENAI_API_KEY],
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      console.log("submissionEvaluationEngine: no snapshot");
      return;
    }

    const data = snapshot.data();
    if (!data) {
      console.log("submissionEvaluationEngine: no data",
        {submissionId: snapshot.id});
      return;
    }

    const submissionRef = snapshot.ref;

    try {
      console.log("submissionEvaluationEngine triggered", {
        submissionId: snapshot.id,
        authorId: data.authorId ?? null,
        playmode: data.playmode ?? null,
        gamemode: data.gamemode ?? null,
        status: data.status ?? null,
      });

      const apiKey = OPENAI_API_KEY.value();

      const playmode = data.playmode ?? "PRACTICE";
      const authorId = data.authorId;

      if (!authorId) {
        console.log("submissionEvaluationEngine: missing authorId", {
          submissionId: snapshot.id,
        });

        await submissionRef.update({
          status: "FAILED",
          evaluationError: "Missing authorId",
        });

        return;
      }

      if (data.status && data.status !== "PENDING") {
        console.log("submissionEvaluationEngine: skipped, not PENDING", {
          submissionId: snapshot.id,
          status: data.status,
        });
        return;
      }

      const requiredWords = Array.isArray(data.wordsUsed) ?
        data.wordsUsed
          .map((w: { word?: string } | string) =>
            typeof w === "string" ? w : (w.word ?? "")
          )
          .filter((word: string) => word.length > 0) :
        [];

      console.log("submissionEvaluationEngine: calling evaluateWithR8", {
        submissionId: snapshot.id,
        requiredWordsCount: requiredWords.length,
        wordCount: data.wordCount ?? 0,
      });

      const result = await evaluateWithR8({
        apiKey,
        content: data.content ?? "",
        gamemode: data.gamemode ?? "STANDARD",
        requiredWords,
        themeName: data.themeName ?? null,
        topicName: data.topicName ?? null,
      });

      console.log("submissionEvaluationEngine: evaluation completed", {
        submissionId: snapshot.id,
        finalScore: result.finalScore,
      });

      const userRef = db.collection("users").doc(authorId);
      const userSnap = await userRef.get();

      if (!userSnap.exists) {
        throw new Error("User not found for submission evaluation");
      }

      const userData = userSnap.data() ?? {};
      const currentRating = Number(userData.rating ?? 0);
      const currentMerit = Number(userData.merit ?? 0);
      const meritCap = Number(userData.meritCap ?? 50000);

      const meritEarned = calculateMerit(
        result.finalScore,
        data.wordCount ?? 0,
        data.gamemode,
        playmode === "RANKED"
      );

      let meritToLiquid = meritEarned;
      let meritToHold = 0;

      if (currentMerit + meritEarned > meritCap) {
        meritToLiquid = Math.max(0, meritCap - currentMerit);
        meritToHold = meritEarned - meritToLiquid;
      }

      let userUpdates: Record<string, unknown> = {};

      if (playmode === "RANKED") {
        const newRating = calculateNewRating(
          currentRating,
          result.finalScore
        );

        const newReputation = onRankedCompleted(
          Number(userData.reputation ?? 0)
        );

        userUpdates = {
          merit: FieldValue.increment(meritToLiquid),
          meritHold: FieldValue.increment(meritToHold),
          rating: newRating,
          reputation: newReputation,
          currentlyInRanked: false,
          rankedSessionStartedAt: FieldValue.delete(),
        };
      } else {
        userUpdates = {
          merit: FieldValue.increment(meritToLiquid),
          meritHold: FieldValue.increment(meritToHold),
        };
      }

      await db.runTransaction(async (tx) => {
        tx.update(submissionRef, {
          evaluation: {
            submissionId: snapshot.id,
            finalScore: result.finalScore,
            feedback: result.feedback,
            meritEarned,
            meritToHold,
            resultStatus: "EVALUATED",
          },
          status: "EVALUATED",
          evaluationError: FieldValue.delete(),
        });

        tx.update(userRef, userUpdates);
      });

      console.log("submissionEvaluationEngine: finished successfully", {
        submissionId: snapshot.id,
        meritEarned,
        meritToHold,
      });
    } catch (error) {
      console.error("submissionEvaluationEngine failed", {
        submissionId: snapshot.id,
        error,
      });

      await submissionRef.update({
        status: "FAILED",
        evaluationError:
          error instanceof Error ? error.message : "Unknown evaluation failure",
      });
    }
  }
);
