package com.inkr8.data

import com.google.firebase.Timestamp

data class Words(
    val id: String = "",
    val word: String = "",
    val type: String = "",
    val definition: String = "",
    val pronunciation: String = "",
    val sentence: String = "",
    val frequencyScore: Int = 100,
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null
)

