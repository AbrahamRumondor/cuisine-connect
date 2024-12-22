package com.example.cuisineconnect.app.screen.create

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.data.response.RecipeResponse
import com.example.cuisineconnect.databinding.FragmentCreateRecipeBinding
import com.example.cuisineconnect.domain.callbacks.TwoWayCallback
import com.example.cuisineconnect.domain.model.Hashtag
import com.example.cuisineconnect.domain.model.Recipe
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.Date

@AndroidEntryPoint
class CreateRecipeFragment : Fragment() {

  private lateinit var binding: FragmentCreateRecipeBinding
  private val createRecipeViewModel: CreateRecipeViewModel by viewModels()

  private val editTextIngreList = mutableListOf<EditText>()
  private val editTextStepList = mutableListOf<EditText>()

  private var imageUri: Uri? = null
  private val storageReference = FirebaseStorage.getInstance().reference

  private var currentHashtag = ""
  private var progressRecipeId: String? = null

  private val SELECT_PICTURE = 200

  // TrendingHashtagAdapter
  private lateinit var trendingHashtagAdapter: HashtagAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  private fun restoreRecipeContent(recipe: RecipeResponse) {

    Log.d("createRecipeFragmnet", "nih: $recipe")

    // Restore title, description, portion, and duration
    binding.etTitle.setText(recipe.title)
    binding.etDescription.setText(recipe.description)
    binding.etPortion.setText(recipe.portion)
    binding.etDuration.setText(recipe.duration)

    // Restore image
    binding.ivImage.visibility = View.VISIBLE
    if (recipe.image.isNotEmpty() && recipe.image != "null") {
      imageUri = Uri.parse(recipe.image)
      Glide.with(this)
        .load(imageUri)
        .into(binding.ivImage)
    }

    // Restore ingredients
    binding.llIngreContainer.removeAllViews()
    editTextIngreList.clear()
    recipe.ingredients.forEach { ingredient ->
      addNewIngredient().apply {
        setText(ingredient)
      }
    }

    // Restore steps
    binding.llStepContainer.removeAllViews()
    editTextStepList.clear()
    createRecipeViewModel.fetchSavedRecipeSteps(recipe.id) { steps ->
      Log.d("createRecipeFragment", "step: ${steps}")
      steps.sortedBy { it.no }.forEach { step ->
        addNewStep().apply {
          setText(step.body)
        }
      }
    }

    // Restore hashtags
    binding.cgChips.removeAllViews()
    recipe.hashtags.forEach { hashtag ->
      addHashtagChip(hashtag)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)

    showLoadingAnimation()

    createRecipeViewModel.fetchSavedRecipeContent {
      hideLoadingAnimation()
      it?.let { recipe ->
        val recipeMap = recipe["recipeResponse"] as? Map<String, Any>
        recipeMap?.let { map ->
          val recipeResponse = transformFromMap(map)
          progressRecipeId = recipeResponse.id
          restoreRecipeContent(recipeResponse)
        }
      }

      // Handle the case where no saved recipe is found (null case)
      it ?: run {
        // Optionally show a message or set a default UI for the case where no saved recipe exists
        Log.d("createRecipeFragment", "No saved recipe found.")
      }
    }

    binding.run {
      setupToolbar()
      setupRecyclerView() // Initialize the RecyclerView and adapter

      btnAddIngre.setOnClickListener {
        addNewIngredient()
      }

      btnAddStep.setOnClickListener {
        addNewStep()
      }

      btnDelete.setOnClickListener {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.clear_progress_title))
        builder.setMessage(getString(R.string.clear_recipe_progress_message))

        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
          createRecipeViewModel.deleteRecipeProgress(object : TwoWayCallback {
            override fun onSuccess() {
              Toast.makeText(context, getString(R.string.success_recipe_progress_cleared), Toast.LENGTH_SHORT).show()
              clearRecipeProgress()
            }

            override fun onFailure(errorMessage: String) {
              Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

          })
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
          dialog.dismiss()
        }
        builder.show()
      }

