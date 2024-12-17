package com.example.cuisineconnect.domain.model

data class User(
    var id: String = "",
    val displayName: String = "loading...",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val bio: String = "",
    val recipes: List<String> = listOf(),
    val bookmarks: List<String> = emptyList(),
    val posts: List<String> = listOf(),
    val image: String = "",
    val background: String = "",
    val follower: List<String> = listOf(),
    val following: List<String> = listOf(),
    val postContent: MutableList<MutableMap<String, String>> = mutableListOf()
)
