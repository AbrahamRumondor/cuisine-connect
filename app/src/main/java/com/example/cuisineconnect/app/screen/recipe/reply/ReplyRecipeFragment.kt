package com.example.cuisineconnect.app.screen.recipe.reply

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.databinding.FragmentReplyRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReplyRecipeFragment : Fragment() {

  private lateinit var binding: FragmentReplyRecipeBinding

  val args: ReplyRecipeFragmentArgs by navArgs()

  private val replyRecipeViewModel: ReplyRecipeViewModel by viewModels()
  private val recipeReplyAdapter by lazy { RecipeReplyAdapter() }

  private var rootReply = Triple(true, "", 0)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentReplyRecipeBinding.inflate(inflater, container, false)

    setupToolbar()

    val recipeId = args.recipeId
    recipeId?.let {
      replyRecipeViewModel.getRepliesByRecipe(it)
      populateRepliesAdapter(it)
      setupSendButton(it)
    }

    return binding.root
  }

  private fun setupSendButton(recipeId: String) {
    binding.run {
      flBtnSend.setOnClickListener {
        val inputText = etInputReply.text.toString().trim()

        if (inputText.isNotEmpty()) {
          lifecycleScope.launch {
            replyRecipeViewModel.user.collectLatest { user ->
              replyRecipeViewModel.setReply(
                recipeId,
                replyRecipeViewModel.createReplyResponse(
                  repliesId = rootReply.second,
                  body = inputText,
                  userId = user.id,
                  recipeId = recipeId,
                )
              ) {
                replyRecipeViewModel.getRepliesByRecipe(recipeId)
              }
              etInputReply.text.clear()
            }
          }
        }
      }


    }
  }

  private fun setupToolbar() {
    (activity as? AppCompatActivity)?.apply {
      setSupportActionBar(binding.toolbar)
      supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true) // Enable the back button
        setDisplayShowHomeEnabled(true)
        title = "Create Recipe" // Set title for the toolbar
      }
    }

    // Handle back button click
    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp() // This uses Navigation Component to go back
    }
  }

  private fun populateRepliesAdapter(recipeId: String) {
    lifecycleScope.launch {
      // Combine the recipe, user, and steps into a single flow
      replyRecipeViewModel.replies.collectLatest { replies ->

        Log.d("brobruh", "replyRecipeAdapter = ${replies.toString()}")
        if (binding.rvReplies.adapter == null) {
          binding.rvReplies.adapter = recipeReplyAdapter
        }
        recipeReplyAdapter.submitReplies(
          // sort by latest date
          replies.sortedByDescending { it.second.date }.toMutableList()
        )
      }
    }
    setRecipeReplyAdapterButtons(recipeId)
  }

  private fun setRecipeReplyAdapterButtons(recipeId: String) {
    recipeReplyAdapter.setItemListener(object : RecipeReplyItemListener {
      override fun onProfilePictureClicked(userId: String) {
//        TODO("Not yet implemented")
      }

      override fun onUpvoteClicked(position: Int, replyId: String) {
        // TODO WRONG LOGIC, UPVOTE MUST USE MAP
        replyRecipeViewModel.upvoteReply(recipeId, replyId)
      }

      override fun onReplyInputClicked(position: Int, targetReplyId: String) {
        rootReply = Triple(false, targetReplyId, position)
      }

      override fun onReplyListClicked(position: Int, replyId: String, repliesId: List<String>) {
        replyRecipeViewModel.getReplyById(recipeId, replyId, repliesId)
      }
    })
  }


}