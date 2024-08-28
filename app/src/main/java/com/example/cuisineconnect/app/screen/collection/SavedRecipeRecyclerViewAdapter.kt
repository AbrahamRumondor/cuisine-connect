package com.example.cuisineconnect.app.screen.collection

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.cuisineconnect.R

import com.example.cuisineconnect.app.screen.collection.placeholder.PlaceholderContent.PlaceholderItem
import com.example.cuisineconnect.databinding.FragmentSavedRecipeBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class SavedRecipeRecyclerViewAdapter(
  private val values: List<PlaceholderItem>
) : RecyclerView.Adapter<SavedRecipeRecyclerViewAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    return ViewHolder(
      FragmentSavedRecipeBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )

  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = values[position]
    holder.idView.text = item.id
    holder.contentView.text = item.content
  }

  override fun getItemCount(): Int = values.size

  inner class ViewHolder(binding: FragmentSavedRecipeBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val idView: TextView = binding.itemNumber
    val contentView: TextView = binding.content

    override fun toString(): String {
      return super.toString() + " '" + contentView.text + "'"
    }
  }

}