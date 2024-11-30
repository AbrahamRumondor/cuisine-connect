// PostContentRenderer.kt
package com.example.cuisineconnect.app.screen.post

import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.UserClickListener
import com.example.cuisineconnect.app.screen.post.detail.PostDetailFragmentDirections
import com.example.cuisineconnect.app.screen.post.detail.PostDetailViewModel
import com.example.cuisineconnect.app.util.ClickableTextView
import com.example.cuisineconnect.databinding.ItemPostRecipeBinding

class PostContentRenderer(
  private val container: LinearLayout,
  private val inflater: LayoutInflater,
  private val viewModel: PostDetailViewModel?,
  private val findNavController: NavController
) {

  private var userClickListener: UserClickListener? = null

  fun setItemListener(userClickListener: UserClickListener) {
    this.userClickListener = userClickListener
  }

  fun renderPostContent(postContent: MutableList<MutableMap<String, String>>) {
    // Clear any existing views in the container to avoid duplicates
    container.removeAllViews()

    // Make a copy of postContent if needed, or directly use postContent
    val currentPostContent = postContent.toList()

    // First, inflate and display a loading layout for each item
    currentPostContent.forEach { _ ->
      val loadingView = LayoutInflater.from(container.context)
        .inflate(R.layout.item_loading_layout, container, false)
      container.addView(loadingView)
    }

    for (item in currentPostContent) {
      Log.d("lolololo", "Processing: $item")
      val type = item["type"] ?: ""
      when {
        type.contains("text") -> addText(
          item["value"] ?: "",
          type.substringBefore("_").toInt()
        )

        type.contains("image") -> addImage(
          item["value"] ?: "",
          type.substringBefore("_").toInt()
        )

        type.contains("recipe") -> addRecipe(
          item["value"] ?: "",
          type.substringBefore("_").toInt(),
          viewModel
        )
      }
    }
  }

  private fun addText(text: String, order: Int?) {
    val customCardView = inflater.inflate(R.layout.item_post_text_view, container, false)

    // Set margins using the new function
    setViewMargins(customCardView, 0, 8)

    val tvUserInput: ClickableTextView = customCardView.findViewById(R.id.tvUserInput)
    tvUserInput.setHashtagText(text) { hashtag ->
      // Handle hashtag click, e.g., navigate to a new screen or perform a search
      navigateToHashtag(hashtag)
    }

    if (order != null && order <= container.childCount) {
      container.removeViewAt(order)
      container.addView(customCardView, order)
    } else {
      container.addView(customCardView, container.childCount)
    }
  }

  // Example function for handling hashtag click
  private fun navigateToHashtag(hashtag: String) {
    // Use navController to navigate
    val action = PostDetailFragmentDirections.actionPostDetailFragment2ToSearchResultFragment(hashtag)
    findNavController.navigate(action)
  }

  private fun addImage(imageUri: String, order: Int?) {
    val customImageView = inflater.inflate(R.layout.item_post_image_view, container, false)

    // Set margins using the new function
    setViewMargins(customImageView, 4, 8)

    val imageView: ImageView = customImageView.findViewById(R.id.iv_image)
    Glide.with(container).load(imageUri).placeholder(R.drawable.loading_image).into(imageView)

    // Set click listener to open the image in full screen
    imageView.setOnClickListener {
      // Create and display a dialog to show the full image
      val dialog = Dialog(container.context)
      dialog.setContentView(R.layout.dialog_full_image)

      val fullImageView = dialog.findViewById<ImageView>(R.id.iv_full_image)

      Glide.with(container).load(imageUri).placeholder(R.drawable.loading_image).into(fullImageView)

      // Set dialog layout parameters to fill 90-95% of the screen
      val params = dialog.window?.attributes
      params?.width =
        (container.context.resources.displayMetrics.widthPixels * 0.80).toInt() // 95% of screen width
      params?.height =
        (container.context.resources.displayMetrics.heightPixels * 0.80).toInt() // 95% of screen height
      dialog.window?.attributes = params

      val closeView = dialog.findViewById<ImageButton>(R.id.btn_close)
      closeView.setOnClickListener {
        dialog.dismiss()
      }

      dialog.show()
    }

    if (order != null && order <= container.childCount) {
      container.removeViewAt(order)
      container.addView(customImageView, order)
    } else {
      container.addView(customImageView, container.childCount)
    }
  }

  private fun addRecipe(
    recipeId: String,
    order: Int?,
    viewModel: PostDetailViewModel?,
  ) {
    Log.d("borborboror", "masukk: ${viewModel}")

    viewModel?.getRecipeById(recipeId) { pair ->
      if (pair == null) {
        val customCardView = LayoutInflater.from(container.context)
          .inflate(R.layout.item_failed_layout, container, false)

        if (order != null && order <= container.childCount) {
          container.removeViewAt(order)
          container.addView(customCardView, order)
        } else {
          container.addView(customCardView, container.childCount)
        }
        return@getRecipeById
      }

      val (user, recipe) = pair

      if (recipe.id == recipeId) {
        val viewRecipe = ItemPostRecipeBinding.inflate(
          LayoutInflater.from(container.context),
          container,
          false
        )

        // Set margins using the new function
        setViewMargins(viewRecipe.root, 4, 4)

        viewRecipe.run {
          tvTitle.text = recipe.title
          tvUpvoteCount.text = recipe.upvotes.size.toString()
          tvReplyCount.text = recipe.replyCount.toString()
          tvBookmarkCount.text = recipe.bookmarks.size.toString()

          Glide.with(container).load(recipe.image).placeholder(R.drawable.recipe_img)
            .into(ivImage)

          user.let {
            tvUsername.text = it.displayName
            Glide.with(container)
              .load(it.image)
              .placeholder(R.drawable.ic_bnv_profile)
              .into(ivUserProfile)
          }
        }

        // Set click listener to navigate to recipe details
        viewRecipe.root.setOnClickListener {
          navigateToDetailRecipe(recipeId)
        }

        if (order != null && order <= container.childCount) {
          container.removeViewAt(order)
          container.addView(viewRecipe.root, order)
        } else {
          container.addView(viewRecipe.root, container.childCount)
        }
      }
    }
  }

  private fun navigateToDetailRecipe(recipeId: String) {
    val action = PostDetailFragmentDirections.actionPostDetailFragment2ToRecipeDetailFragment(recipeId)
    findNavController.navigate(action)
  }
  private fun setViewMargins(
    view: android.view.View,
    horizontalMarginDp: Int,
    verticalMarginDp: Int
  ) {
    val density = view.context.resources.displayMetrics.density
    val horizontalMarginInPx = (horizontalMarginDp * density).toInt()
    val verticalMarginInPx = (verticalMarginDp * density).toInt()

    val layoutParams = view.layoutParams as LinearLayout.LayoutParams
    layoutParams.setMargins(
      horizontalMarginInPx,
      verticalMarginInPx,
      horizontalMarginInPx,
      verticalMarginInPx
    )
    view.layoutParams = layoutParams
  }

}