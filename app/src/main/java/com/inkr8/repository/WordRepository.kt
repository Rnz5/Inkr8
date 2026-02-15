package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Words
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class WordRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val wordsCollection = firestore.collection("words")

    suspend fun getRandomWords(limit: Long): List<Words> {
        val snapshot = wordsCollection
            .whereEqualTo("isActive", true)
            .limit(limit)
            .get()
            .await()

        return snapshot.toObjects(Words::class.java).shuffled()
    }

    suspend fun getSingleRandomWord(): Words? {
        return getRandomWords(10).randomOrNull()
    }
}
