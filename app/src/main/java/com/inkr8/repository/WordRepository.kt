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
        val randomOffset = Random.nextDouble()

        val snapshot = wordsCollection
            .whereEqualTo("isActive", true)
            .whereGreaterThanOrEqualTo("randomIndex", randomOffset)
            .limit(limit)
            .get()
            .await()

        val results = mutableListOf<Words>()
        results.addAll(snapshot.documents.mapNotNull { doc -> 
            doc.toObject(Words::class.java)?.copy(id = doc.id) 
        })

        if (results.size < limit) {
            val remaining = limit - results.size
            val secondSnapshot = wordsCollection
                .whereEqualTo("isActive", true)
                .whereLessThan("randomIndex", randomOffset)
                .limit(remaining)
                .get()
                .await()
            
            results.addAll(secondSnapshot.documents.mapNotNull { doc -> 
                doc.toObject(Words::class.java)?.copy(id = doc.id) 
            })
        }

        return results.shuffled()
    }

    suspend fun getSingleRandomWord(): Words? {
        return getRandomWords(1).firstOrNull()
    }

    suspend fun getWordsByTexts(wordTexts: List<String>): List<Words> {
        if (wordTexts.isEmpty()) return emptyList()

        val snapshot = firestore.collection("words")
            .whereIn("word", wordTexts)
            .get()
            .await()
        
        val fetched = snapshot.toObjects(Words::class.java)

        return wordTexts.mapNotNull { text ->
            fetched.find { it.word.equals(text, ignoreCase = true) }
        }
    }
}
