import * as functions from "firebase-functions/v1";
import {db} from "../firebase/admin";

export const createUserProfile =
functions.auth.user().onCreate(async (user) => {
  const userRef = db.collection("users").doc(user.uid);

  await userRef.set({
    id: user.uid,
    name: user.displayName ?? "",
    email: user.email ?? "",
    merit: 1000,
    tournamentsPlayed: 0,
    tournamentsWon: 0,
    totalMeritEarned: 0,
    tipsReceived: 0,
    joinedDate: Date.now(),
  });
});

