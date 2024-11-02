package com.example.cuisineconnect.domain.model

data class User(
    var id: String = "",
    val name: String = "loading...",
    val email: String = "",
    val password: String = "",
    val recipes: List<String> = listOf(),
    val bookmarks: List<String> = emptyList(),
    val posts: List<String> = listOf(),
    val image: String = "",
    val follower: List<String> = listOf(),
    val following: List<String> = listOf()
)
