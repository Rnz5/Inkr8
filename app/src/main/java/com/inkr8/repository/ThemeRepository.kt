package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Theme
import kotlinx.coroutines.tasks.await

class ThemeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val themesCollection = firestore.collection("themes")

    suspend fun getRandomTheme(): Theme? {
        val snapshot = themesCollection.get().await()
        val themes = snapshot.toObjects(Theme::class.java)

        if (themes.isEmpty()) return null

        return themes.random()
    }
}
