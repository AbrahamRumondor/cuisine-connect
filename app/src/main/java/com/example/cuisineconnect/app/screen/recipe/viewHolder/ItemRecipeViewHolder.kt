package com.example.cuisineconnect.app.screen.recipe.viewHolder

import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.ItemListListener
import com.example.cuisineconnect.app.screen.create.CreatePostViewModel
import com.example.cuisineconnect.databinding.ItemRecipeBigImageBinding
import com.example.cuisineconnect.domain.model.Recipe
import com.example.cuisineconnect.domain.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ItemRecipeViewHolder(
  private val view: ItemRecipeBigImageBinding,
//    private val itemListener: OrderSummaryItemListener?,
) : RecyclerView.ViewHolder(view.root) {

  val app = view.root.context

  fun bind(
    user: User?,
    recipe: Recipe?,
    listener: ItemListListener?,
    createPostViewModel: CreatePostViewModel?,
    fromHomePage: Boolean = false,
    alreadyDisplayed: () -> Unit
  ) {
    view.run {
      if (user != null && recipe != null) {

        Log.d("lilil", "this is recipe ${user} and ${recipe}")

        tvUsername.text = user.displayName

        Glide.with(root)
          .load(user.image)
          .placeholder(R.drawable.ic_bnv_profile)
          .into(ivUserProfile)

        cvProfile.setOnClickListener {
          listener?.onUserProfileClicked(user.id)
        }

        tvTitle.text = recipe.title
//        tvDesc.text = recipe.description

        val formattedDate = getRelativeTime(recipe.date) // recipe.date is of type Date
        tvDate.text = formattedDate

        cvRecipe.setOnClickListener {
          listener?.onRecipeClicked(recipe.id)
        }

        cvRecipe.setOnLongClickListener {
          val builder = AlertDialog.Builder(view.root.context)
          builder.setTitle(app.getString(R.string.choose_recipe_title))
          builder.setPositiveButton(app.getString(R.string.yes)) { dialog, _ ->
            Toast.makeText(
              view.root.context,
              app.getString(R.string.this_recipe_is_chosen),
              Toast.LENGTH_SHORT
            ).show()
            listener?.onRecipeLongClicked(recipe.id)
            dialog.dismiss()
          }
          builder.setNegativeButton(app.getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
          }
          builder.create().show()
          true
        }

        val isAuthor = createPostViewModel?.user?.value?.id == user.id
        val isNotFollowing = createPostViewModel?.user?.value?.following?.none { it == user.id } ?: true

        if (isNotFollowing && !isAuthor && fromHomePage) {
          llRecommendation.visibility = View.VISIBLE
        } else {
          llRecommendation.visibility = View.GONE
        }

        btnDelete.visibility = View.GONE
        if (isAuthor) {
          btnDelete.visibility = View.VISIBLE
          btnDelete.setOnClickListener { view ->
            // Create a PopupMenu
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.item_options, popupMenu.menu)

            // Set click listeners for menu items
            popupMenu.setOnMenuItemClickListener { menuItem ->
              when (menuItem.itemId) {
                R.id.action_delete -> {
                  // Show delete confirmation dialog
                  showDeleteConfirmationDialog(recipe, view, listener)
                  true
                }
                else -> false
              }
            }

            // Show the PopupMenu
            popupMenu.show()
          }
        }
        val uri = Uri.parse(recipe.image)
        Glide.with(view.root)
          .load(uri)
          .placeholder(R.drawable.recipe_img)
          .into(ivImage)

//        if (isMyRecipes) holder.editRecipe.visibility = View.VISIBLE
//        else holder.editRecipe.visibility = View.GONE

        tvUpvoteCount.text = recipe.upvotes.size.toString()
        tvReplyCount.text = recipe.replyCount.toString()
        tvBookmarkCount.text = recipe.bookmarks.size.toString()

//                ivOrderAdd.setOnClickListener {
//                    itemListener?.onAddItemClicked(position, menu.id)
//                }
      }
    }

    alreadyDisplayed()
  }

  private fun showDeleteConfirmationDialog(recipe: Recipe, view: View, listener: ItemListListener?) {
    val builder = AlertDialog.Builder(view.context)
    builder.setTitle(app.getString(R.string.delete_confirmation_title))
    builder.setMessage(app.getString(R.string.delete_confirmation_message))

    builder.setPositiveButton(app.getString(R.string.yes)) { dialog, _ ->
      Toast.makeText(
        view.context,
        "${recipe.title}${app.getString(R.string.recipe_deleted)}",
        Toast.LENGTH_SHORT
      ).show()

      listener?.onItemDeleteClicked(recipe.id, "recipe")
      dialog.dismiss() // Dismiss the dialog
    }

    builder.setNegativeButton(app.getString(R.string.no)) { dialog, _ ->
      dialog.dismiss()
    }

    builder.create().show()
  }

  private fun getRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val timestamp = date.time
    val diff = now - timestamp

    return when {
      // Less than 1 minute ago
      diff < TimeUnit.MINUTES.toMillis(1) -> {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds <= 1) "$seconds ${view.root.context.getString(R.string.second_ago)}" else "$seconds ${view.root.context.getString(R.string.seconds_ago)}"
      }

      // Less than 1 hour ago
      diff < TimeUnit.HOURS.toMillis(1) -> {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (minutes == 1L) "$minutes ${view.root.context.getString(R.string.minute_ago)}" else "$minutes ${view.root.context.getString(R.string.minutes_ago)}"
      }

      // Less than 1 day ago
      diff < TimeUnit.HOURS.toMillis(24) -> {
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        if (hours == 1L) "$hours ${view.root.context.getString(R.string.hour_ago)}" else "$hours ${view.root.context.getString(R.string.hours_ago)}"
      }

      // Less than 5 days ago
      diff < TimeUnit.DAYS.toMillis(5) -> {
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        if (days == 1L) "$days ${view.root.context.getString(R.string.day_ago)}" else "$days ${view.root.context.getString(R.string.days_ago)}"
      }

      // Default to the "MMM dd" format for anything older
      else -> {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateFormat.format(date)
      }
    }
  }
}