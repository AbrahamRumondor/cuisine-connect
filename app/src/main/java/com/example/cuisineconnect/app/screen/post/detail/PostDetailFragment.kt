package com.example.cuisineconnect.app.screen.post.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.screen.post.PostContentRenderer
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.databinding.FragmentPostDetailBinding
import com.example.cuisineconnect.domain.model.Post
import com.example.cuisineconnect.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    // Initialize the PostContentRenderer
    postContentRenderer = PostContentRenderer(binding.llPostContents, inflater, postDetailViewModel,
      findNavController())
    setupContentRendererListener()

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

  private fun setupContentRendererListener() {
//    postContentRenderer.setItemListener(object : UserClickListener {
//      override fun onUserClicked(userId: String) {
//        if (userId == currentUser?.id) {
//          val action =
//            PostDetailFragmentDirections.actionPostDetailFragment2ToProfileFragment()
//          findNavController().navigate(action)
//          return
//        } else {
//          val action =
//            PostDetailFragmentDirections.actionPostDetailFragment2ToOtherProfileFragment(
//              userId
//            )
//          findNavController().navigate(action)
//        }
//      }
//    })
  }

  private fun loadPostDetails(postId: String) {
    // Fetch the post by ID along with the user who created it
    postDetailViewModel.getPostById(postId) { result ->
      result?.let { (user, post) ->
        binding.run {
//          TODO postDetailViewModel.updateTrendingCounter(
//            post.hashtags,
//            post.id
//          )

          tvUpvoteCount.text = post.upvotes.size.toString()
          tvReplyCount.text = post.replyCount.toString()
          // Render the post content
          postContentRenderer.renderPostContent(post.postContent)

          tvUsername.text = user.name
          tvDate.text = getRelativeTime(post.date)

          currentUser?.let {
            setupUpvoteButton(post, it)
//
//            if (post.id.contains(it.id)) {
//              ivIcBookmark.visibility = View.GONE
//              tvBookmarkCount.visibility = View.GONE
//            } else {
//              ivIcBookmark.visibility = View.VISIBLE
//              tvBookmarkCount.visibility = View.VISIBLE
//              tvBookmarkCount.text = post.bookmarks.size.toString()
//            }

          }

          Glide.with(binding.root)
            .load(user.image)   // Load the image URL into the ImageView
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(ivUserProfile)

          cvProfile.setOnClickListener {
            goToProfile(user.id)
          }

          tvUsername.setOnClickListener {
            goToProfile(user.id)
          }
        }
      } ?: run {
        // Handle the case where either post or user is null
      }
    }
  }

  private fun goToProfile(userId: String) {
    if (userId == currentUser?.id) {
      val action =
        PostDetailFragmentDirections.actionPostDetailFragment2ToProfileFragment()
      findNavController().navigate(action)
      return
    } else {
      val action =
        PostDetailFragmentDirections.actionPostDetailFragment2ToOtherProfileFragment(
          userId
        )
      findNavController().navigate(action)
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
    val isBookmarked = post.bookmarks[user.id] == true

    updateUpvoteIcon(isUpvoted)
//    updateBookmarkIcon(isBookmarked)

    binding.llUpvote.setOnClickListener {
      if (isUpvoted) {
        handleDownvote(post.id, user.id)
      } else {
        handleUpvote(post.id, user.id)
      }
    }

//    binding.llBookmark.setOnClickListener {
//      if (isBookmarked) {
//        handleRemoveBookmark(post.id, user.id)
//      } else {
//        handleAddBookmark(post.id, user.id)
//      }
//    }
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

//  private fun updateBookmarkIcon(isBookmarked: Boolean) {
//    binding.ivIcBookmark.setImageResource(
//      if (isBookmarked) R.drawable.ic_bookmark_solid else R.drawable.ic_bookmark_regular
//    )
//  }

  private fun handleAddBookmark(postId: String, userId: String) {
    postDetailViewModel.addToBookmark(postId, userId) {
      loadPostDetails(postId) // Refresh post details to update the bookmark state
      showToast("Added to bookmarks")
    }
  }

  private fun handleRemoveBookmark(postId: String, userId: String) {
    postDetailViewModel.removeFromBookmark(postId, userId) {
      loadPostDetails(postId) // Refresh post details to update the bookmark state
      showToast("Removed from bookmarks")
    }
  }

  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
      binding.inclInternet.root.visibility = View.VISIBLE
      binding.cvBottomDetail.visibility = View.GONE
      binding.scrollableContent.visibility = View.GONE
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
      binding.inclInternet.root.visibility = View.GONE
      binding.cvBottomDetail.visibility = View.VISIBLE
      binding.scrollableContent.visibility = View.VISIBLE
    }
  }
}