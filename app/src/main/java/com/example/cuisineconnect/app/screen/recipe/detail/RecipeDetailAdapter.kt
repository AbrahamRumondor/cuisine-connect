package com.example.cuisineconnect.app.screen.recipe.detail

import RecipeDetailTagsViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.app.listener.DetailRecipeItemListener
import com.example.cuisineconnect.app.screen.recipe.detail.viewHolder.RecipeDetailEstimationViewHolder
import com.example.cuisineconnect.app.screen.recipe.detail.viewHolder.RecipeDetailHeaderViewHolder
import com.example.cuisineconnect.app.screen.recipe.detail.viewHolder.RecipeDetailIngredientViewHolder
import com.example.cuisineconnect.app.screen.recipe.detail.viewHolder.RecipeDetailStepViewHolder
import com.example.cuisineconnect.app.screen.recipe.detail.viewHolder.RecipeDetailSubTitleViewHolder
import com.example.cuisineconnect.app.screen.recipe.detail.viewHolder.RecipeDetailUserViewHolder
import com.example.cuisineconnect.app.screen.recipe.detail.viewHolder.ViewBottomSpacingViewHolder
import com.example.cuisineconnect.databinding.RecipeDetailEstimationBinding
import com.example.cuisineconnect.databinding.RecipeDetailHeaderBinding
import com.example.cuisineconnect.databinding.RecipeDetailIngredientBinding
import com.example.cuisineconnect.databinding.RecipeDetailStepBinding
import com.example.cuisineconnect.databinding.RecipeDetailTagsBinding
import com.example.cuisineconnect.databinding.SubTitleBinding
import com.example.cuisineconnect.databinding.UserHorizontalBinding
import com.example.cuisineconnect.databinding.ViewBottomSpaceBinding
import com.example.cuisineconnect.domain.model.Hashtag
import com.example.cuisineconnect.domain.model.Ingredient
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User

class RecipeDetailAdapter :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private var items: MutableList<Any?> = mutableListOf()
  private var detailRecipeItemListener: DetailRecipeItemListener? = null

  private val SHOW_HEADER = 0
  private val SHOW_USER = 1
  private val SHOW_ESTIMATION = 2
  private val SHOW_SUB_TITLE = 3
  private val SHOW_INGREDIENT = 4
  private val SHOW_STEP = 5
  private val SHOW_TAGS = 6
  private val SHOW_SPACING = 7

  override fun getItemViewType(position: Int): Int {
    return when (val item = items[position]) {
      is Ingredient? -> SHOW_INGREDIENT
      is Step -> SHOW_STEP
      is User -> SHOW_USER
      is String -> {
        when (item) {
          "Bahan-bahan", "Langkah-langkah" -> SHOW_SUB_TITLE
          else -> SHOW_INGREDIENT
        }
      }

      is Pair<*, *> -> {  // Use Pair<*, *> to allow any type of Pair
        val pair = item as Pair<String, Recipe>
        when (pair.first) {
          "Header" -> SHOW_HEADER
          "Estimation" -> SHOW_ESTIMATION
          else -> throw IllegalArgumentException("Invalid string type")
        }
      }

      is Boolean -> SHOW_SPACING
      is List<*> -> { // Check if the list contains hashtags
        if (item.firstOrNull() is String) SHOW_TAGS else throw IllegalArgumentException("Invalid list type")
      }

      else -> throw IllegalArgumentException("Invalid item type")
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      SHOW_HEADER -> {
        val bindingHeader = RecipeDetailHeaderBinding.inflate(inflater, parent, false)
        RecipeDetailHeaderViewHolder(bindingHeader)
      }

      SHOW_SUB_TITLE -> {
        val bindingSubTitle = SubTitleBinding.inflate(inflater, parent, false)
        RecipeDetailSubTitleViewHolder(bindingSubTitle)
      }

      SHOW_INGREDIENT -> {
        val bindingIngredients = RecipeDetailIngredientBinding.inflate(inflater, parent, false)
        RecipeDetailIngredientViewHolder(bindingIngredients)
      }

      SHOW_STEP -> {
        val bindingSteps = RecipeDetailStepBinding.inflate(inflater, parent, false)
        RecipeDetailStepViewHolder(bindingSteps)
      }

      SHOW_ESTIMATION -> {
        val bindingEstimation = RecipeDetailEstimationBinding.inflate(inflater, parent, false)
        RecipeDetailEstimationViewHolder(bindingEstimation)
      }

      SHOW_USER -> {
        val bindingUser = UserHorizontalBinding.inflate(inflater, parent, false)
        RecipeDetailUserViewHolder(bindingUser)
      }

      SHOW_TAGS -> {
        val bindingTags = RecipeDetailTagsBinding.inflate(inflater, parent, false)
        RecipeDetailTagsViewHolder(bindingTags)
      }

      SHOW_SPACING -> {
        val bindingSpacing = ViewBottomSpaceBinding.inflate(inflater, parent, false)
        ViewBottomSpacingViewHolder(bindingSpacing)
      }

      else -> throw IllegalArgumentException("Invalid view type")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (val item = items[position]) {
      is Step -> (holder as RecipeDetailStepViewHolder).bind(item)
      is String -> {
        when (item) {
          "Bahan-bahan", "Langkah-langkah" -> (holder as RecipeDetailSubTitleViewHolder).bind(item)
          else -> (holder as RecipeDetailIngredientViewHolder).bind(item)
        }
      }

      is User -> (holder as RecipeDetailUserViewHolder).bind(item)
      is Pair<*, *> -> {
        val pair = item as Pair<String, Recipe>
        when (pair.first) {
          "Header" -> (holder as RecipeDetailHeaderViewHolder).bind(pair.second)
          "Estimation" -> (holder as RecipeDetailEstimationViewHolder).bind(pair.second)
          else -> throw IllegalArgumentException("Invalid string type")
        }
      }

      is Boolean -> (holder as ViewBottomSpacingViewHolder).bind()
      is List<*> -> (holder as RecipeDetailTagsViewHolder).bind(item as List<String>)
      else -> throw IllegalArgumentException("Invalid item type")
    }
  }

  override fun getItemCount(): Int = items.size

  fun submitRecipeParts(orderItems: MutableList<Any>) {
    this.items.clear()
    this.items.addAll(orderItems)
    notifyDataSetChanged()  // Call notifyDataSetChanged to refresh the list
  }

  fun setItemListener(detailRecipeItemListener: DetailRecipeItemListener) {
    this.detailRecipeItemListener = detailRecipeItemListener
  }
}