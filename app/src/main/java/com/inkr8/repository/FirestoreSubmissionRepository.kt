package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Submissions
import com.inkr8.mappers.toDomain
import com.inkr8.mappers.toFirestore

class FirestoreSubmissionRepository {
    private val userId: String
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("submissions")

    fun addSubmission(
        submission: Submissions,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val firestoreSubmission = submission.toFirestore()

        collection
            .document(firestoreSubmission.id)
            .set(firestoreSubmission)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    fun getLastSubmission(onSuccess: (Submissions?) -> Unit, onError: (Exception) -> Unit
    ) {
        collection.orderBy("timestamp").limitToLast(1).get().addOnSuccessListener { snapshot ->
                val submission = snapshot.documents.firstOrNull()?.toObject(FirestoreSubmission::class.java)?.toDomain()

                onSuccess(submission)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getAllSubmissions(
        onSuccess: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        collection.orderBy("timestamp").get().addOnSuccessListener { snapshot ->
                val submissions = snapshot.documents.mapNotNull { it.toObject(FirestoreSubmission::class.java)?.toDomain() }
                onSuccess(submissions)
            }
            .addOnFailureListener { onError(it) }
    }
}