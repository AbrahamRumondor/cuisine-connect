package com.example.cuisineconnect.app.screen.recipe.reply

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.listener.RecipeReplyItemListener
import com.example.cuisineconnect.databinding.FragmentReplyRecipeBinding
import com.example.cuisineconnect.domain.model.Reply
import com.example.cuisineconnect.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    setupToolbar()

    val itemId = args.itemId
    itemId?.let {
      recipeReplyAdapter.submitViewmodel(replyRecipeViewModel)
      replyRecipeViewModel.getRepliesByRecipe(it, null)
      populateRepliesAdapter(it)
      setupSendButton(it)
    }

    return binding.root
  }

  private fun setupSendButton(itemId: String) {
    binding.run {
      flBtnSend.setOnClickListener {
        val inputText = etInputReply.text.toString().trim()

        if (inputText.isNotEmpty()) {
          lifecycleScope.launch {
            replyRecipeViewModel.user.collectLatest { user ->
              replyRecipeViewModel.setReply(
                itemId,
                replyRecipeViewModel.createReplyResponse(
                  repliesId = rootReply.second.ifEmpty { itemId },
                  body = inputText,
                  userId = user.id,
                  itemId = itemId,
                ),
                isNewReply = true
              ) {
                replyRecipeViewModel.getRepliesByRecipe(itemId, "")
              }
              etInputReply.text.clear()
              clReplyOther.visibility = View.GONE
              rootReply = Triple(true, "", 0)
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
        title = "Reply Recipe" // Set title for the toolbar
      }
    }

    // Handle back button click
    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp() // This uses Navigation Component to go back
    }
  }

  private fun populateRepliesAdapter(itemId: String) {
    lifecycleScope.launch {
      // Combine the recipe, user, and steps into a single flow
      replyRecipeViewModel.replies.collectLatest { replies ->

        Log.d("brobruh", "replyRecipeAdapter = ${replies.toString()}")
        if (binding.rvReplies.adapter == null) {
          binding.rvReplies.adapter = recipeReplyAdapter
        }
        if (replies != null) {
          recipeReplyAdapter.submitReplies(replies.toMutableList())
        }
      }
    }
    setRecipeReplyAdapterButtons(itemId)
  }

  private fun setRecipeReplyAdapterButtons(itemId: String) {
    recipeReplyAdapter.setItemListener(object : RecipeReplyItemListener {
      override fun onProfilePictureClicked(userId: String) {
//        TODO("Not yet implemented")
      }

      override fun onUpvoteClicked(position: Int, reply: Reply, userId: String) {
        replyRecipeViewModel.upvoteReply(itemId, reply.id, userId) { replied ->
          recipeReplyAdapter.updateReplyAtPosition(position, replied.copy(isRoot = reply.isRoot))
        }
      }

      override fun onDownVoteClicked(position: Int, reply: Reply, userId: String) {
        replyRecipeViewModel.downVoteReply(itemId, reply.id, userId) { replied ->
          recipeReplyAdapter.updateReplyAtPosition(position, replied.copy(isRoot = reply.isRoot))
        }
      }

      override fun onReplyInputClicked(position: Int, targetReplyId: String, user: User) {
        binding.run {
          val text = "Replying to ${user.displayName}"
          tvReplyOtherUser.text = text
          clReplyOther.visibility = View.VISIBLE

          // Assuming you have an EditText for the reply input
          etInputReply.requestFocus() // Request focus on the input field

          // Show the keyboard
          val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
          imm?.showSoftInput(etInputReply, InputMethodManager.SHOW_IMPLICIT)

          btnClose.setOnClickListener {
            clReplyOther.visibility = View.GONE
            rootReply = Triple(true, "", 0)
          }
        }
        rootReply = Triple(false, targetReplyId, position)
      }

      override fun onReplyListClicked(position: Int, replyId: String, repliesId: List<String>) {
        replyRecipeViewModel.getRepliesByRecipe(itemId, replyId)
      }

      override fun onReplyListSecondClicked(
        position: Int,
        replyId: String,
        repliesId: List<String>
      ) {
        replyRecipeViewModel.openedReply.removeIf { it == replyId }
        replyRecipeViewModel.getRepliesByRecipe(itemId, "")
      }
    })
  }

  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
      binding.inclInternet.root.visibility = View.VISIBLE
      binding.cvToolbar.visibility = View.GONE
      binding.rvReplies.visibility = View.GONE
      binding.clReplyOther.visibility = View.GONE
      binding.vDivider.visibility = View.GONE
      binding.clInputReply.visibility = View.GONE
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
      binding.inclInternet.root.visibility = View.GONE
      binding.cvToolbar.visibility = View.VISIBLE
      binding.rvReplies.visibility = View.VISIBLE
      binding.vDivider.visibility = View.VISIBLE
      binding.clInputReply.visibility = View.VISIBLE
    }
  }

}