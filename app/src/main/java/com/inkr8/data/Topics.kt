package com.inkr8.data

data class Topic(
    val id: String,
    val themeId: String,
    val name: String = "",
    val description: String = "",
    val difficulty: String = ""
)
