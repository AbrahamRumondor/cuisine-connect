package com.example.cuisineconnect.app.screen.collection.myRecipe

import android.annotation.SuppressLint
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
import com.example.cuisineconnect.databinding.ItemRecipeHorizontalBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    return ViewHolder(
      ItemRecipeHorizontalBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )

  }

  @SuppressLint("SimpleDateFormat")
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = recipes[position]
    holder.title.text = item.second.title
    holder.description.text = item.second.description

    val dateFormat = SimpleDateFormat("MMM dd")
    val formattedDate = dateFormat.format(item.second.date)

    holder.date.text = formattedDate
    holder.recipeItem.setOnClickListener {
      recipeListListener?.onRecipeClicked(item.second.id)
      Toast.makeText(
        myRecipeFragment.root.context,
        "you touched ${item.second.title} 😳",
        Toast.LENGTH_SHORT
      ).show()
    }
    holder.editRecipe.setOnClickListener {
      Toast.makeText(
        myRecipeFragment.root.context,
        "${item.second.title} edited",
        Toast.LENGTH_SHORT
      ).show()
    }
    val uri = Uri.parse(item.second.image)
    Glide.with(myRecipeFragment.root)
      .load(uri)   // Load the image URL into the ImageView
      .placeholder(android.R.drawable.ic_menu_report_image)
      .into(holder.image)

    if (isMyRecipes) holder.editRecipe.visibility = View.VISIBLE
    else holder.editRecipe.visibility = View.GONE

    holder.upvoteCount.text = item.second.upvotes.size.toString()
    Log.d("oofoof", "masuk ${item.second.title} ${item.second.replyCount}")
    holder.replyCount.text = item.second.replyCount.toString()
    holder.bookmarkCount.text = item.second.bookmarkCount.toString()

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

  inner class ViewHolder(binding: ItemRecipeHorizontalBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val title = binding.tvTitle
    val description = binding.tvDesc
    val date = binding.tvDate
    val image = binding.ivImageTitle
    val recipeItem = binding.clRecipeItem
    val editRecipe = binding.btnEdit
    val userProfile = binding.ivUserProfile
    val userTitle = binding.tvUsername
    val upvoteCount = binding.tvUpvoteCount
    val replyCount = binding.tvReplyCount
    val bookmarkCount = binding.tvBookmarkCount
  }

  fun updateData(newItems: List<Pair<User?, Recipe>>) {
    recipes = newItems
    notifyItemRangeChanged(0, newItems.size)
  }

  fun isMyRecipes() {
    isMyRecipes = true
  }

  fun isNotMyRecipes() {
    isMyRecipes = false
  }

  fun setItemListener(recipeListListener: RecipeListListener) {
    this.recipeListListener = recipeListListener
  }

}