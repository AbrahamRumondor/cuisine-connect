package com.example.cuisineconnect.app.screen.profile.post.viewHolder

import android.app.AlertDialog
import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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

        val formattedDate = getRelativeTime(post.date) // recipe.date is of type Date
        tvDate.text = formattedDate
        tvDate.text = formattedDate

        val isAuthor = createPostViewModel?.user?.value?.id == user.id
        btnEdit.visibility = View.INVISIBLE
        if (isAuthor) {
          btnEdit.visibility = View.VISIBLE
          btnEdit.setOnClickListener {
            val builder = AlertDialog.Builder(view.root.context)
            builder.setTitle("Delete Recipe")
            builder.setMessage("Are you sure you want to delete the recipe?")

            builder.setPositiveButton("Yes") { dialog, _ ->
              Toast.makeText(
                view.root.context,
                "post deleted",
                Toast.LENGTH_SHORT
              ).show()

              listener?.onItemDeleteClicked(post.id, "post")
              dialog.dismiss() // Dismiss the dialog
            }

            builder.setNegativeButton("No") { dialog, _ ->
              dialog.dismiss()
            }
            builder.show()
          }
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
        .inflate(R.layout.item_loading_layout, view.llPostContents, false)
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
      .inflate(R.layout.item_post_text_view, view.llPostContents, false)

    // Set margins using the new function
    setViewMargins(customCardView, 0, 8)

    val tvUserInput: TextView = customCardView.findViewById(R.id.tvUserInput)
    tvUserInput.text = text

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
    Glide.with(view.root).load(imageUri).placeholder(R.drawable.loading_image).into(imageView)

    // Set click listener to open the image in full screen
    imageView.setOnClickListener {
      // Create and display a dialog to show the full image
      val dialog = Dialog(view.root.context)
      dialog.setContentView(R.layout.dialog_full_image)

      val fullImageView = dialog.findViewById<ImageView>(R.id.iv_full_image)
      Glide.with(view.root).load(imageUri).placeholder(R.drawable.loading_image).into(fullImageView)

      // Set dialog layout parameters to fill 90-95% of the screen
      val params = dialog.window?.attributes
      params?.width = (view.root.context.resources.displayMetrics.widthPixels * 0.90).toInt() // 95% of screen width
      params?.height = (view.root.context.resources.displayMetrics.heightPixels * 0.90).toInt() // 95% of screen height
      dialog.window?.attributes = params

      dialog.show()
    }

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

    Log.d("borborboror", "masukk: ${createPostViewModel}")

    createPostViewModel?.getRecipeById(recipeId) { pair ->
      if (pair == null) {
        val customCardView = LayoutInflater.from(view.root.context)
          .inflate(R.layout.item_failed_layout, view.llPostContents, false)

        if (order != null && order <= view.llPostContents.childCount) {
          view.llPostContents.removeViewAt(order)
          view.llPostContents.addView(customCardView, order)
        } else {
          view.llPostContents.addView(customCardView, view.llPostContents.childCount)
        }
        return@getRecipeById
      }

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

  private fun getRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val timestamp = date.time
    val diff = now - timestamp

    return when {
      // Less than 1 minute ago
      diff < TimeUnit.MINUTES.toMillis(1) -> {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds <= 1) "$seconds second ago" else "$seconds seconds ago"
      }

      // Less than 1 hour ago
      diff < TimeUnit.HOURS.toMillis(1) -> {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (minutes == 1L) "$minutes minute ago" else "$minutes minutes ago"
      }

      // Less than 1 day ago
      diff < TimeUnit.HOURS.toMillis(24) -> {
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if (hours == 1L) "$hours hour ago" else "$hours hours ago"
      }

      // Less than 5 days ago
      diff < TimeUnit.DAYS.toMillis(5) -> {
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        if (days == 1L) "$days day ago" else "$days days ago"
      }

      // Default to the "MMM dd" format for anything older
      else -> {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateFormat.format(date)
      }
    }
  }
}