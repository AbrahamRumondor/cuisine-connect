package com.example.cuisineconnect.app.screen.collection

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.example.cuisineconnect.app.screen.collection.placeholder.PlaceholderContent.PlaceholderItem
import com.example.cuisineconnect.databinding.ItemRecipeHorizontalBinding
import com.example.cuisineconnect.domain.model.Recipe
import java.util.Date

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyRecipeRecyclerViewAdapter(
  private val values: List<Recipe>
) : RecyclerView.Adapter<MyRecipeRecyclerViewAdapter.ViewHolder>() {

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
    val item = values[position]
    holder.title.text = item.title
    holder.description.text = item.description
    holder.date.text = item.date.toString()
  }

  override fun getItemCount(): Int = values.size

  inner class ViewHolder(binding: ItemRecipeHorizontalBinding) : RecyclerView.ViewHolder(binding.root) {
    val title = binding.tvTitle
    val description = binding.tvDesc
    val date = binding.tvDate
    val image = binding.ivImageTitle
  }

}