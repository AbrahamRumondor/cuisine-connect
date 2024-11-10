package com.example.cuisineconnect.app.screen.collection.myRecipe

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.collection.myRecipe.MyRecipeRecyclerViewAdapter.ViewHolder

import com.example.cuisineconnect.app.screen.collection.placeholder.PlaceholderContent.PlaceholderItem
import com.example.cuisineconnect.databinding.FragmentMyRecipeListBinding
import com.example.cuisineconnect.databinding.ItemRecipeBigImageBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyRecipeRecyclerViewAdapter(
  private val myRecipeFragment: FragmentMyRecipeListBinding
) : RecyclerView.Adapter<ViewHolder>() {

  private var recipes: List<Pair<User?, Recipe>> = listOf()
  private var recipeListListener: RecipeListListener? = null

  private var isMyRecipes = false

  // Use MutableStateFlow to track population state
  private val _isPopulated = MutableStateFlow(false)
  val isPopulated: StateFlow<Boolean> get() = _isPopulated

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    _isPopulated.value = false
    return ViewHolder(
      ItemRecipeBigImageBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )

  }

  @SuppressLint("SimpleDateFormat")
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    _isPopulated.value = true // Set the value to true once the item is bound
    val item = recipes[position]
    holder.title.text = item.second.title
//    holder.description.text = item.second.description

    val dateFormat = SimpleDateFormat("MMM dd")
    val formattedDate = dateFormat.format(item.second.date)

    holder.date.text = formattedDate
    holder.recipeItem.setOnClickListener {
      recipeListListener?.onRecipeClicked(item.second.id)
      Toast.makeText(
        myRecipeFragment.root.context,
        "you clicked ${item.second.title}",
        Toast.LENGTH_SHORT
      ).show()
    }
    holder.recipeItem.setOnLongClickListener {
      recipeListListener?.onRecipeLongClicked(item.second.id)
      val builder = AlertDialog.Builder(
        myRecipeFragment.root.context,
      )
      builder.setTitle("Choose this recipe?")
      builder.setPositiveButton("Yes") { dialog, _ ->
        Toast.makeText(
          myRecipeFragment.root.context,
          "${item.second.title} is chosen",
          Toast.LENGTH_SHORT
        ).show()
        recipeListListener?.onRecipeLongClicked(item.second.id)
        dialog.dismiss()
      }
      builder.setNegativeButton("No") { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
      true
    }
    holder.deleteRecipe.setOnClickListener {
      val builder = AlertDialog.Builder(myRecipeFragment.root.context)
      builder.setTitle("Delete Recipe")
      builder.setMessage("Are you sure you want to delete the recipe?")

      builder.setPositiveButton("Yes") { dialog, _ ->
        Toast.makeText(
          myRecipeFragment.root.context,
          "${item.second.title} deleted",
          Toast.LENGTH_SHORT
        ).show()

        recipeListListener?.onItemDeleteClicked(item.second.id, "recipe")
        dialog.dismiss() // Dismiss the dialog
      }

      builder.setNegativeButton("No") { dialog, _ ->
        dialog.dismiss()
      }

      builder.create().show()
    }
    val uri = Uri.parse(item.second.image)
    Glide.with(myRecipeFragment.root)
      .load(uri)   // Load the image URL into the ImageView
      .placeholder(android.R.drawable.ic_menu_report_image)
      .into(holder.image)

    if (isMyRecipes) holder.deleteRecipe.visibility = View.VISIBLE
    else holder.deleteRecipe.visibility = View.GONE

    holder.upvoteCount.text = item.second.upvotes.size.toString()
    Log.d("oofoof", "masuk ${item.second.title} ${item.second.replyCount}")
    holder.replyCount.text = item.second.replyCount.toString()
    holder.bookmarkCount.text = item.second.bookmarks.size.toString()

    // USER
    item.first?.let { user ->
      holder.userTitle.text = user.name

      val userUri = Uri.parse(user.image)
      Glide.with(myRecipeFragment.root)
        .load(userUri)   // Load the image URL into the ImageView
        .placeholder(R.drawable.ic_bnv_profile)
        .into(holder.userProfile)
    }

  }

  override fun getItemCount(): Int = recipes.size

  inner class ViewHolder(binding: ItemRecipeBigImageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val title = binding.tvTitle
    val date = binding.tvDate
    val image = binding.ivImage
    val recipeItem = binding.cvRecipe
    val deleteRecipe = binding.btnDelete
    val userProfile = binding.ivUserProfile
    val userTitle = binding.tvUsername
    val upvoteCount = binding.tvUpvoteCount
    val replyCount = binding.tvReplyCount
    val bookmarkCount = binding.tvBookmarkCount
  }

  fun updateData(newItems: List<Pair<User?, Recipe>>) {
    recipes = newItems // Make sure to update the recipes property
    if (newItems.isEmpty()) {
      notifyDataSetChanged()
    } else if (newItems.size != recipes.size) {
      notifyDataSetChanged()
    } else {
      notifyItemRangeChanged(0, newItems.size)
    }
  }

  fun removeData(recipeId: String) {
    // Find the index of the item with the given recipeId
    val indexToRemove = recipes.indexOfFirst { it.second.id == recipeId }

    // If the item exists in the list, remove it and notify the adapter
    if (indexToRemove != -1) {
      // Convert the list to mutable so we can remove the item
      val updatedRecipes = recipes.toMutableList()
      updatedRecipes.removeAt(indexToRemove)

      // Update the list and notify the adapter
      recipes = updatedRecipes
      notifyItemRemoved(indexToRemove)
    }
  }

  fun isMyRecipes() {
    isMyRecipes = true
  }

  fun setItemListener(recipeListListener: RecipeListListener) {
    this.recipeListListener = recipeListListener
  }

}