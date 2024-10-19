package com.example.cuisineconnect.domain.usecase.user

import com.example.cuisineconnect.domain.model.User
import com.example.cuisineconnect.domain.repository.AuthRepository
import com.example.cuisineconnect.domain.repository.UserRepository
import com.example.cuisineconnect.domain.usecase.user.UserUseCase
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class UserUseCaseImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : UserUseCase {

    override suspend fun getCurrentUser(): StateFlow<User> {
        return userRepository.getCurrentUser(authRepository.getCurrentUserID())
    }

    override suspend fun getUserByUserId(userId: String): User? {
        return userRepository.getUserByUserId(userId)
    }

    override suspend fun storeUser(uid: String, user: User) {
        userRepository.storeUser(uid, user)
    }

    override fun addRecipeToUser(newRecipe: String) {
        userRepository.addRecipeToUser(newRecipe)
    }

    override fun addPostToUser(newPost: String) {
        userRepository.addPostToUser(newPost)
    }

    override suspend fun getUsersStartingWith(prefix: String): List<User> {
        return userRepository.getUsersStartingWith(prefix)
    }
}