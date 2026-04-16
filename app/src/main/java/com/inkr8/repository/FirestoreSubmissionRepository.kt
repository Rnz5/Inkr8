package com.inkr8.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.functions.functions
import com.inkr8.AuthManager
import com.inkr8.data.Submissions
import com.inkr8.mappers.toDomain
import com.inkr8.mappers.toFirestore

class FirestoreSubmissionRepository() {
    private val db = FirebaseFirestore.getInstance()
    private val submissionsCollection  = db.collection("submissions")
    private val usersCollection = db.collection("users")

    fun addSubmission(
        submission: Submissions,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (submission.authorId.isBlank()) {
            onError(IllegalStateException("Submission authorId cannot be empty"))
            return
        }

        val firestoreSubmission = submission.toFirestore()
        val submissionRef = submissionsCollection.document(firestoreSubmission.id)

        submissionRef.set(firestoreSubmission)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun saveSubmission(
        submissionId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "action" to "SAVE_SUBMISSION",
            "submissionId" to submissionId
        )

        Firebase.functions
            .getHttpsCallable("applyMeritAction")
            .call(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun listenToAllSubmissions(
        authorId: String,
        onUpdate: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return submissionsCollection
            .whereEqualTo("authorId", authorId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val submissions = snapshot?.documents?.mapNotNull {
                    it.toObject(FirestoreSubmission::class.java)?.toDomain()
                } ?: emptyList()
                onUpdate(submissions)
            }
    }

    fun getAllSubmissions(
        authorId: String,
        onSuccess: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        submissionsCollection.whereEqualTo("authorId", authorId).orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { snapshot ->
                val submissions = snapshot.documents.mapNotNull {
                    it.toObject(FirestoreSubmission::class.java)?.toDomain()
                }
                onSuccess(submissions)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getLastSubmission(
        onSuccess: (Submissions?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = AuthManager.currentUser()?.uid ?: return
        submissionsCollection.whereEqualTo("authorId", userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { snapshot ->
                val submission = snapshot.documents.firstOrNull()?.toObject(FirestoreSubmission::class.java)?.toDomain()
                onSuccess(submission)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun getLastSubmissionRealtime(
        onUpdate: (Submissions) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val userId = AuthManager.currentUser()?.uid ?: return null

        return submissionsCollection.whereEqualTo("authorId", userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null) {
                    val submission = doc.toObject(FirestoreSubmission::class.java)?.toDomain()
                    if (submission != null) {
                        onUpdate(submission)
                    }
                }
            }
    }

    fun unlockFeedbackExpansion(
        submissionId: String,
        skipMeritCost: Boolean = false,
        onSuccess: (Long) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val functions = Firebase.functions
        val data = hashMapOf(
            "submissionId" to submissionId,
            "skipMeritCost" to skipMeritCost
        )

        functions
            .getHttpsCallable("unlockFeedbackExpansion")
            .call(data)
            .addOnSuccessListener { result ->
                val map = result.data as? Map<*, *>
                val cost = (map?.get("cost") as? Number)?.toInt() ?: 0
                onSuccess(cost.toLong())
            }
            .addOnFailureListener { e ->
                onError(Exception(e.message ?: "Failed to unlock feedback"))
            }
    }
}
