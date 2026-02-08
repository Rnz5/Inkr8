package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Submissions
import com.inkr8.mappers.toDomain
import com.inkr8.mappers.toFirestore

class FirestoreSubmissionRepository() {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("submissions")


    fun addSubmission(
        submission: Submissions,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val firestoreSubmission = submission.toFirestore()

        if (submission.authorId.isBlank()) {
            onError(IllegalStateException("Submission authorId cannot be empty"))
            return
        }

        collection
            .document(firestoreSubmission.id)
            .set(firestoreSubmission)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getLastSubmission(
        onSuccess: (Submissions?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection
            .orderBy("timestamp")
            .limitToLast(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val submission = snapshot.documents.firstOrNull()?.toObject(FirestoreSubmission::class.java)?.toDomain()
                onSuccess(submission)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun getAllSubmissions(
        authorId: String? = null,
        onSuccess: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val query = if (authorId != null) {
            collection.whereEqualTo("authorId", authorId).orderBy("timestamp")
        } else {
            collection.orderBy("timestamp")
        }

        query.get().addOnSuccessListener { snapshot ->
            val submissions = snapshot.documents.mapNotNull {
                it.toObject(FirestoreSubmission::class.java)?.toDomain()
            }
            onSuccess(submissions)
        }.addOnFailureListener { onError(it) }
    }
}