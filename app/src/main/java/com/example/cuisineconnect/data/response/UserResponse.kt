package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.User
import com.google.firebase.firestore.PropertyName

data class UserResponse(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var id: String = "",

    @get:PropertyName("user_name")
    @set:PropertyName("user_name")
    var name: String = "",

    @get:PropertyName("user_email")
    @set:PropertyName("user_email")
    var email: String = "",

    @get:PropertyName("user_password")
    @set:PropertyName("user_password")
    var password: String = "",

    @get:PropertyName("user_recipes")
    @set:PropertyName("user_recipes")
    var recipes: List<String> = listOf(),

    @get:PropertyName("user_image")
    @set:PropertyName("user_image")
    var image: String = "",

    @get:PropertyName("user_follower")
    @set:PropertyName("user_follower")
    var follower: List<String> = listOf(),

    @get:PropertyName("user_following")
    @set:PropertyName("user_following")
    var following: List<String> = listOf()
) {

    constructor() : this("", "", "", "")

    companion object {
        fun transform(userResponse: UserResponse): User {
            return User(
                id = userResponse.id,
                name = userResponse.name,
                email = userResponse.email,
                password = userResponse.password,
                recipes = userResponse.recipes,
                follower = userResponse.follower,
                following = userResponse.following,
                image = userResponse.image
            )
        }

        fun transform(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                name = user.name,
                email = user.email,
                password = user.password,
                recipes = user.recipes,
                follower = user.follower,
                following = user.following,
                image = user.image
            )
        }
    }
}