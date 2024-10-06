package com.example.cuisineconnect.app.screen.profile.post.viewHolder

import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.databinding.ItemPostHorizontalBinding
import com.example.cuisineconnect.databinding.ItemPostRecipeBinding
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.User
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat

class ItemPostViewHolder(
  private val view: ItemPostHorizontalBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(
    user: User?,
    post: Post,
    listener: RecipeListListener?,
    createPostViewModel: CreatePostViewModel?
  ) {
    view.run {
      if (user != null) {

        Log.d("lilil", "this is post ${user} and ${post}")

        tvUsername.text = user.name

        Glide.with(root)
          .load(user.image)
          .placeholder(android.R.drawable.ic_menu_report_image)
          .into(ivUserProfile)

        val dateFormat = SimpleDateFormat("MMM dd")
        val formattedDate = dateFormat.format(post.date)
        tvDate.text = formattedDate

        btnEdit.setOnClickListener {
          Toast.makeText(
            view.root.context,
            "${post.date} edited",
            Toast.LENGTH_SHORT
          ).show()
        }

        restorePostContent(
          createPostViewModel, post.postContent
        )

        tvUpvoteCount.text = post.upvotes.size.toString()
        tvReplyCount.text = post.replyCount.toString()
        tvBookmarkCount.text = post.bookmarkCount.toString()

//                ivOrderAdd.setOnClickListener {
//                    itemListener?.onAddItemClicked(position, menu.id)
//                }
      }
    }
  }

  private fun restorePostContent(
    createPostViewModel: CreatePostViewModel?,
    postContent: MutableList<MutableMap<String, String>> = mutableListOf()
  ) {
    view.llPostContents.removeAllViews()

    // Create a copy of the postContent to avoid ConcurrentModificationException
    val currentPostContent = postContent.toList()

    for (item in currentPostContent) {
      val customCardView = LayoutInflater.from(view.root.context)
        .inflate(R.layout.item_post_edit_text_input, view.llPostContents, false)
      view.llPostContents.addView(customCardView)
    }

    // Sequentially process each item
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
          createPostViewModel
        )
      }
    }
  }

  private fun addText(text: String, order: Int?) {
    val customCardView = LayoutInflater.from(view.root.context)
      .inflate(R.layout.item_post_edit_text_input, view.llPostContents, false)

    // Set margins using the new function
    setViewMargins(customCardView, 0, 8)

    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    etUserInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    etUserInput.setText(text)

    if (order != null && order <= view.llPostContents.childCount) {
      view.llPostContents.removeViewAt(order)
      view.llPostContents.addView(customCardView, order)
    } else {
      view.llPostContents.addView(customCardView, view.llPostContents.childCount)
    }
  }

  private fun addImage(imageUri: String, order: Int?) {
    val customImageView = LayoutInflater.from(view.root.context)
      .inflate(R.layout.item_post_image_view, view.llPostContents, false)

    // Set margins using the new function
    setViewMargins(customImageView, 0, 8)

    val imageView: ImageView = customImageView.findViewById(R.id.iv_image)
    Glide.with(view.root).load(imageUri).into(imageView)

    if (order != null && order <= view.llPostContents.childCount) {
      view.llPostContents.removeViewAt(order)
      view.llPostContents.addView(customImageView, order)
    } else {
      view.llPostContents.addView(customImageView, view.llPostContents.childCount)
    }
  }

  private fun addRecipe(
    recipeId: String,
    order: Int?,
    createPostViewModel: CreatePostViewModel?,
  ) {
    createPostViewModel?.getRecipeById(recipeId) { pair ->
      val (user, recipe) = pair

      if (recipe.id == recipeId) {
        val viewRecipe = ItemPostRecipeBinding.inflate(
          LayoutInflater.from(view.root.context),
          view.llPostContents,
          false
        )

        // Set margins using the new function
        setViewMargins(viewRecipe.root, 4, 8)

        viewRecipe.run {
          tvTitle.text = recipe.title
          tvDesc.text = recipe.description
          tvUpvoteCount.text = recipe.upvotes.size.toString()
          tvReplyCount.text = recipe.replyCount.toString()
          tvBookmarkCount.text = recipe.bookmarkCount.toString()

          val dateFormat = SimpleDateFormat("MMM dd")
          val formattedDate = dateFormat.format(recipe.date)
          tvDate.text = formattedDate

          Glide.with(view.root).load(recipe.image).into(ivImageTitle)

          user.let {
            tvUsername.text = it.name
            Glide.with(view.root).load(it.image).into(ivUserProfile)
          }
        }

        if (order != null && order <= view.llPostContents.childCount) {
          view.llPostContents.removeViewAt(order)
          view.llPostContents.addView(viewRecipe.root, order)
        } else {
          view.llPostContents.addView(viewRecipe.root, view.llPostContents.childCount)
        }
      }
    }
  }

  private fun setViewMargins(view: android.view.View, horizontalMarginDp: Int, verticalMarginDp: Int) {
    val density = view.context.resources.displayMetrics.density
    val horizontalMarginInPx = (horizontalMarginDp * density).toInt()
    val verticalMarginInPx = (verticalMarginDp * density).toInt()

    val layoutParams = view.layoutParams as LinearLayout.LayoutParams
    layoutParams.setMargins(horizontalMarginInPx, verticalMarginInPx, horizontalMarginInPx, verticalMarginInPx)
    view.layoutParams = layoutParams
  }

}