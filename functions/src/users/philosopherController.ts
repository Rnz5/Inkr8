import {onCall, HttpsError} from "firebase-functions/v2/https";
import {db} from "../firebase/admin";

/**
 * googleapis placeholder :-(
 */
async function verifyPurchaseWithGoogle(
  purchaseToken: string,
  productId: string
): Promise<boolean> {
  // implement actual google play developer API call here post haste
  if (!purchaseToken || !productId) return false;

  console.log(`Verifying purchase: ${productId} with token: ${purchaseToken}`);
  return true;
}

export const activatePhilosopherStatus = onCall(
  {
    region: "us-central1",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "User must be authenticated.");
    }

    const {purchaseToken, productId} = request.data;

    if (!purchaseToken || !productId) {
      throw new HttpsError(
        "invalid-argument",
        "Missing purchase verification data (purchaseToken or productId)."
      );
    }

    const userRef = db.collection("users").doc(uid);

    try {
      const userSnap = await userRef.get();
      if (!userSnap.exists) {
        throw new HttpsError("not-found", "User profile not found.");
      }

      const isValid = await verifyPurchaseWithGoogle(purchaseToken, productId);

      if (!isValid) {
        throw new HttpsError(
          "permission-denied",
          "Purchase verification failed."
        );
      }

      const philosopherSince = Date.now();
      const updatedFields = {
        isPhilosopher: true,
        philosopherSince: philosopherSince,
        lastPurchaseToken: purchaseToken,
      };

      await userRef.update(updatedFields);

      return {
        success: true,
        message: "Philosopher status activated.",
        updatedFields: updatedFields,
      };
    } catch (error) {
      console.error("Error activating Philosopher status:", error);
      if (error instanceof HttpsError) throw error;
      throw new HttpsError("internal", "An internal error occurred.");
    }
  }
);
