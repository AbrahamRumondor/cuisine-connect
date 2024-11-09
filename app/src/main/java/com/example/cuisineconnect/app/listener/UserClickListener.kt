package com.example.cuisineconnect.app.listener

import com.example.cuisineconnect.domain.model.User

interface UserClickListener {
  fun onUserClicked(userId: String) {}
  fun onUserClicked(user: User) {}
  // uses {} for default implementation (so dont have to impelemnts).
}