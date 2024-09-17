package com.example.cuisineconnect.app.listener

interface DetailRecipeItemListener {
  fun onAddressClicked(position: Int)
  fun onAddItemClicked(position: Int, menuId: String)
  fun onDecreaseItemClicked(position: Int, menuId: String)
  fun onDeleteItemClicked(position: Int, menuId: String)
  fun onRadioButtonClicked(position: Int, id: Int)
  fun onNotesFilled(notes: String)
  fun onCheckoutButtonClicked()
  // uses {} for default implementation (so dont have to impelemnts).
}