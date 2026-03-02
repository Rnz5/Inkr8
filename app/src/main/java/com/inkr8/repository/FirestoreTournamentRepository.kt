package com.inkr8.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Tournament
import com.inkr8.data.Submissions
import com.inkr8.data.TournamentStatus
import com.inkr8.economy.EconomyConfig
import com.inkr8.economy.TournamentEconomyCalculator
import com.inkr8.economy.TournamentRewardCalculator
import com.inkr8.evaluation.R8Evaluator
import com.inkr8.evaluation.SubmissionProcessor
import com.inkr8.mappers.toFirestore
import com.inkr8.timing.TournamentTimingConfig

class FirestoreTournamentRepository {
    private val db = FirebaseFirestore.getInstance()
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
        val submissionRef = tournamentRef.collection("submissions").document(userId)

        val enrollmentRef = tournamentRef.collection("enrollments").document(userId)

        db.runTransaction { transaction ->
            val tournamentSnapshot = transaction.get(tournamentRef)
            if (!tournamentSnapshot.exists()) {
                throw Exception("Tournament not found")
            }

            val tournament = tournamentSnapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament")

            if (tournament.status != TournamentStatus.ACTIVE) {
                throw Exception("Tournament not active")
            }

            // deadline
            if (System.currentTimeMillis() > tournament.submissionDeadline) {
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

    fun updateTournamentPhase(
        tournamentId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val tournamentRef = tournamentsCollection.document(tournamentId)

        db.runTransaction { transaction ->

            val snapshot = transaction.get(tournamentRef)
            if (!snapshot.exists()) throw Exception("Tournament not found")

            val tournament = snapshot.toObject(Tournament::class.java) ?: throw Exception("Invalid tournament")

            val now = System.currentTimeMillis()

            when (tournament.status) {
                TournamentStatus.ENROLLING -> {
                    if (now > tournament.enrollmentDeadline) {

                        if (tournament.playersCount >= tournament.minPlayers) {
                            transaction.update(tournamentRef, "status", TournamentStatus.ACTIVE)
                        } else {
                            transaction.update(tournamentRef, "status", TournamentStatus.CANCELLED)
                            transaction.update(tournamentRef, "cancelledAt", now)
                        }
                    }
                }

                TournamentStatus.ACTIVE -> {
                    if (now > tournament.submissionDeadline) {
                        transaction.update(tournamentRef, "status", TournamentStatus.EVALUATING)
                    }
                }
                else -> { }
            }

        }.addOnSuccessListener { tournamentRef.get().addOnSuccessListener { snapshot ->
            val tournament = snapshot.toObject(Tournament::class.java)
            if (tournament?.status == TournamentStatus.CANCELLED) {
                refundCancelledTournament(tournamentId, onSuccess, onError)
            } else { onSuccess() } }
        }.addOnFailureListener { onError(it) }
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

                            // cancel tournament if not submissions
                            if (submissions.isEmpty()) {

                                val cancelBatch = db.batch()
                                cancelBatch.update(tournamentRef, "status", TournamentStatus.CANCELLED)

                                cancelBatch.commit().addOnSuccessListener { refundCancelledTournament(tournamentId, onSuccess, onError) }.addOnFailureListener { onError(it) }
                                return@addOnSuccessListener
                            }

                            val evaluatedSubmissions = submissions.map { processor.process(it) }

                            val ranked = evaluatedSubmissions.sortedByDescending { it.evaluation?.finalScore ?: 0.0 }

                            val rewardPercentages = TournamentRewardCalculator.calculateRewardPercentages(ranked.size)

                            val batch = db.batch()

                            ranked.forEachIndexed { index, submission ->

                                //users rewards
                                val reward = (tournament.prizePool * rewardPercentages.getOrElse(index) { 0.0 }).toLong()

                                val updatedEvaluation = submission.evaluation?.copy(meritEarned = reward, rankLeaderboard = (index + 1).toLong())

                                val submissionRef = tournamentRef.collection("submissions").document(submission.authorId)

                                batch.update(submissionRef, mapOf("evaluation" to updatedEvaluation?.toFirestore(), "status" to SubmissionStatus.EVALUATED.name))

                                val winnerRef = db.collection("users").document(submission.authorId)
                                batch.update(winnerRef, "merit", FieldValue.increment(reward))
                            }

                            batch.update(tournamentRef, "status", TournamentStatus.COMPLETED)

                            //host rewards
                            val totalRevenue = tournament.entranceFee * tournament.playersCount
                            val hostProfit = totalRevenue - tournament.prizePool - tournament.systemFee

                            if(hostProfit > 0){
                                val hostRef = db.collection("users").document(tournament.creatorId)
                                batch.update(hostRef, "merit", com.google.firebase.firestore.FieldValue.increment(hostProfit))
                            }


                            batch.commit().addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }
                        }
                }

        }.addOnFailureListener { onError(it) }
    }

    fun refundCancelledTournament(
        tournamentId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val tournamentRef = tournamentsCollection.document(tournamentId)

        tournamentRef.get().addOnSuccessListener { snapshot ->

                val tournament = snapshot.toObject(Tournament::class.java) ?: return@addOnSuccessListener onError(Exception("Invalid tournament"))

                if (tournament.status != TournamentStatus.CANCELLED) {
                    return@addOnSuccessListener onError(Exception("Tournament not cancelled"))
                }
                if (tournament.refunded) {
                    return@addOnSuccessListener onError(Exception("Already refunded"))
                }

                tournamentRef.collection("enrollments").get().addOnSuccessListener { enrollmentSnapshot ->

                        val enrollmentDocs = enrollmentSnapshot.documents

                        db.runTransaction { transaction ->

                            val freshSnapshot = transaction.get(tournamentRef)
                            val freshTournament = freshSnapshot.toObject(Tournament::class.java) ?: throw Exception("Invalid tournament")

                            if (freshTournament.refunded){
                                throw Exception("Already refunded")
                            }

                            // refund players
                            enrollmentDocs.forEach { documentSnapshot ->

                                val userId = documentSnapshot.id
                                val userRef = db.collection("users").document(userId)

                                transaction.update(userRef, "merit", com.google.firebase.firestore.FieldValue.increment(freshTournament.entranceFee))
                            }

                            // refund host
                            val hostRef = db.collection("users").document(freshTournament.creatorId)

                            transaction.update(hostRef, "merit", com.google.firebase.firestore.FieldValue.increment(freshTournament.prizePool))

                            // mark refunded
                            transaction.update(tournamentRef, "refunded", true)

                        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }

                    }.addOnFailureListener { onError(it) }

            }.addOnFailureListener { onError(it) }
    }

}