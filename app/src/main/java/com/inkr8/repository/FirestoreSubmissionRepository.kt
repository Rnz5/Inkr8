package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
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
        val userRef = usersCollection.document(submission.authorId)

        db.runTransaction { transaction -> transaction.set(submissionRef, firestoreSubmission)

            transaction.update(userRef, "submissionsCount", com.google.firebase.firestore.FieldValue.increment(1))
            null
        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { e -> onError(e) }
    }

    fun getLastSubmission(
        onSuccess: (Submissions?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        submissionsCollection
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
            submissionsCollection.whereEqualTo("authorId", authorId).orderBy("timestamp")
        } else {
            submissionsCollection.orderBy("timestamp")
        }

        query.get().addOnSuccessListener { snapshot ->
            val submissions = snapshot.documents.mapNotNull {
                it.toObject(FirestoreSubmission::class.java)?.toDomain()
            }
            onSuccess(submissions)
        }.addOnFailureListener { onError(it) }
    }
}