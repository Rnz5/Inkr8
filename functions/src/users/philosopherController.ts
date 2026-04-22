import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db} from "../firebase/admin";

export const activatePhilosopherStatus = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "User must be authenticated to activate Philosopher status.");
    }

    const userRef = db.collection("users").doc(uid);

    try {
      const userSnap = await userRef.get();
      if (!userSnap.exists) {
        throw new HttpsError("not-found", "User profile not found.");
      }

      await userRef.update({
        isPhilosopher: true,
        philosopherSince: Date.now(),
      });

      return {
        success: true,
        message: "Philosopher status activated.",
      };
    } catch (error) {
      console.error("Error activating Philosopher status:", error);
      if (error instanceof HttpsError) throw error;
      throw new HttpsError("internal", "An internal error occurred while updating status.");
    }
  }
);
