package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Tournament
import com.inkr8.data.Submissions
import com.inkr8.data.TournamentStatus
import com.inkr8.economic.TournamentRewardCalculator
import com.inkr8.evaluation.R8Evaluator
import com.inkr8.evaluation.SubmissionProcessor
import com.inkr8.mappers.toFirestore

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
        val tournamentRef = tournamentsCollection.document(tournamentId)
        val enrollmentRef = tournamentRef.collection("enrollments").document(userId)

        db.runTransaction { transaction ->

            val tournamentSnapshot = transaction.get(tournamentRef)
            if (!tournamentSnapshot.exists()) {
                throw Exception("Tournament not found")
            }

            val tournament = tournamentSnapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament data")

            if (tournament.status != TournamentStatus.OPEN) {
                throw Exception("Tournament is not open")
            }

            if (tournament.playersCount >= tournament.maxPlayers) {
                throw Exception("Tournament is full")
            }

            if (transaction.get(enrollmentRef).exists()) {
                throw Exception("Already enrolled")
            }


            transaction.set(enrollmentRef, mapOf("joinedAt" to System.currentTimeMillis()))

            transaction.update(tournamentRef, "playersCount", tournament.playersCount + 1)

            // mark full if max players reached :)
            if (tournament.playersCount + 1 >= tournament.maxPlayers) {
                transaction.update(tournamentRef, "status", TournamentStatus.FULL)
            }

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
        val submissionRef = tournamentRef.collection("submissions").document(userId)

        val enrollmentRef = tournamentRef.collection("enrollments").document(userId)

        db.runTransaction { transaction ->
            val tournamentSnapshot = transaction.get(tournamentRef)
            if (!tournamentSnapshot.exists()) {
                throw Exception("Tournament not found")
            }

            val tournament = tournamentSnapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament")

            // tournament must be open or full to not accept submissions
            if (tournament.status != TournamentStatus.OPEN && tournament.status != TournamentStatus.FULL){
                throw Exception("Tournament not accepting submissions")
            }

            // deadline
            if (System.currentTimeMillis() > tournament.deadline) {
                throw Exception("Deadline passed")
            }

            // enrolled
            if (!transaction.get(enrollmentRef).exists()) {
                throw Exception("User not enrolled")
            }

            // only 1 submissions bc only 1 round is supported per tournament by now
            if (transaction.get(submissionRef).exists()) {
                throw Exception("Already submitted")
            }

            // simple matching
            if (submission.authorId != userId) {
                throw Exception("Invalid submission author")
            }

            // save submission
            transaction.set(submissionRef, submission)

            // +1 submissions count
            transaction.update(tournamentRef, "submissionsCount", tournament.submissionsCount + 1)

        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }
    }

    fun closeTournamentIfDeadlinePassed(
        tournamentId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val tournamentRef = tournamentsCollection.document(tournamentId)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(tournamentRef)
            if (!snapshot.exists()) {
                throw Exception("Tournament not found")
            }

            val tournament = snapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament")

            if (System.currentTimeMillis() > tournament.deadline && (tournament.status == TournamentStatus.OPEN || tournament.status == TournamentStatus.FULL)){
                transaction.update(tournamentRef, "status", TournamentStatus.EVALUATING)
            }

        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }
    }

    fun evaluateTournament(
        tournamentId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val tournamentRef = tournamentsCollection.document(tournamentId)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(tournamentRef)

            val tournament = snapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament")

            if (tournament.status != TournamentStatus.EVALUATING) {
                throw Exception("Tournament not ready for evaluation")
            }

            transaction.update(tournamentRef, "status", TournamentStatus.EVALUATING)

        }.addOnSuccessListener {

            tournamentRef.get()
                .addOnSuccessListener { tournamentSnapshot ->

                    val tournament = tournamentSnapshot.toObject(Tournament::class.java)
                        ?: return@addOnSuccessListener onError(Exception("Invalid tournament"))

                    tournamentRef.collection("submissions")
                        .get()
                        .addOnSuccessListener { submissionsSnapshot ->

                            val processor = SubmissionProcessor(R8Evaluator())

                            val submissions = submissionsSnapshot.documents.mapNotNull { it.toObject(Submissions::class.java) }

                            if (submissions.isEmpty()) {
                                onError(Exception("No submissions found"))
                                return@addOnSuccessListener
                            }

                            val evaluatedSubmissions = submissions.map { processor.process(it) }

                            val ranked = evaluatedSubmissions.sortedByDescending { it.evaluation?.finalScore ?: 0.0 }

                            val rewardPercentages = TournamentRewardCalculator.calculateRewardPercentages(ranked.size)

                            val batch = db.batch()

                            ranked.forEachIndexed { index, submission ->

                                val reward = (tournament.prizePool * rewardPercentages.getOrElse(index) { 0.0 }).toLong()

                                val updatedEvaluation = submission.evaluation?.copy(meritEarned = reward, rankLeaderboard = (index + 1).toLong())

                                val submissionRef = tournamentRef.collection("submissions").document(submission.authorId)

                                batch.update(
                                    submissionRef,
                                    mapOf(
                                        "evaluation" to updatedEvaluation?.toFirestore(),
                                        "status" to SubmissionStatus.EVALUATED.name
                                    )
                                )
                            }

                            batch.update(tournamentRef, "status", TournamentStatus.COMPLETED)

                            batch.commit().addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }
                        }
                }

        }.addOnFailureListener { onError(it) }
    }

}