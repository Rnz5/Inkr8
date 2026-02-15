package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Topic
import kotlinx.coroutines.tasks.await

class TopicRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val topicsCollection = firestore.collection("topics")

    suspend fun getRandomTopicFromTheme(themeId: String): Topic? {
        val snapshot = topicsCollection.whereEqualTo("themeId", themeId).get().await()

        val topics = snapshot.toObjects(Topic::class.java)

        if (topics.isEmpty()) return null

        return topics.random()
    }
}
