package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Users

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection("users")

    fun ensureUserExists(
        uid: String,
        name: String,
        email: String?,
        onReady: (Users) -> Unit
    ) {
        val docRef = usersCollection.document(uid)

        docRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.toObject(Users::class.java)!!
                    onReady(user)
                } else {
                    val newUser = Users(
                        id = uid,
                        name = name,
                        email = email
                    )

                    docRef.set(newUser)
                        .addOnSuccessListener {
                            onReady(newUser)
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()

            }
    }
}
