import * as admin from "firebase-admin";
import * as functions from "firebase-functions";

admin.initializeApp();

export const addUserToFirestore = functions
  .region("europe-west2")
  .auth.user()
  .onCreate((user) => {
    // creates document in Firestore for new user
    var usersRef = admin.firestore().collection("users");
    return usersRef.doc(user.uid).set({
      displayName: user.displayName,
    });
  });

export const removeUserFromFirestore = functions
  .region("europe-west2")
  .auth.user()
  .onDelete((user) => {
    // removes user's Firestore document on delete user
    var usersRef = admin.firestore().collection("users");
    return usersRef.doc(user.uid).delete();
  });
