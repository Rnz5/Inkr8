package com.inkr8.data

data class Users(
    val id: Int,
    val name: String = "placeholder",
    val email: String? = null,
    val merit: Int = 1000,
    val rank: String = "rank-placeholder",
    val elo: Int = 0,
    val profileImageURL: String = "",
    val bannerImageURL: String = "",
    val achievements: List<String> = emptyList(),
    val joinedDate: Long = System.currentTimeMillis()

)
