package com.inkr8.data

data class Users(
    val id: String = "", //<- this caused a crash [= ""] and that fixed it ... o_o
    val name: String = "",
    val email: String? = null,
    var merit: Long = 1000,
    val rank: String = "",
    val elo: Long = 0,
    val bestScore: Double = 0.0,
    val submissionsCount: Long = 0,
    val profileImageURL: String = "",
    val bannerImageURL: String = "",
    val achievements: List<String> = emptyList(),
    val joinedDate: Long = System.currentTimeMillis()
)

