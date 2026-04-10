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
                    val newUser = Users(
                        id = uid,
                        name = "",
                        email = email,
                        merit = 1000,
                        rating = 0,
                        reputation = 0,
                        bestScore = 0.0,
                        submissionsCount = 0,
                        profileImageURL = "",
                        bannerImageURL = "",
                        achievements = emptyList(),
                        joinedDate = System.currentTimeMillis(),
                        rankedWinStreak = 0,
                        rankedLossStreak = 0,
                        currentlyInRanked = false,
                        rankedSessionStartedAt = null,
                        tournamentsPlayed = 0,
                        tournamentsWon = 0,
                        totalMeritEarned = 0,
                        tipsReceived = 0,
                        isPhilosopher = false,
                        philosopherSince = null,
                        hasChosenUsername = false
                    )

                    docRef.set(newUser)
                        .addOnSuccessListener {
                            onReady(newUser)
                        }
                        .addOnFailureListener { it.printStackTrace() }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun validateUsername(username: String): String? {
        if (username.length < 2) return "Username must be at least 2 characters."
        if (username.length > 20) return "Username must be at most 20 characters."

        val allowedRegex = Regex("^[A-Za-z0-9_.,*{}\\[\\]()√]+$")
        if (!allowedRegex.matches(username)) {
            return "Username contains invalid characters."
        }

        val hasLetterOrDigit = username.any { it.isLetterOrDigit() }
        if (!hasLetterOrDigit) {
            return "Username must contain at least one letter or number."
        }

        return null
    }

    private fun normalizeUsername(username: String): String {
        return username.trim().lowercase()
    }

    fun claimUsername(
        userId: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val validationError = validateUsername(username)
        if (validationError != null) {
            onError(Exception(validationError))
            return
        }

        val normalized = normalizeUsername(username)
        val usernameRef = firestore.collection("usernames").document(normalized)
        val userRef = usersCollection.document(userId)

        firestore.runTransaction { transaction ->
            val usernameSnapshot = transaction.get(usernameRef)
            val userSnapshot = transaction.get(userRef)

            if (!userSnapshot.exists()) {
                throw Exception("User document not found.")
            }

            if (usernameSnapshot.exists()) {
                throw Exception("That username is already taken :(")
            }

            transaction.set(
                usernameRef,
                mapOf(
                    "userId" to userId,
                    "username" to username,
                    "normalized" to normalized,
                    "createdAt" to System.currentTimeMillis()
                )
            )

            transaction.update(
                userRef,
                mapOf(
                    "name" to username,
                    "hasChosenUsername" to true
                )
            )
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onError(Exception(it.message ?: "Failed to claim username."))
        }
    }

    fun isUsernameAvailable(
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        val normalized = username.trim().lowercase()

        firestore.collection("usernames")
            .document(normalized)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(!snapshot.exists())
            }
            .addOnFailureListener {
                it.printStackTrace()
                onResult(false)
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

    fun enablePhilosopher(
        userId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update(
                mapOf(
                    "isPhilosopher" to true,
                    "philosopherSince" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun disablePhilosopher(
        userId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update(
                mapOf(
                    "isPhilosopher" to false,
                    "philosopherSince" to null
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

}


