import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db} from "../firebase/admin";
import {calculateTournamentProjection} from "./systemTournamentEconomy";

type TournamentRequirements = {
  minRating?: number | null;
  maxRating?: number | null;
  minReputation?: number | null;
  minMerit?: number | null;
};

export const createUserTournament = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "You must be signed in.");
    }

    const title = String(request.data?.title ?? "").trim();
    const gamemode = String(request.data?.gamemode ?? "STANDARD").trim();
    const prizePool = Number(request.data?.prizePool ?? 0);
    const maxPlayers = Number(request.data?.maxPlayers ?? 0);

    const requirements = (request.data?.requirements ?? {}) as
     TournamentRequirements;

    if (!title) {
      throw new HttpsError("invalid-argument", "Missing tournament title.");
    }

    if (gamemode !== "STANDARD" && gamemode !== "ON_TOPIC") {
      throw new HttpsError("invalid-argument", "Invalid gamemode.");
    }

    if (!Number.isFinite(prizePool) || prizePool < 5000) {
      throw new HttpsError(
        "invalid-argument",
        "Prize pool must be at least 5000."
      );
    }

    if (!Number.isFinite(maxPlayers) || maxPlayers < 2 || maxPlayers > 100) {
      throw new HttpsError(
        "invalid-argument",
        "Max players must be between 2 and 100."
      );
    }

    const minPlayers = 6;

    const userRef = db.collection("users").doc(uid);
    const tournamentRef = db.collection("tournaments").doc();

    await db.runTransaction(async (tx) => {
      const userSnap = await tx.get(userRef);

      if (!userSnap.exists) {
        throw new HttpsError("not-found", "User not found.");
      }

      const user = userSnap.data();
      const currentMerit = user?.merit ?? 0;
      const creatorName = user?.name ?? "Unknown Host";

      if (currentMerit < prizePool) {
        throw new HttpsError("failed-precondition", "Not enough Merit.");
      }

      const projection = calculateTournamentProjection({
        prizePool,
        maxPlayers,
      });

      const now = Date.now();
      const enrollmentDeadline = now + (12 * 60 * 60 * 1000);
      const submissionDeadline = enrollmentDeadline + (24 * 60 * 60 * 1000);

      tx.update(userRef, {
        merit: currentMerit - prizePool,
      });

      tx.set(tournamentRef, {
        id: tournamentRef.id,
        title,
        creatorId: uid,
        creatorName,
        prizePool,
        maxPlayers,
        minPlayers,
        playersCount: 0,
        submissionsCount: 0,
        entranceFee: projection.entranceFee,
        systemFee: projection.systemFee,
        enrollmentDeadline,
        submissionDeadline,
        refunded: false,
        requirements: {
          minRating: requirements.minRating ?? null,
          maxRating: requirements.maxRating ?? null,
          minReputation: requirements.minReputation ?? null,
          minMerit: requirements.minMerit ?? null,
        },
        status: "ENROLLING",
        strictnessMultiplier: 0.92,
        createdAt: now,
        gamemode,
        requiredWords: [],
        themeId: null,
        themeName: null,
        topicId: null,
        topicName: null,
        isSystemHosted: false,
      });
    });

    return {
      success: true,
      tournamentId: tournamentRef.id,
    };
  }
);
