package com.example.cuisineconnect.app.screen.collection.bookmark

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.databinding.FragmentMyRecipeListBinding
import com.example.cuisineconnect.databinding.ItemRecipeBigImageBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat

class BookmarkRecipeRecyclerViewAdapter(
  private val bookmarkRecipeFragmentBinding: FragmentMyRecipeListBinding
) : RecyclerView.Adapter<BookmarkRecipeRecyclerViewAdapter.ViewHolder>() {

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
    val app = bookmarkRecipeFragmentBinding.root.context

    _isPopulated.value = true // Set the value to true once the item is bound
    val item = recipes[position]
    holder.title.text = item.second.title

    val dateFormat = SimpleDateFormat("MMM dd")
    val formattedDate = dateFormat.format(item.second.date)
    holder.date.text = formattedDate

    holder.recipeItem.setOnClickListener {
      recipeListListener?.onRecipeClicked(item.second.id)
    }

    holder.recipeItem.setOnLongClickListener {
      val builder = AlertDialog.Builder(bookmarkRecipeFragmentBinding.root.context)
      builder.setTitle(app.getString(R.string.choose_this_recipe))
      builder.setPositiveButton(app.getString(R.string.yes)) { dialog, _ ->
        Toast.makeText(
          bookmarkRecipeFragmentBinding.root.context,
          "${item.second.title} ${app.getString(R.string.recipe_chosen)}",
          Toast.LENGTH_SHORT
        ).show()
        recipeListListener?.onRecipeLongClicked(item.second.id)
        dialog.dismiss()
      }
      builder.setNegativeButton(app.getString(R.string.no)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
      true
    }

    holder.deleteRecipe.setOnClickListener {
      val builder = AlertDialog.Builder(bookmarkRecipeFragmentBinding.root.context)
      builder.setTitle(app.getString(R.string.delete_recipe))
      builder.setMessage(app.getString(R.string.are_you_sure_you_want_to_delete_the_recipe))
      builder.setPositiveButton(app.getString(R.string.yes)) { dialog, _ ->
        Toast.makeText(
          bookmarkRecipeFragmentBinding.root.context,
          "${item.second.title} ${app.getString(R.string.recipe_deleted)}",
          Toast.LENGTH_SHORT
        ).show()
        recipeListListener?.onItemDeleteClicked(item.second.id, "recipe")
        dialog.dismiss()
      }
      builder.setNegativeButton(app.getString(R.string.no)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }

    val uri = Uri.parse(item.second.image)
    Glide.with(bookmarkRecipeFragmentBinding.root)
      .load(uri)
      .placeholder(android.R.drawable.ic_menu_report_image)
      .into(holder.image)

    if (isMyRecipes) holder.deleteRecipe.visibility = View.VISIBLE
    else holder.deleteRecipe.visibility = View.GONE

    holder.upvoteCount.text = item.second.upvotes.size.toString()
    Log.d("BookmarkAdapter", "Loaded ${item.second.title} with replies ${item.second.replyCount}")
    holder.replyCount.text = item.second.replyCount.toString()
    holder.bookmarkCount.text = item.second.bookmarks.size.toString()

    // USER
    item.first?.let { user ->
      holder.userTitle.text = user.displayName
      val userUri = Uri.parse(user.image)
      Glide.with(bookmarkRecipeFragmentBinding.root)
        .load(userUri)
        .placeholder(R.drawable.ic_bnv_profile)
        .into(holder.userProfile)
      holder.userProfile.setOnClickListener {
        recipeListListener?.onUserProfileClicked(user.id)
      }
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
    recipes = newItems

    if (newItems.isEmpty()) {
      // Notify adapter that data is cleared
      notifyDataSetChanged()
    } else if (newItems.size != recipes.size) {
      // If the new list size is different, refresh the entire list
      notifyDataSetChanged()
    } else {
      // If only items are modified but count is the same, update them in range
      notifyItemRangeChanged(0, newItems.size)
    }
  }

  fun removeData(recipeId: String) {
    val indexToRemove = recipes.indexOfFirst { it.second.id == recipeId }
    if (indexToRemove != -1) {
      val updatedRecipes = recipes.toMutableList()
      updatedRecipes.removeAt(indexToRemove)
      recipes = updatedRecipes
      notifyItemRemoved(indexToRemove)
    }
  }

  fun setItemListener(recipeListListener: RecipeListListener) {
    this.recipeListListener = recipeListListener
  }
}