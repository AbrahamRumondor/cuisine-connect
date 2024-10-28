package com.example.cuisineconnect.app.screen.post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.screen.recipe.detail.RecipeDetailFragmentDirections
import com.example.cuisineconnect.databinding.FragmentPostDetailBinding
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

  private val args: PostDetailFragmentArgs by navArgs()
  private lateinit var binding: FragmentPostDetailBinding
  private lateinit var postContentRenderer: PostContentRenderer
  private val postDetailViewModel: PostDetailViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentPostDetailBinding.inflate(inflater, container, false)

    binding.btnBack.setOnClickListener {
      activity?.supportFragmentManager?.popBackStack()
    }

    // Initialize the PostContentRenderer
    postContentRenderer = PostContentRenderer(binding.llPostContents, inflater, postDetailViewModel)

    // Retrieve and display post data
    val postId = args.postId
    loadPostDetails(postId)

    binding.llReply.setOnClickListener {
      val action =
        PostDetailFragmentDirections.actionPostDetailFragment2ToReplyRecipeFragment(
          postId
        )
      findNavController().navigate(action)
    }

    return binding.root
  }

  private fun loadPostDetails(postId: String) {
    // Fetch the post by ID along with the user who created it
    postDetailViewModel.getPostById(postId) { result ->
      result?.let { (user, post) ->
        binding.run {
          tvUpvoteCount.text = post.upvotes.size.toString()
          tvReplyCount.text = post.replyCount.toString()
          // Render the post content
          postContentRenderer.renderPostContent(post.postContent)

          setupUpvoteButton(post, user)

          tvUsername.text = user.name
          tvUniqueUsername.text = "@${user.name}"
          tvDate.text = getRelativeTime(post.date)

          Glide.with(binding.root)
            .load(user.image)   // Load the image URL into the ImageView
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(ivUserProfile)
        }
      } ?: run {
        // Handle the case where either post or user is null
      }
    }
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

  private fun setupUpvoteButton(post: Post, user: User) {
    val isUpvoted = post.upvotes[user.id] == true

    updateUpvoteIcon(isUpvoted)

    binding.llUpvote.setOnClickListener {
      if (isUpvoted) {
        handleDownvote(post.id, user.id)
      } else {
        handleUpvote(post.id, user.id)
      }
    }
  }

  private fun updateUpvoteIcon(isUpvoted: Boolean) {
    binding.ivIcThumbs.setImageResource(
      if (isUpvoted) R.drawable.ic_thumbs_up_solid else R.drawable.ic_thumbs_up_regular
    )
  }

  private fun handleUpvote(postId: String, userId: String) {
    postDetailViewModel.upvotePost(postId, userId) {
      loadPostDetails(postId)
      showToast("Upvoted")
    }
  }

  private fun handleDownvote(postId: String, userId: String) {
    postDetailViewModel.downVotePost(postId, userId) {
      loadPostDetails(postId)
      showToast("Downvoted")
    }
  }

  private fun showToast(message: String) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
  }
}