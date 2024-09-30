package com.example.cuisineconnect.app.screen.create

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentCreatePostBinding
import com.example.cuisineconnect.databinding.ItemPostRecipeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CreatePostFragment : Fragment() {

  private lateinit var binding: FragmentCreatePostBinding
  private val createPostViewModel: CreatePostViewModel by viewModels()

  private var imageUri: Uri? = null

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
      recipeId?.let {
        addRecipe(it)  // Call addRecipe with the selected recipeId
      }
    }
    restorePostContent()
  }

  private fun setupDisplay() {
    binding.run {
      btnAddText.setOnClickListener {
        addText("")
      }
      btnAddImage.setOnClickListener {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
      }
      btnAddRecipe.setOnClickListener {
        findNavController().navigate(R.id.action_createPostFragment_to_homeFragment)
      }
    }
  }

  private fun addText(text: String) {
    // Add text to the container (UI logic)
    val customCardView = LayoutInflater.from(context)
      .inflate(R.layout.item_post_edit_text_input, binding.llPostContents, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    binding.llPostContents.addView(customCardView)
    etUserInput.setText(text)
    createPostViewModel.postContent.add(
      mapOf(
        "type" to "text",
        "value" to etUserInput.text.toString()
      )
    )
  }

  private fun addImage(imageUri: String) {
    // Add image to the container (UI logic)
    val customImageView = LayoutInflater.from(context)
      .inflate(R.layout.item_post_image_view, binding.llPostContents, false)
    val imageView: ImageView = customImageView.findViewById(R.id.iv_image)
    context?.let { Glide.with(it).load(imageUri).into(imageView) }
    binding.llPostContents.addView(customImageView)

    // Store the image URI in postContent
    createPostViewModel.postContent.add(mapOf("type" to "image", "value" to imageUri))
  }

  private fun addRecipe(recipeId: String) {
    lifecycleScope.launch {
      createPostViewModel.getRecipeById(recipeId)
      createPostViewModel.recipes.collectLatest { pair ->
        val (user, recipe) = pair ?: return@collectLatest

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
            tvBookmarkCount.text = recipe.bookmarkCount.toString()

            val dateFormat = SimpleDateFormat("MMM dd")
            val formattedDate = dateFormat.format(recipe.date)
            tvDate.text = formattedDate

            Glide
              .with(binding.root)
              .load(recipe.image)
              .into(ivImageTitle)

            user?.let {
              tvUsername.text = it.name
              Glide
                .with(binding.root)
                .load(it.image)
                .into(ivUserProfile)
            }
          }

          binding.llPostContents.addView(bindingRecipe.root)
          createPostViewModel.postContent.add(mapOf("type" to "recipe", "value" to recipeId))
          return@collectLatest
        }
      }
    }
  }

  private fun restorePostContent() {
    // Create a copy of the postContent to avoid ConcurrentModificationException
    val currentPostContent = ArrayList(createPostViewModel.postContent)

    currentPostContent.forEach { item ->
      when (item["type"]) {
        "text" -> addText(item["value"] ?: "")
        "image" -> addImage(item["value"] ?: "")
        "recipe" -> addRecipe(item["value"] ?: "")
      }
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == RESULT_OK) {
      // compare the resultCode with the
      // SELECT_PICTURE constant

      if (requestCode == SELECT_PICTURE) {
        // Get the url of the image from data
        imageUri = data?.data
        if (null != imageUri) {
          // update the preview image in the layout
//          IVPreviewImage.setImageURI(selectedImageUri)
          addImage(imageUri.toString())
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
        findNavController().navigateUp() // Navigate back to the previous fragment
      }
    }
  }

}