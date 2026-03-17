import * as admin from "firebase-admin";

admin.initializeApp();

export const db = admin.firestore();
export const FieldValue = admin.firestore.FieldValue;