      btnSave.setOnClickListener {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.save_progress_title))
        builder.setMessage(getString(R.string.save_recipe_progress_message))

        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->

          showLoadingAnimation()
          val steps = createRecipeViewModel.toSteps(getSteps())
          val hashtags = getAllHashtags()

          createRecipeViewModel.saveRecipeProgressForCurrentUser(
            title = etTitle.text.toString(),
            description = etDescription.text.toString(),
            portion = etPortion.text.toString(),
            duration = etDuration.text.toString(),
            image = imageUri.toString(),
            ingredients = getIngredients(),
            steps = steps,
            imageUri = imageUri,
            hashtags = hashtags,
            existId = progressRecipeId
          ) { text ->
            hideLoadingAnimation()
            if (text == null) {
              Toast.makeText(context, getString(R.string.success_recipe_saved), Toast.LENGTH_SHORT).show()
              findNavController().popBackStack()
            } else {
              Toast.makeText(context, getText(text), Toast.LENGTH_SHORT).show()
            }
          }

        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
          dialog.dismiss()
        }
        builder.show()
      }


      btnPublish.setOnClickListener {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.publish_title))
        builder.setMessage(getString(R.string.publish_recipe_message))

        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
          // Validate required fields
          val title = etTitle.text.toString().trim()
          val description = etDescription.text.toString().trim()
          val portion = etPortion.text.toString().trim()
          val duration = etDuration.text.toString().trim()
          val ingredients = getIngredients()
          val steps = getSteps()
          val hashtags = getAllHashtags()

          when {
            title.isEmpty() -> {
              showError(getString(R.string.title_required))
              return@setPositiveButton
            }
//            description.isEmpty() -> {
//              showError("Description is required to publish the recipe.")
//              return@setPositiveButton
//            }
            portion.isEmpty() -> {
              showError(getString(R.string.portion_required))
              return@setPositiveButton
            }
            duration.isEmpty() -> {
              showError(getString(R.string.duration_required))
              return@setPositiveButton
            }
            ingredients.isEmpty() -> {
              showError(getString(R.string.add_ingredient_message))
              return@setPositiveButton
            }
            steps.isEmpty() -> {
              showError(getString(R.string.add_step_message))
              return@setPositiveButton
            }
          }

          // Show loading and proceed to save in the database
          showLoadingAnimation()
          createRecipeViewModel.saveRecipeInDatabase(
            title = title,
            description = description,
            portion = portion,
            duration = duration,
            image = imageUri.toString(),
            ingredients = ingredients,
            steps = createRecipeViewModel.toSteps(steps),
            imageUri = imageUri,
            hashtags = hashtags
          ) { text ->
            hideLoadingAnimation()
            if (text == null) {
              Toast.makeText(context, getString(R.string.success_recipe_created), Toast.LENGTH_SHORT).show()
              findNavController().popBackStack()
            } else {
              Toast.makeText(context, getText(text), Toast.LENGTH_SHORT).show()
            }
          }
        }

        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
          dialog.dismiss()
        }
        builder.show()
      }

      ivImage.setOnClickListener {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
      }

      setupTagsBar()
    }


    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
      guardFromBackDialog()
    }

    return binding.root
  }

  private fun guardFromBackDialog() {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(getString(R.string.notification))
    builder.setMessage(getString(R.string.guard))

    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
      findNavController().navigateUp()
      dialog.dismiss() // Dismiss the dialog
    }

    builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
      dialog.dismiss()
    }
    builder.show()
  }

  private fun showError(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
      imageUri = data?.data
      if (null != imageUri) {
        Glide.with(this)
          .load(imageUri)
          .into(binding.ivImage)
        binding.ivImage.visibility = View.VISIBLE
      }
    }
  }

  private fun setupToolbar() {
    binding.btnBack.setOnClickListener {
      guardFromBackDialog()
    }
  }

  private fun addNewIngredient(): EditText {
    val customCardView = LayoutInflater.from(context)
      .inflate(R.layout.item_edit_text_input, binding.llIngreContainer, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    val btnDelete: ImageButton = customCardView.findViewById(R.id.btn_delete)
    etUserInput.hint = getString(R.string.carrot_2)

    binding.llIngreContainer.addView(customCardView)
    editTextIngreList.add(etUserInput)

    btnDelete.setOnClickListener {
      binding.llIngreContainer.removeView(customCardView)
      editTextIngreList.remove(etUserInput)
    }

    return etUserInput
  }


  private fun addNewStep(): EditText {
    val customCardView = LayoutInflater.from(context)
      .inflate(R.layout.item_edit_text_input, binding.llStepContainer, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    val btnDelete: ImageButton = customCardView.findViewById(R.id.btn_delete)

    etUserInput.hint = getString(R.string.step_hint)
    etUserInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

    binding.llStepContainer.addView(customCardView)
    editTextStepList.add(etUserInput)

    btnDelete.setOnClickListener {
      binding.llStepContainer.removeView(customCardView)
      editTextStepList.remove(etUserInput)
    }

    return etUserInput
  }

  private fun getSteps(): List<String> {
    val list = mutableListOf<String>()
    // Iterate through all children in llStepContainer
    for (i in 0 until binding.llStepContainer.childCount) {
      val childView = binding.llStepContainer.getChildAt(i)
      val etUserInput = childView.findViewById<EditText>(R.id.etUserInput)

      // Only add the EditText content if it's not empty
      if (!etUserInput.text.isNullOrEmpty()) {
        list.add(etUserInput.text.toString())
      }
    }
    return list
  }

  private fun getIngredients(): List<String> {
    val list = mutableListOf<String>()
    // Iterate through all children in llIngreContainer
    for (i in 0 until binding.llIngreContainer.childCount) {
      val childView = binding.llIngreContainer.getChildAt(i)
      val etUserInput = childView.findViewById<EditText>(R.id.etUserInput)

      // Only add the EditText content if it's not empty
      if (!etUserInput.text.isNullOrEmpty()) {
        list.add(etUserInput.text.toString())
      }
    }
    return list
  }

  // RecyclerView setup for hashtags
  private fun setupRecyclerView() {
    trendingHashtagAdapter = HashtagAdapter(mutableListOf()) { hashtag ->
      if (hashtag.id == "create_new") {
        hashtag.apply {
          body = currentHashtag
          id = ""
        }
      }
      addHashtagChip(hashtag.body)
    }

    binding.rvTagList.apply {
      layoutManager = LinearLayoutManager(requireContext()) // Use requireContext() in fragment
      adapter = trendingHashtagAdapter
    }
  }

  private fun updateHashtags(hashtags: List<Hashtag>) {
    trendingHashtagAdapter.updateHashtags(hashtags)
  }

  private fun clearHashtagList() {
    trendingHashtagAdapter.updateHashtags(emptyList()) // Clear the adapter list
  }

  private fun setupTagsBar() {
    // Add debounced text change listener
    binding.etTags.addTextChangedListener(object : TextWatcher {
      private var searchRunnable: Runnable? = null

      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val query = s.toString().trim()
//        binding.root.post {
//          binding.root.smoothScrollTo(0, binding.root.bottom)
//        }

        // Cancel any existing search to debounce
        searchRunnable?.let {
          binding.etTags.removeCallbacks(it)
        }

        // Debounce by adding a small delay before executing search
        searchRunnable = Runnable {
          if (query.isNotEmpty()) {
            val trimmedQuery = query.trim()
            val formattedQuery =
              if (trimmedQuery.startsWith("#")) trimmedQuery else "#$trimmedQuery"

            searchHashtags(formattedQuery)
          } else {
            clearHashtagList()
          }
        }
        binding.etTags.postDelayed(searchRunnable, 300) // 300ms delay
      }

      override fun afterTextChanged(s: Editable?) {}
    })
  }

  private fun searchHashtags(query: String) {
    currentHashtag = query

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


  private fun addHashtagChip(hashtag: String) {
    if (!chipExists(hashtag)) {
      val chip = Chip(context).apply {
        text = hashtag
        isCloseIconVisible = true
        setOnCloseIconClickListener { removeChip(this) }

        // Apply custom styles
        isClickable = false
        isCheckable = false
        chipStrokeColor = ContextCompat.getColorStateList(context, R.color.white)
        chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.cc_text_dark_green)
        setTextColor(ContextCompat.getColor(context, R.color.white))
        closeIconTint = ContextCompat.getColorStateList(context, R.color.white) // Make close icon white
      }
      binding.cgChips.addView(chip)
      binding.etTags.setText("")
    } else {
      Toast.makeText(context, getString(R.string.hashtag_already_added), Toast.LENGTH_SHORT).show()
    }
  }

  // Method to check if chip already exists
  private fun chipExists(hashtag: String): Boolean {
    for (i in 0 until binding.cgChips.childCount) {
      val chip = binding.cgChips.getChildAt(i) as? Chip
      if (chip?.text.toString() == hashtag) {
        return true
      }
    }
    return false
  }

  // Method to remove chip
  private fun removeChip(chip: Chip) {
    binding.cgChips.removeView(chip)
  }

  private fun getAllHashtags(): List<String> {
    val hashtags = mutableListOf<String>()
    for (i in 0 until binding.cgChips.childCount) {
      val chip = binding.cgChips.getChildAt(i) as Chip
      hashtags.add(chip.text.toString()) // Collect the hashtag text from each chip
    }
    return hashtags
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

  private fun clearRecipeProgress() {
    // Clear input fields
    binding.etTitle.setText("")
    binding.etDescription.setText("")
    binding.etPortion.setText("")
    binding.etDuration.setText("")

    // Clear any image or reset it
    binding.ivImage.setImageResource(R.drawable.recipe_img)

    // Clear the ingredient list
    binding.llIngreContainer.removeAllViews()
    editTextIngreList.clear()

    // Clear the steps list
    binding.llStepContainer.removeAllViews()
    editTextStepList.clear()

    // Clear the hashtags
    binding.cgChips.removeAllViews()

    // Optionally update any other UI components that should reflect the cleared state
    // For example, resetting the visibility of certain views if needed
  }

  companion object {
    fun transformFromMap(map: Map<String, Any>): RecipeResponse {
      return RecipeResponse(
        id = map["recipe_id"] as? String ?: "",
        title = map["recipe_title"] as? String ?: "",
        ingredients = (map["recipe_ingredient"] as? List<String>) ?: emptyList(),
        date = (map["recipe_date"] as? Long)?.let { Date(it) } ?: Date(),
        upvotes = (map["recipe_upvotes"] as? Map<String, Boolean>) ?: emptyMap(),
        hashtags = (map["recipe_hashtags"] as? List<String>) ?: emptyList(),
        image = map["recipe_image"] as? String ?: "",
        description = map["recipe_description"] as? String ?: "",
        portion = map["recipe_portion"] as? String ?: "",
        duration = map["recipe_duration"] as? String ?: "",
        replyCount = (map["recipe_reply_count"] as? Long)?.toInt() ?: 0,
        bookmarks = (map["recipe_bookmarks"] as? Map<String, Boolean>) ?: emptyMap(),
        referencedBy = (map["recipe_referenced_by"] as? Map<String, Boolean>) ?: emptyMap(),
        userId = map["recipe_user_id"] as? String ?: "",
        recipeTitleSplit = (map["recipe_title_split"] as? List<String>) ?: emptyList()
      )
    }
  }
}