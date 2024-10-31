package com.example.cuisineconnect.app.screen.create

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentCreatePostBinding
import com.example.cuisineconnect.databinding.ItemPostRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CreatePostFragment : Fragment() {

  private lateinit var binding: FragmentCreatePostBinding
  private val createPostViewModel: CreatePostViewModel by viewModels()

  private var imageUri: Uri? = null
  private var imageToReplaceIndex: Int? = null

  var SELECT_PICTURE: Int = 200

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentCreatePostBinding.inflate(inflater, container, false)
    setupToolbar()

    setupDisplay()

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity().supportFragmentManager.setFragmentResultListener(
      "requestKey",
      viewLifecycleOwner
    ) { requestKey, bundle ->
      val recipeId = bundle.getString("recipeId")
      Log.d("oofoof", recipeId ?: "none")

      recipeId?.let { id ->
        lifecycleScope.launch {
          Log.d("lololol", "this: ${createPostViewModel.postContent}")
          restorePostContent()
          addRecipe(id, true, null)
        }
      }
    }
  }

  private fun setupDisplay() {
    binding.run {
      btnAddText.setOnClickListener {
        addText("", true, null)
      }
      btnAddImage.setOnClickListener {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
      }
      btnAddRecipe.setOnClickListener {
        updateTextPostContent()
        findNavController().navigate(R.id.action_createPostFragment_to_homeFragment)
      }

      val postText = Html.fromHtml("<b><u>Post</u></b>")
      btnPost.text = postText
      btnPost.setOnClickListener {
        updateTextPostContent()
        llLoading.root.visibility = View.VISIBLE
        createPostViewModel.savePostInDatabase {
          Toast.makeText(activity, "Successfully Created Post", Toast.LENGTH_SHORT).show()
          llLoading.root.visibility = View.GONE
          findNavController().popBackStack()
        }
      }
    }
  }

  private fun addText(text: String, isFromListener: Boolean, order: Int?) {
    // Add text to the container (UI logic)
    val customCardView = LayoutInflater.from(context)
      .inflate(R.layout.item_post_edit_text_input, binding.llPostContents, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    etUserInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

    if (order != null && order <= binding.llPostContents.childCount) {
      binding.llPostContents.removeViewAt(order)
      binding.llPostContents.addView(customCardView, order)
    } else {
      binding.llPostContents.addView(customCardView, binding.llPostContents.childCount)
    }

    etUserInput.setText(text)

    // If the function is called by a listener, add text content to ViewModel
    if (isFromListener) {
      Log.d("lololol", "add text")
      createPostViewModel.postContent.add(
        mutableMapOf(
          "type" to "${createPostViewModel.postContent.size}_text",
          "value" to etUserInput.text.toString()
        )
      )
    }

    // Scroll to the bottom of the ScrollView first, then focus on the EditText
    binding.svPost.post {
      binding.svPost.fullScroll(View.FOCUS_DOWN)

      // Request focus on the new EditText and show the keyboard after scrolling
      etUserInput.requestFocus()
      etUserInput.post {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(etUserInput, InputMethodManager.SHOW_IMPLICIT)
      }
    }
  }

  private fun addImage(imageUri: String, isFromListener: Boolean, order: Int?) {
    // Add image to the container (UI logic)
    val customImageView = LayoutInflater.from(context)
      .inflate(R.layout.item_post_image_view, binding.llPostContents, false)
    val imageView: ImageView = customImageView.findViewById(R.id.iv_image)

    // Load image into the ImageView
    context?.let { Glide.with(it).load(imageUri).into(imageView) }

    // Set a click listener to allow replacing the image
    imageView.setOnClickListener {
      imageToReplaceIndex = order ?: binding.llPostContents.indexOfChild(customImageView)
      val intent = Intent()
      intent.type = "image/*"
      intent.action = Intent.ACTION_GET_CONTENT
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    // Add the image to the correct position in the layout
    if (order != null && order <= binding.llPostContents.childCount) {
      binding.llPostContents.removeViewAt(order)
      binding.llPostContents.addView(customImageView, order)
    } else {
      binding.llPostContents.addView(customImageView, binding.llPostContents.childCount)
    }

    // Store the image URI in postContent if this is a new addition
    if (isFromListener) {
      Log.d("lololol", "add image")
      createPostViewModel.postContent.add(
        mutableMapOf(
          "type" to "${createPostViewModel.postContent.size}_image",
          "value" to imageUri
        )
      )
    }

    // Introduce a delay before scrolling to the bottom
    binding.llPostContents.postDelayed({
      binding.svPost.fullScroll(View.FOCUS_DOWN)
    }, 100) // Adjust the delay as necessary
  }

  private fun addRecipe(recipeId: String, isFromListener: Boolean, order: Int?) {
    createPostViewModel.getRecipeById(recipeId) { pair ->
      if (pair == null) {
        // TODO
        return@getRecipeById
      }

      val (user, recipe) = pair

      // Only add the recipe if it matches the given recipeId
      if (recipe.id == recipeId) {
        val bindingRecipe =
          ItemPostRecipeBinding.inflate(
            LayoutInflater.from(context),
            binding.llPostContents,
            false
          )

        // Update the UI with the recipe data
        bindingRecipe.run {
          tvTitle.text = recipe.title
          tvDesc.text = recipe.description
          tvUpvoteCount.text = recipe.upvotes.size.toString()
          tvReplyCount.text = recipe.replyCount.toString()
          tvBookmarkCount.text = recipe.bookmarks.size.toString()

          val dateFormat = SimpleDateFormat("MMM dd")
          val formattedDate = dateFormat.format(recipe.date)
          tvDate.text = formattedDate

          Glide
            .with(binding.root)
            .load(recipe.image)
            .placeholder(R.drawable.ic_no_image)
            .into(ivImageTitle)

          user.let {
            tvUsername.text = it.name
            Glide
              .with(binding.root)
              .load(it.image)
              .into(ivUserProfile)
          }
        }


        if (order != null && order <= binding.llPostContents.childCount) {
          binding.llPostContents.removeViewAt(order)
          binding.llPostContents.addView(bindingRecipe.root, order)
        } else {
          binding.llPostContents.addView(bindingRecipe.root, binding.llPostContents.childCount)
        }
//        binding.llPostContents.addView(bindingRecipe.root)
        Log.d("lololol", "isFrom: $isFromListener recipe ${createPostViewModel.postContent}")
        if (isFromListener) {
          createPostViewModel.postContent.add(
            mutableMapOf(
              "type" to "${createPostViewModel.postContent.size}_recipe",
              "value" to recipeId
            )
          )
        }
      }

      binding.svPost.post {
        binding.svPost.fullScroll(View.FOCUS_DOWN)
      }
    }
  }

  private suspend fun restorePostContent() = coroutineScope {
    // Create a copy of the postContent to avoid ConcurrentModificationException
    val currentPostContent = createPostViewModel.postContent.toList()

    for (item in currentPostContent) {
      val customCardView = LayoutInflater.from(context)
        .inflate(R.layout.item_post_edit_text_input, binding.llPostContents, false)
      binding.llPostContents.addView(customCardView)
    }

    // Sequentially process each item
    for (item in currentPostContent) {
      Log.d("lolololo", "Processing: $item")
      val type = item["type"] ?: ""
      when {
        type.contains("text") -> addText(
          item["value"] ?: "", false,
          type.substringBefore("_").toInt()
        )

        type.contains("image") -> addImage(
          item["value"] ?: "",
          false,
          type.substringBefore("_").toInt()
        )

        type.contains("recipe") -> addRecipeSuspend(
          item["value"] ?: "",
          false,
          type.substringBefore("_").toInt()
        )
      }
    }
  }

  private suspend fun addRecipeSuspend(recipeId: String, isFromListener: Boolean, order: Int?) =
    withContext(
      Dispatchers.IO
    ) {
      // Whatever logic you have in addRecipe, ensure it's coroutine-friendly
      addRecipe(recipeId, isFromListener, order)
    }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == RESULT_OK) {
      if (requestCode == SELECT_PICTURE) {
        imageUri = data?.data
        if (null != imageUri) {
          imageUri?.let {
            // Check if we are replacing an existing image
            imageToReplaceIndex?.let { index ->
              // Replace the image in the layout and update the ViewModel content
              createPostViewModel.postContent[index] = mutableMapOf(
                "type" to "${index}_image",
                "value" to it.toString()
              )

              // Replace the existing image in the UI
              addImage(it.toString(), false, index)

              // Reset the image replacement index
              imageToReplaceIndex = null
            } ?: run {
              // If it's a new image, just add it
              createPostViewModel.imageList.add(it)
              addImage(it.toString(), true, null)
            }
          }
        }
      }
    }
  }

  // TODO pake list of map

  private fun setupToolbar() {
    binding.toolbar.apply {

      // Set the back button icon
      setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)

      // Handle the back button click event
      setNavigationOnClickListener {
        createPostViewModel.clearPostContents()
        findNavController().navigateUp() // Navigate back to the previous fragment
      }
    }
  }

  private fun updateTextPostContent(): List<String> {
    val list = mutableListOf<String>()

    // Loop through all child views in the LinearLayout
    for (i in 0 until binding.llPostContents.childCount) {
      val linearLayout = binding.llPostContents.getChildAt(i)

      // Check if the child is a CardView and contains the EditText
      if (linearLayout is LinearLayout) {
        val editText = linearLayout.findViewById<EditText>(R.id.etUserInput)
        if (editText != null) {
          // Get the text from the EditText
          val text = editText.text.toString()

          // Update the corresponding postContent item where type matches "${i}_text"
          val updatedPostContent = createPostViewModel.postContent.map { post ->
            if (post["type"] == "${i}_text") {
              post.toMutableMap().apply {
                put("value", text)
              }
            } else post
          }
          createPostViewModel.postContent = updatedPostContent.toMutableList()

          // Also add it to the local list if needed
          list.add(text)
        }
      }
    }
    Log.d("brobruh", "Updated list: $list")
    return list
  }

}