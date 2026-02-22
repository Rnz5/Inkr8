package com.inkr8.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inkr8.data.Users
import com.inkr8.economic.EconomyConfig

class UserRepository(
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

    fun spendMerit(
        userId: String,
        amount: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val userRef = usersCollection.document(userId)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(userRef)
            val currentMerit = (snapshot.get("merit") as? Number)?.toLong() ?: 0L

            if (currentMerit < amount) {
                throw Exception(EconomyConfig.insuffientMerit())
            }else{
                transaction.update(userRef, "merit", currentMerit - amount)
            }
            null
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun addMerit(userId: String, amount: Long) {
        usersCollection.document(userId)
            .update("merit", FieldValue.increment(amount))
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



}


