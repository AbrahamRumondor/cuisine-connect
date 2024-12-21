package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.User
import com.google.firebase.firestore.PropertyName

data class UserResponse(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var id: String = "",

    @get:PropertyName("user_language")
    @set:PropertyName("user_language")
    var language: String = "id",

    @get:PropertyName("user_name")
    @set:PropertyName("user_name")
    var name: String = "", // display name

    @get:PropertyName("user_unique_name")
    @set:PropertyName("user_unique_name")
    var uniqueUsername: String = "", // username

    @get:PropertyName("user_email")
    @set:PropertyName("user_email")
    var email: String = "",

    @get:PropertyName("user_password")
    @set:PropertyName("user_password")
    var password: String = "",

    @get:PropertyName("user_bio")
    @set:PropertyName("user_bio")
    var bio: String = "",

    @get:PropertyName("user_recipes")
    @set:PropertyName("user_recipes")
    var recipes: List<String> = emptyList(),

    @get:PropertyName("user_posts")
    @set:PropertyName("user_posts")
    var posts: List<String> = listOf(),

    @get:PropertyName("user_bookmarks")
    @set:PropertyName("user_bookmarks")
    var bookmarks: List<String> = emptyList(),

    @get:PropertyName("user_image")
    @set:PropertyName("user_image")
    var image: String = "",

    @get:PropertyName("user_background")
    @set:PropertyName("user_background")
    var background: String = "",

    @get:PropertyName("user_follower")
    @set:PropertyName("user_follower")
    var follower: List<String> = emptyList(),

    @get:PropertyName("user_following")
    @set:PropertyName("user_following")
    var following: List<String> = emptyList(),

    @get:PropertyName("user_ongoing_post")
    @set:PropertyName("user_ongoing_post")
    var postContent: MutableList<MutableMap<String, String>> = mutableListOf()
) {

    constructor() : this("", "", "", "")

    companion object {
        fun transform(userResponse: UserResponse): User {
            return User(
                id = userResponse.id,
                displayName = userResponse.name,
                email = userResponse.email,
                password = userResponse.password,
                recipes = userResponse.recipes,
                follower = userResponse.follower,
                following = userResponse.following,
                image = userResponse.image,
                posts = userResponse.posts,
                bookmarks = userResponse.bookmarks,
                username = userResponse.uniqueUsername,
                postContent = userResponse.postContent,
                background = userResponse.background,
                bio = userResponse.bio,
                language = userResponse.language
            )
        }

        fun transform(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                name = user.displayName,
                email = user.email,
                password = user.password,
                recipes = user.recipes,
                follower = user.follower,
                following = user.following,
                image = user.image,
                posts = user.posts,
                bookmarks = user.bookmarks,
                uniqueUsername = user.username,
                postContent = user.postContent,
                background = user.background,
                bio = user.bio,
                language = user.language
            )
        }
    }
}