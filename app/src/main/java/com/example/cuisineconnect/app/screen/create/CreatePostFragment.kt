package com.example.cuisineconnect.app.screen.create

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.MainActivity
import com.example.cuisineconnect.app.util.UserUtil
import com.example.cuisineconnect.app.util.UserUtil.currentUser
import com.example.cuisineconnect.app.util.UserUtil.isSelectingRecipe
import com.example.cuisineconnect.databinding.FragmentCreatePostBinding
import com.example.cuisineconnect.databinding.ItemPostRecipeBinding
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.Hashtag
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CreatePostFragment : Fragment() {

  private lateinit var binding: FragmentCreatePostBinding
  private val createPostViewModel: CreatePostViewModel by viewModels()
  private val createRecipeViewModel: CreateRecipeViewModel by viewModels()

  private var imageUri: Uri? = null
  private var imageToReplaceIndex: Int? = null

  private var isNotFromAddRecipe = true

  var SELECT_PICTURE: Int = 200

  private lateinit var trendingHashtagAdapter: HashtagAdapter

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentCreatePostBinding.inflate(inflater, container, false)
    setupToolbar()

    if (isNotFromAddRecipe) {
      createPostViewModel.fetchSavedPostContent {

      }
    }

    setupDisplay()

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (isNotFromAddRecipe) {
      createPostViewModel.fetchSavedPostContent {
        lifecycleScope.launch {
          Log.d("lololol", "this notfromadd: ${createPostViewModel.postContent}")
          restorePostContent()
        }
      }
    }

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
          isNotFromAddRecipe = true
        }
      }
    }
  }

  private fun setupDisplay() {
    binding.run {
      currentUser?.let {
        tvUsername.text = it.displayName
        Glide
          .with(binding.root)
          .load(it.image)
          .placeholder(R.drawable.ic_bnv_profile)
          .into(ivUserProfile)
      }

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
        isNotFromAddRecipe = false
        updateTextPostContent()

        requireActivity().let { activity ->
          if (activity is MainActivity) {
            activity.showChooseRecipeView(true) // To show the view
            // activity.showChooseRecipeView(false) // To hide the view
          }
        }
        findNavController().navigate(R.id.action_createPostFragment_to_homeFragment)
      }

      btnSave.setOnClickListener {
        updateTextPostContent()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Save Progress")
        builder.setMessage("Are you sure to save current recipe progress?")

        builder.setPositiveButton("Yes") { dialog, _ ->
          createPostViewModel.savePostProgress(object : TwoWayCallback {
            override fun onSuccess() {
              Toast.makeText(context, "Successfully saved post", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(errorMessage: String) {
              Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
          })
        }
        builder.setNegativeButton("No") { dialog, _ ->
          dialog.dismiss()
        }
        builder.show()
      }

      btnDelete.setOnClickListener {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Clear Progress")
        builder.setMessage("Are you sure to clear current recipe progress?")

        builder.setPositiveButton("Yes") { dialog, _ ->
          createPostViewModel.deletePostProgress(object : TwoWayCallback {
            override fun onSuccess() {
              Toast.makeText(context, "Successfully delete progress", Toast.LENGTH_SHORT).show()
              binding.llPostContents.removeAllViews()
              createPostViewModel.postContent.clear()
            }

            override fun onFailure(errorMessage: String) {
              Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
          })
        }
        builder.setNegativeButton("No") { dialog, _ ->
          dialog.dismiss()
        }
        builder.show()
      }

      btnPost.setOnClickListener {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Publish")
        builder.setMessage("Are you sure you want to publish the post?")

        builder.setPositiveButton("Yes") { dialog, _ ->

          updateTextPostContent()
          showLoadingAnimation()
          createPostViewModel.savePostInDatabase {
            val contentIsEmpty = createPostViewModel.postContent.isEmpty() || createPostViewModel.postContent.any { it["value"].isNullOrEmpty() }
            if (contentIsEmpty) {
              hideLoadingAnimation()
              Toast.makeText(activity, "Please ensure all fields are filled in.", Toast.LENGTH_SHORT)
                .show()
              return@savePostInDatabase
            }
            Toast.makeText(activity, "Successfully Created Post", Toast.LENGTH_SHORT).show()
            hideLoadingAnimation()
            findNavController().popBackStack()
          }

        }
        builder.setNegativeButton("No") { dialog, _ ->
          dialog.dismiss()
        }
        builder.show()
      }
    }
  }

  private fun addText(text: String, isFromListener: Boolean, order: Int?) {
    val customCardView = LayoutInflater.from(context)
      .inflate(R.layout.item_post_edit_text_input, binding.llPostContents, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    val rvHashtag: RecyclerView = customCardView.findViewById(R.id.rv_hashtag)
    etUserInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    val deleteBtn: ImageView = customCardView.findViewById(R.id.btn_delete) // Add delete button

    if (order != null && order < binding.llPostContents.childCount) {
      binding.llPostContents.removeViewAt(order)
      binding.llPostContents.addView(customCardView, order)
    } else {
      binding.llPostContents.addView(customCardView, binding.llPostContents.childCount)
    }

    etUserInput.setText(text)

    deleteBtn.setOnClickListener {
      val index = binding.llPostContents.indexOfChild(customCardView)
      if (index != -1) {
        binding.llPostContents.removeView(customCardView)
        createPostViewModel.postContent.removeAt(index)

        for (i in index until createPostViewModel.postContent.size) {
          val entry = createPostViewModel.postContent[i]
          val type = entry["type"]
          if (type != null) {
            val typeSuffix = type.substringAfter("_")
            createPostViewModel.postContent[i]["type"] = "${i}_${typeSuffix}"
          }
        }
      }
      return@setOnClickListener
    }

    // Add TextWatcher to detect "#" and show hashtag suggestions
    etUserInput.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val query = s.toString()

        // Find the last "#" character in the query
        val lastHashPosition = query.lastIndexOf("#")
        val lastSpacePosition = query.lastIndexOf(" ")

        // Check if "#" exists and is not followed by a space
        val isShowRV = lastHashPosition > lastSpacePosition

        // If valid hashtag is found and no space after "#"
        if (isShowRV && lastHashPosition != -1) {
          val hashtagQuery =
            query.substring(lastHashPosition + 1) // Get the hashtag query after the last "#"

          if (hashtagQuery.isNotEmpty()) {
            searchHashtags("#$hashtagQuery")
            rvHashtag.visibility = View.VISIBLE // Show RecyclerView for hashtag suggestions
          }
        } else {
          rvHashtag.visibility =
            View.GONE // Hide RecyclerView if "#" is followed by a space or no hashtag after "#"
        }
      }

      override fun afterTextChanged(s: Editable?) {}
    })

    // RecyclerView setup
    setupRecyclerView(rvHashtag, etUserInput)

    // Scroll to the bottom of the ScrollView, focus on EditText, and show keyboard
    binding.svPost.post {
      binding.svPost.fullScroll(View.FOCUS_DOWN)
      etUserInput.requestFocus()
      etUserInput.post {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(etUserInput, InputMethodManager.SHOW_IMPLICIT)
      }
    }

    // Handle ViewModel update if needed
    if (isFromListener) {
      createPostViewModel.postContent.add(
        mutableMapOf(
          "type" to "${createPostViewModel.postContent.size}_text",
          "value" to etUserInput.text.toString()
        )
      )
    }
  }

  // RecyclerView setup
  private fun setupRecyclerView(rvHashtag: RecyclerView, etUserInput: EditText) {
    trendingHashtagAdapter = HashtagAdapter(mutableListOf()) { hashtag ->
      addHashtagToEditText(hashtag.body, etUserInput)
      rvHashtag.visibility = View.GONE
    }

    rvHashtag.layoutManager = LinearLayoutManager(requireContext())
    rvHashtag.adapter = trendingHashtagAdapter
  }

  private fun addHashtagToEditText(hashtag: String, etUserInput: EditText) {
    val currentText = etUserInput.text.toString()
    val cursorPosition = etUserInput.selectionStart
    val greenColor = ContextCompat.getColor(requireContext(), R.color.cc_text_dark_green)

    // Find the position of the last hashtag
    val lastHashtagStart = currentText.lastIndexOf("#", cursorPosition - 1)
    var lastHashtagEnd = currentText.indexOf(" ", lastHashtagStart)

    // If no space after the last hashtag, take the rest of the text as the hashtag
    if (lastHashtagEnd == -1) {
      lastHashtagEnd = currentText.length
      currentText.substring(lastHashtagStart)
    } else {
      currentText.substring(lastHashtagStart, lastHashtagEnd)
    }

    // Replace the last hashtag with the new hashtag
    val newText = currentText.replaceRange(
      lastHashtagStart,
      lastHashtagEnd,
      hashtag
    )

    // Create a SpannableString to apply the color to the hashtag
    val spannableText = SpannableString(newText)

    // Iterate over the text to find all hashtags and apply the green color span
    var start = newText.indexOf("#")
    while (start != -1) {
      val end = newText.indexOf(" ", start)
      val hashtagEnd = if (end == -1) newText.length else end

      // Apply the green color span to the hashtag
      spannableText.setSpan(ForegroundColorSpan(greenColor), start, hashtagEnd, 0)

      // Move to the next hashtag
      start = if (end == -1) -1 else newText.indexOf("#", hashtagEnd)
    }

    // Set the new text with colored hashtags and position the cursor after the hashtag
    etUserInput.setText(spannableText)
    etUserInput.setSelection(lastHashtagStart + hashtag.length) // Position cursor after the new hashtag


  }

  // Search hashtags and update RecyclerView
  private fun searchHashtags(query: String) {
    createRecipeViewModel.searchTags(query) { hashtags, exception ->
      if (exception == null) {
        updateHashtags(hashtags)
      } else {
        Toast.makeText(
          context,
          "Failed to fetch hashtags: ${exception.message}",
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  // Update RecyclerView adapter with new hashtags
  private fun updateHashtags(hashtags: List<Hashtag>) {
    trendingHashtagAdapter.updateHashtags(hashtags)
  }

  private fun clearHashtagList() {
    trendingHashtagAdapter.updateHashtags(emptyList()) // Clear the adapter list
  }

  private fun addImage(imageUri: String, isFromListener: Boolean, order: Int?) {
    // Add image to the container (UI logic)
    val customImageView = LayoutInflater.from(context)
      .inflate(R.layout.item_post_image_view, binding.llPostContents, false)
    val imageView: ImageView = customImageView.findViewById(R.id.iv_image)
    val btnDelete: ImageView = customImageView.findViewById(R.id.btn_delete)
    btnDelete.visibility = View.VISIBLE

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

    btnDelete.setOnClickListener {
      val index = binding.llPostContents.indexOfChild(customImageView)
      if (index != -1) {
        binding.llPostContents.removeView(customImageView)
        createPostViewModel.postContent.removeAt(index) // remove where type is index_image

        for (i in index until createPostViewModel.postContent.size) {
          val entry = createPostViewModel.postContent[i]
          val type = entry["type"]
          if (type != null) {
            val typeSuffix = type.substringAfter("_")
            createPostViewModel.postContent[i]["type"] = "${i}_${typeSuffix}"
          }
        }
      }
      return@setOnClickListener
    }

    // Add the image to the correct position in the layout
    if (order != null && order < binding.llPostContents.childCount) {
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
          tvUpvoteCount.text = recipe.upvotes.size.toString()
          tvReplyCount.text = recipe.replyCount.toString()
          tvBookmarkCount.text = recipe.bookmarks.size.toString()

          val dateFormat = SimpleDateFormat("MMM dd")
          val formattedDate = dateFormat.format(recipe.date)

          Glide
            .with(binding.root)
            .load(recipe.image)
            .placeholder(R.drawable.recipe_img)
            .into(ivImage)

          user.let {
            tvUsername.text = it.displayName
            Glide
              .with(binding.root)
              .load(it.image)
              .placeholder(R.drawable.ic_bnv_profile)
              .into(ivUserProfile)
          }

          btnDelete.visibility = View.VISIBLE
          btnDelete.setOnClickListener {
            val index = binding.llPostContents.indexOfChild(bindingRecipe.root)
            if (index != -1) {
              binding.llPostContents.removeView(bindingRecipe.root)
              createPostViewModel.postContent.removeAt(index)

              for (i in index until createPostViewModel.postContent.size) {
                val entry = createPostViewModel.postContent[i]
                val type = entry["type"]
                if (type != null) {
                  val typeSuffix = type.substringAfter("_")
                  createPostViewModel.postContent[i]["type"] = "${i}_${typeSuffix}"
                }
              }
            }
            return@setOnClickListener
          }
        }

        if (order != null && order < binding.llPostContents.childCount) {
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
    binding.btnBack.setOnClickListener {
      createPostViewModel.clearPostContents()
      findNavController().navigateUp()
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

  private fun showLoadingAnimation() {
    binding.flShadow.visibility = View.VISIBLE
    binding.progressBar.setAnimation(R.raw.cc_loading) // Set the animation from res/raw
    binding.progressBar.repeatCount = LottieDrawable.INFINITE // Loop the animation infinitely
    binding.progressBar.playAnimation() // Start the animation
    binding.progressBar.visibility = View.VISIBLE
  }

  private fun hideLoadingAnimation() {
    binding.progressBar.cancelAnimation() // Stop the Lottie animation
    binding.progressBar.visibility = View.GONE
    binding.flShadow.visibility = View.GONE
  }

  override fun onResume() {
    super.onResume()
    requireActivity().let { activity ->
      if (activity is MainActivity) {
        activity.showChooseRecipeView(false) // To show the view
        // activity.showChooseRecipeView(false) // To hide the view
      }
    }
  }

}

//  private fun addText(text: String, isFromListener: Boolean, order: Int?) {
//    // Add text to the container (UI logic)
//    val customCardView = LayoutInflater.from(context)
//      .inflate(R.layout.item_post_edit_text_input, binding.llPostContents, false)
//    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
//    etUserInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
//
//    if (order != null && order <= binding.llPostContents.childCount) {
//      binding.llPostContents.removeViewAt(order)
//      binding.llPostContents.addView(customCardView, order)
//    } else {
//      binding.llPostContents.addView(customCardView, binding.llPostContents.childCount)
//    }
//
//    etUserInput.setText(text)
//
//    // If the function is called by a listener, add text content to ViewModel
//    if (isFromListener) {
//      Log.d("lololol", "add text")
//      createPostViewModel.postContent.add(
//        mutableMapOf(
//          "type" to "${createPostViewModel.postContent.size}_text",
//          "value" to etUserInput.text.toString()
//        )
//      )
//    }
//
//    // Scroll to the bottom of the ScrollView first, then focus on the EditText
//    binding.svPost.post {
//      binding.svPost.fullScroll(View.FOCUS_DOWN)
//
//      // Request focus on the new EditText and show the keyboard after scrolling
//      etUserInput.requestFocus()
//      etUserInput.post {
//        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//        imm?.showSoftInput(etUserInput, InputMethodManager.SHOW_IMPLICIT)
//      }
//    }
//  }