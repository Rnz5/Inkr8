package com.inkr8.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.inkr8.data.Tournament
import com.inkr8.data.Submissions
import com.inkr8.data.TournamentStatus
import com.inkr8.economy.EconomyConfig
import com.inkr8.economy.TournamentEconomyCalculator
import com.inkr8.timing.TournamentTimingConfig


class FirestoreTournamentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()
    private val tournamentsCollection = db.collection("tournaments")

    fun createTournament(
        tournament: Tournament,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        require(tournament.maxPlayers <= 100L)
        require(tournament.prizePool >= 5000L)

        val hostRef = db.collection("users").document(tournament.creatorId)
        val tournamentRef = tournamentsCollection.document(tournament.id)

        db.runTransaction { transaction ->

            val hostSnapshot = transaction.get(hostRef)
            if (!hostSnapshot.exists()) throw Exception("Host not found")

            val hostMerit = hostSnapshot.getLong("merit") ?: 0L
            if (hostMerit < tournament.prizePool) {
                throw Exception("Insufficient merit to fund prize pool")
            }

            val projection = TournamentEconomyCalculator.calculateProjection(
                prizePool = tournament.prizePool,
                maxPlayers = tournament.maxPlayers.toInt()
            )

            val now = System.currentTimeMillis()
            val enrollmentDeadline = now + TournamentTimingConfig.ENROLLMENT_DURATION_MS
            val submissionDeadline = enrollmentDeadline + TournamentTimingConfig.SUBMISSION_DURATION_MS

            val enrichedTournament = tournament.copy(
                entranceFee = projection.entranceFee,
                systemFee = projection.systemFee,
                status = TournamentStatus.ENROLLING,
                playersCount = 0,
                submissionsCount = 0,
                minPlayers = TournamentTimingConfig.MIN_PLAYERS.toLong(),
                enrollmentDeadline = enrollmentDeadline,
                submissionDeadline = submissionDeadline,
                createdAt = now
            )

            // deduct host escrow
            transaction.update(hostRef, "merit", hostMerit - tournament.prizePool)

            transaction.set(tournamentRef, enrichedTournament)

        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }
    }

    fun enrollUser(
        tournamentId: String,
        userId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val tournamentRef = tournamentsCollection.document(tournamentId)
        val enrollmentRef = tournamentRef.collection("enrollments").document(userId)

        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->

            val tournamentSnapshot = transaction.get(tournamentRef)
            if (!tournamentSnapshot.exists()) {
                throw Exception("Tournament not found")
            }

            val tournament = tournamentSnapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament data")

            if (userId == tournament.creatorId) {
                throw Exception("Host cannot enroll in their own tournament")
            }

            if (tournament.status != TournamentStatus.ENROLLING) {
                throw Exception("Enrollment closed")
            }

            if (System.currentTimeMillis() > tournament.enrollmentDeadline) {
                throw Exception("Enrollment period ended")
            }

            if (tournament.playersCount >= tournament.maxPlayers) {
                throw Exception("Tournament is full")
            }

            if (transaction.get(enrollmentRef).exists()) {
                throw Exception("Already enrolled")
            }


            //player enrollment procedure and deduction of entrance fee

            val userSnapshot = transaction.get(userRef)
            if (!userSnapshot.exists()) throw Exception("User not found")

            val currentMerit = userSnapshot.getLong("merit") ?: 0L

            if (currentMerit < tournament.entranceFee) {
                throw Exception(EconomyConfig.insufficientMerit())
            }

            transaction.update(userRef, "merit", currentMerit - tournament.entranceFee)
            transaction.set(enrollmentRef, mapOf("joinedAt" to System.currentTimeMillis()))
            transaction.update(tournamentRef, "playersCount", tournament.playersCount + 1)


        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }
    }

    fun submitToTournament(
        tournamentId: String,
        userId: String,
        submission: Submissions,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val tournamentRef = tournamentsCollection.document(tournamentId)
        val tournamentSubmissionRef = tournamentRef.collection("submissions").document(userId)
        val enrollmentRef = tournamentRef.collection("enrollments").document(userId)

        val globalSubmissionRef = db.collection("submissions").document(submission.id)

        db.runTransaction { transaction ->
            val tournamentSnapshot = transaction.get(tournamentRef)
            if (!tournamentSnapshot.exists()) {
                throw Exception("Tournament not found")
            }

            val tournament = tournamentSnapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament")

            if (userId == tournament.creatorId) {
                throw Exception("Host cannot submit to their own tournament")
            }

            if (tournament.status != TournamentStatus.ACTIVE) {
                throw Exception("Tournament not active")
            }

            if (System.currentTimeMillis() > tournament.submissionDeadline) {
                throw Exception("Deadline passed")
            }

            if (!transaction.get(enrollmentRef).exists()) {
                throw Exception("User not enrolled")
            }

            if (transaction.get(tournamentSubmissionRef).exists()) {
                throw Exception("Already submitted")
            }

            if (submission.authorId != userId) {
                throw Exception("Invalid submission author")
            }

            transaction.set(tournamentSubmissionRef, submission)
            transaction.set(globalSubmissionRef, submission)

            transaction.update(tournamentRef, "submissionsCount", tournament.submissionsCount + 1)

        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getLeaderboard(
        tournamentId: String,
        onSuccess: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("tournaments")
            .document(tournamentId)
            .collection("submissions")
            .orderBy("evaluation.rankLeaderboard")
            .get()
            .addOnSuccessListener { snapshot ->
                val leaderboard = snapshot.toObjects(Submissions::class.java)
                onSuccess(leaderboard)
            }.addOnFailureListener { onError(it) }
    }

    fun sendTournamentTip(
        tournamentId: String,
        tipperId: String,
        recipientId: String,
        amount: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val tipId = "${tipperId}_${recipientId}"

        val tipData = mapOf(
            "tipperId" to tipperId,
            "recipientId" to recipientId,
            "amount" to amount,
            "createdAt" to System.currentTimeMillis(),
            "processed" to false
        )

        db.collection("tournaments")
            .document(tournamentId)
            .collection("tips")
            .document(tipId)
            .set(tipData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    fun listenToTournamentFeed(
        onUpdate: (List<Tournament>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return tournamentsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                val tournaments = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Tournament::class.java)
                    } catch (e: Exception) {
                        Log.e("TournamentRepository", "Failed to parse tournament ${doc.id}", e)
                        null
                    }
                }

                val filtered = tournaments.filter {
                    it.status == TournamentStatus.ENROLLING || it.status == TournamentStatus.ACTIVE
                }

                val sorted = filtered.sortedWith(
                    compareBy<Tournament> {
                        when (it.status) {
                            TournamentStatus.ENROLLING -> 0
                            TournamentStatus.ACTIVE -> 1
                            else -> 2
                        }
                    }.thenBy {
                        when (it.status) {
                            TournamentStatus.ENROLLING -> it.enrollmentDeadline
                            TournamentStatus.ACTIVE -> it.submissionDeadline
                            else -> Long.MAX_VALUE
                        }
                    }
                )

                onUpdate(sorted)
            }
    }

    fun enrollUserViaFunction(
        tournamentId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "tournamentId" to tournamentId
        )

        functions
            .getHttpsCallable("enrollInTournament")
            .call(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error: Exception ->
                onError(Exception(error.message ?: "Failed to enroll"))
            }
    }

    fun listenToEnrollmentStatus(
        tournamentId: String,
        userId: String,
        onUpdate: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return tournamentsCollection
            .document(tournamentId)
            .collection("enrollments")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                onUpdate(snapshot?.exists() == true)
            }
    }


}