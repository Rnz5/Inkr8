import * as functions from "firebase-functions/v1";
import {db, FieldValue} from "../firebase/admin";

export const createUserProfile =
functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;
  const userRef = db.collection("users").doc(uid);

  await userRef.set({
    id: uid,
    name: user.displayName ?? "",
    email: user.email ?? "",
    merit: 1000,
    rating: 0,
    reputation: 0,
    bestScore: 0.0,
    submissionsCount: 0,
    tournamentsPlayed: 0,
    tournamentsWon: 0,
    totalMeritEarned: 0,
    tipsReceived: 0,
    joinedDate: Date.now(),
    hasChosenUsername: false,
    isPhilosopher: false,
  });

  if (uid !== "R8") {
    const statsRef = db.collection("metadata").doc("rankings");
    await statsRef.set({
      leagueCounts: {
        SCRIBE: FieldValue.increment(1),
      },
    }, {merge: true});
  }
});
