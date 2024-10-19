package com.example.cuisineconnect.app.listener

import com.example.cuisineconnect.domain.model.User

interface OnClickItemListener {
  fun onPromptClicked(prompt: String){}
  fun onUserClicked(user: User){}
}