package com.example.cuisineconnect.domain.usecase.user

import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
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

    override suspend fun storeUser(uid: String, user: User, isUpdate: Boolean): Result<Unit> {
        return userRepository.storeUser(uid, user, isUpdate)
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

    override fun followUser(currentUserId: String, targetUserId: String, callback: TwoWayCallback) {
        userRepository.followUser(currentUserId, targetUserId, callback)
    }

    override fun unfollowUser(
        currentUserId: String,
        targetUserId: String,
        callback: TwoWayCallback
    ) {
        userRepository.unfollowUser(currentUserId, targetUserId, callback)
    }

    override suspend fun getUserBookmarks(userId: String): StateFlow<List<String>> {
        return userRepository.getUserBookmarks(userId)
    }
}