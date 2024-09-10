package com.example.cuisineconnect.app.screen.collection

import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.cuisineconnect.app.component.RecipeListListener

import com.example.cuisineconnect.app.screen.collection.placeholder.PlaceholderContent.PlaceholderItem
import com.example.cuisineconnect.databinding.FragmentMyRecipeBinding
import com.example.cuisineconnect.databinding.FragmentMyRecipeListBinding
import com.example.cuisineconnect.databinding.ItemRecipeHorizontalBinding
import com.example.cuisineconnect.domain.model.Recipe
import java.util.Date

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyRecipeRecyclerViewAdapter(
  private val myRecipeFragment: FragmentMyRecipeListBinding
) : RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder>() {

  private var recipes: List<Recipe> = listOf()
  private var recipeListListener: RecipeListListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    return ViewHolder(
      ItemRecipeHorizontalBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )

  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = recipes[position]
    holder.title.text = item.title
    holder.description.text = item.description
    holder.date.text = item.date.toString()
    holder.recipeItem.setOnClickListener {
      Toast.makeText(myRecipeFragment.root.context, "you touched ${item.title} 😳", Toast.LENGTH_SHORT).show()
    }
    holder.editRecipe.setOnClickListener {
      Toast.makeText(myRecipeFragment.root.context, "${item.title} edited", Toast.LENGTH_SHORT).show()
    }
    val uri = Uri.parse(item.image)
    Glide.with(myRecipeFragment.root)
      .load(uri)   // Load the image URL into the ImageView
      .into(holder.image)
  }

  override fun getItemCount(): Int = recipes.size

  inner class ViewHolder(binding: ItemRecipeHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
    val title = binding.tvTitle
    val description = binding.tvDesc
    val date = binding.tvDate
    val image = binding.ivImageTitle
    val recipeItem = binding.clRecipeItem
    val editRecipe = binding.btnEdit
  }

  fun updateData(newItems: List<Recipe>) {
    recipes = newItems
    notifyItemRangeChanged(0, newItems.size)
  }

  fun setItemListener(recipeListListener: RecipeListListener) {
    this.recipeListListener = recipeListListener
  }

}