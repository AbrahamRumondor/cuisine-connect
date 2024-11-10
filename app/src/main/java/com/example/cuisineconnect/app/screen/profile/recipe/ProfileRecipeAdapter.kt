package com.example.cuisineconnect.app.screen.profile.recipe

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.app.screen.recipe.viewHolder.ItemRecipeViewHolder
import com.example.cuisineconnect.databinding.ItemRecipeBigImageBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileRecipeAdapter : RecyclerView.Adapter<ItemRecipeViewHolder>() {

  private var createPostViewModel: CreatePostViewModel? = null

  private var items: MutableList<Pair<User?, Recipe?>> = mutableListOf()
  private var recipeItemListener: ItemListListener? = null

  // Use MutableStateFlow to track population state
  private val _isPopulated = MutableStateFlow(false)
  val isPopulated: StateFlow<Boolean> get() = _isPopulated

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemRecipeViewHolder {
    _isPopulated.value = false
    val inflater = LayoutInflater.from(parent.context)
    val bindingRecipe = ItemRecipeBigImageBinding.inflate(inflater, parent, false)
    return ItemRecipeViewHolder(bindingRecipe)
  }

  override fun onBindViewHolder(holder: ItemRecipeViewHolder, position: Int) {
    _isPopulated.value = true // Set the value to true once the item is bound
    val user = items[position].first
    val recipe = items[position].second
    holder.bind(user, recipe, recipeItemListener, createPostViewModel)
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun submitRecipeList(recipeItems: MutableList<Pair<User?, Recipe?>>) {
    Log.d("ProfileRecipeAdapter", "List of recipes: $recipeItems")

    this.items.clear()
    this.items.addAll(recipeItems)
    notifyDataSetChanged()
  }

  fun setItemListener(recipeItemListener: ItemListListener) {
    this.recipeItemListener = recipeItemListener
  }

  fun addViewModel(createPostViewModel: CreatePostViewModel) {
    this.createPostViewModel = createPostViewModel
  }

  fun removeRecipe(recipeId: String) {
    val indexToRemove = items.indexOfFirst {
      it.second?.id == recipeId
    }

    if (indexToRemove != -1) {
      items.removeAt(indexToRemove)
      notifyItemRemoved(indexToRemove)
    }
  }
}