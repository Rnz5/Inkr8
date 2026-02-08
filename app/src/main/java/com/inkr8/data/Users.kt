package com.inkr8.data

data class Users(
    val id: String = "",
    val name: String = "",
    val email: String? = null,
    val merit: Int = 1000,
    val rank: String = "",
    val elo: Long = 0,
    val submissionsCount: Long = 0,
    val profileImageURL: String = "",
    val bannerImageURL: String = "",
    val achievements: List<String> = emptyList(),
    val joinedDate: Long = System.currentTimeMillis(),

)
