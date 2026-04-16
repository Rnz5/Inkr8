package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Topic
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class TopicRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val topicsCollection = firestore.collection("topics")

    suspend fun getRandomTopicFromTheme(themeId: String): Topic? {
        val randomOffset = Random.nextDouble()

        var snapshot = topicsCollection
            .whereEqualTo("themeId", themeId)
            .whereGreaterThanOrEqualTo("randomIndex", randomOffset)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            snapshot = topicsCollection
                .whereEqualTo("themeId", themeId)
                .whereLessThan("randomIndex", randomOffset)
                .limit(1)
                .get()
                .await()
        }

        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(Topic::class.java)?.copy(id = doc.id)
        }
    }
}
