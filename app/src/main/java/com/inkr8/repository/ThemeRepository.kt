package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Theme
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class ThemeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val themesCollection = firestore.collection("themes")

    suspend fun getRandomTheme(): Theme? {
        val randomOffset = Random.nextDouble()

        var snapshot = themesCollection
            .whereGreaterThanOrEqualTo("randomIndex", randomOffset)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            snapshot = themesCollection
                .whereLessThan("randomIndex", randomOffset)
                .limit(1)
                .get()
                .await()
        }

        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(Theme::class.java)?.copy(id = doc.id)
        }
    }
}
