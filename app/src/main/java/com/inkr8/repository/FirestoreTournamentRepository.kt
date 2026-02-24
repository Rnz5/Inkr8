package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Tournament

class FirestoreTournamentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tournamentsCollection = db.collection("tournaments")

    fun createTournament(
        tournament: Tournament,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        tournamentsCollection.document(tournament.id).set(tournament)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun enrollUser(
        tournamentId: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
    }

}