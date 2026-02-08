package com.inkr8.data

data class Users(
    val id: String,
    val name: String = "",
    val email: String? = null,
    val merit: Int = 1000,
    val rank: String = "",
    val elo: Int = 0,
    val submissionsCount: Int = 0,
    val profileImageURL: String = "",
    val bannerImageURL: String = "",
    val achievements: List<String> = emptyList(),
    val joinedDate: Long = System.currentTimeMillis()
)

