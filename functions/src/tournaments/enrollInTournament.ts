import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db, FieldValue} from "../firebase/admin";

export const enrollInTournament = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "You must be signed in.");
    }

    const tournamentId = request.data.tournamentId;
    if (!tournamentId || typeof tournamentId !== "string") {
      throw new HttpsError("invalid-argument", "Missing tournamentId.");
    }

    const tournamentRef = db.collection("tournaments").doc(tournamentId);
    const enrollmentRef = tournamentRef.collection("enrollments").doc(uid);
    const userRef = db.collection("users").doc(uid);

    try {
      await db.runTransaction(async (tx) => {
        const tournamentSnap = await tx.get(tournamentRef);
        if (!tournamentSnap.exists) {
          throw new HttpsError("not-found", "Tournament not found.");
        }

        const tournament = tournamentSnap.data();
        if (!tournament) {
          throw new HttpsError("internal", "Invalid tournament data.");
        }

        if (uid === tournament.creatorId) {
          throw new HttpsError("failed-precondition", "Host cannot enroll.");
        }

        if (tournament.status !== "ENROLLING") {
          throw new HttpsError("failed-precondition", "Enrollment closed.");
        }

        if (Date.now() > tournament.enrollmentDeadline) {
          throw new HttpsError("failed-precondition",
            "Enrollment period ended.");
        }

        if (tournament.playersCount >= tournament.maxPlayers) {
          throw new HttpsError("failed-precondition", "Tournament is full.");
        }

        const existingEnrollment = await tx.get(enrollmentRef);
        if (existingEnrollment.exists) {
          throw new HttpsError("already-exists", "Already enrolled.");
        }

        const userSnap = await tx.get(userRef);
        if (!userSnap.exists) {
          throw new HttpsError("not-found", "User not found.");
        }

        const user = userSnap.data();
        const currentMerit = user?.merit ?? 0;
        const entranceFee = tournament.entranceFee;

        if (currentMerit < entranceFee) {
          throw new HttpsError("failed-precondition", "Not enough Merit.");
        }

        const newBalance = currentMerit - entranceFee;

        tx.update(userRef, {
          merit: newBalance,
        });

        tx.set(enrollmentRef, {
          joinedAt: Date.now(),
          userId: uid,
        });

        tx.update(tournamentRef, {
          playersCount: FieldValue.increment(1),
        });

        const txRef = userRef.collection("meritTransactions").doc();
        tx.set(txRef, {
          amount: -entranceFee,
          reason: "ENTER_TOURNAMENT",
          timestamp: Date.now(),
          balanceAfter: newBalance,
        });
      });

      return {
        success: true,
        message: "Enrolled successfully.",
      };
    } catch (error) {
      if (error instanceof HttpsError) {
        throw error;
      }

      console.error("enrollInTournament failed:", error);
      throw new HttpsError("internal", "Failed to enroll in tournament.");
    }
  }
);
