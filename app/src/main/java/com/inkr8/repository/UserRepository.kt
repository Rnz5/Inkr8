package com.inkr8.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.google.firebase.functions.FirebaseFunctions

class UserRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection("users")

    fun getAllUsers(onResult: (List<Users>) -> Unit) {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(Users::class.java)
                onResult(users)
            }
    }

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
                    val user = snapshot.toObject(Users::class.java)
                    if (user != null) {
                        onReady(user)
                    }
                } else {
                    docRef.addSnapshotListener { newSnapshot, _ ->
                        if (newSnapshot != null && newSnapshot.exists()) {
                            val user = newSnapshot.toObject(Users::class.java)
                            if (user != null) {
                                onReady(user)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun applyMeritAction(
        action: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "action" to action
        )

        functions
            .getHttpsCallable("applyMeritAction")
            .call(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error: Exception ->
                onError(Exception(error.message ?: "Failed to apply merit action"))
            }
    }

    fun getUserById(
        userId: String,
        onResult: (Users?) -> Unit
    ) {
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onResult(snapshot.toObject(Users::class.java))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(null)
            }
    }

    fun updateEmail(userId: String, email: String?) {
        usersCollection.document(userId)
            .update("email", email)
    }

    fun updateRatingAndStreak(
        userId: String,
        newRating: Long,
        winStreak: Long,
        lossStreak: Long
    ) {
        val updates = mapOf(
            "rating" to newRating,
            "rankedWinStreak" to winStreak,
            "rankedLossStreak" to lossStreak
        )

        firestore.collection("users").document(userId).update(updates)
    }

    fun getTop100Users(
        onResult: (List<Users>) -> Unit
    ) {
        firestore.collection("users").orderBy("rating", Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener {
            snapshot ->
                val users = snapshot.documents.mapNotNull { it.toObject(Users::class.java) }
                onResult(users)
            }
    }

    fun updateReputation(userId: String, newReputation: Long) {
        usersCollection.document(userId).update("reputation", newReputation)
    }

    fun startRankedSession(userId: String) {
        val updates = mapOf("currentlyInRanked" to true, "rankedSessionStartedAt" to System.currentTimeMillis())

        usersCollection.document(userId).update(updates)
    }

    fun finishRankedSession(userId: String) {
        val updates = mapOf("currentlyInRanked" to false, "rankedSessionStartedAt" to null)

        usersCollection.document(userId).update(updates)
    }

    fun getUsersByIds(
        userIds: List<String>,
        onResult: (Map<String, Users>) -> Unit
    ) {
        val distinctIds = userIds.distinct().filter { it.isNotBlank() }

        if (distinctIds.isEmpty()) {
            onResult(emptyMap())
            return
        }

        firestore.collection("users")
            .whereIn("id", distinctIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(Users::class.java)
                onResult(users.associateBy { it.id })
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(emptyMap())
            }
    }

}


