package com.example.cuisineconnect.app.screen.create

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentCreateRecipeBinding
import com.example.cuisineconnect.domain.model.Hashtag
import com.google.android.material.chip.Chip
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateRecipeFragment : Fragment() {

  private lateinit var binding: FragmentCreateRecipeBinding
  private val createRecipeViewModel: CreateRecipeViewModel by viewModels()

  private val editTextIngreList = mutableListOf<EditText>()
  private val editTextStepList = mutableListOf<EditText>()

  private var imageUri: Uri? = null
  private val storageReference = FirebaseStorage.getInstance().reference

  private var currentHashtag = ""

  private val SELECT_PICTURE = 200

  // TrendingHashtagAdapter
  private lateinit var trendingHashtagAdapter: HashtagAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)

    binding.run {
      setupToolbar()
      setupRecyclerView() // Initialize the RecyclerView and adapter

      btnAddIngre.setOnClickListener {
        addNewIngredient()
      }

      btnAddStep.setOnClickListener {
        addNewStep()
      }

      btnPublish.setOnClickListener {
        val steps = createRecipeViewModel.toSteps(getSteps())
        val hashtags = getAllHashtags()

        createRecipeViewModel.saveRecipeInDatabase(
          title = etTitle.text.toString(),
          description = etDescription.text.toString(),
          portion = etPortion.text.toString().toInt(),
          duration = etDuration.text.toString().toInt(),
          image = imageUri.toString(),
          ingredients = getIngredients(),
          steps = steps,
          imageUri = imageUri,
          hashtags = hashtags
        ) { text ->
          if (text == null) {
            Toast.makeText(context, "Success! Recipe created", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
          } else {
            Toast.makeText(context, getText(text), Toast.LENGTH_SHORT).show()
          }
        }
      }

      ivImage.setOnClickListener {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
      }

      setupTagsBar()
    }
    return binding.root
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
    (activity as? AppCompatActivity)?.apply {
      setSupportActionBar(binding.toolbar)
      supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(true)
        setDisplayShowHomeEnabled(true)
        title = "Create Recipe"
      }
    }

    // Handle back button click
    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }
  }

  private fun addNewIngredient() {
    val customCardView = LayoutInflater.from(context)
      .inflate(R.layout.item_edit_text_input, binding.llIngreContainer, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    val btnDelete: ImageButton = customCardView.findViewById(R.id.btn_delete)
    etUserInput.hint = "2 Carrots"

    // Add the new EditText to the container
    binding.llIngreContainer.addView(customCardView)

    // Add the new EditText to the list
    editTextIngreList.add(etUserInput)

    btnDelete.setOnClickListener {
      binding.llIngreContainer.removeView(customCardView)
      editTextIngreList.remove(etUserInput)
    }
  }

  private fun addNewStep() {
    val customCardView = LayoutInflater.from(context)
      .inflate(R.layout.item_edit_text_input, binding.llStepContainer, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    val btnDelete: ImageButton = customCardView.findViewById(R.id.btn_delete)

    etUserInput.hint = "Start by putting all the ingredients together..."
    etUserInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

    // Add the new EditText to the container
    binding.llStepContainer.addView(customCardView)

    // Add the new EditText to the list
    editTextStepList.add(etUserInput)

    btnDelete.setOnClickListener {
      binding.llStepContainer.removeView(customCardView)
      editTextStepList.remove(etUserInput)
    }
  }

  private fun getSteps(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until (binding.llStepContainer.childCount)) {
      val childView = binding.llStepContainer.getChildAt(i)
      if (childView is CardView) {
        val editText = childView.findViewById<EditText>(R.id.etUserInput)
        if (!editText.text.isNullOrEmpty()) {
          list.add(editText.text.toString())
        }
      }
    }
    return list
  }

  private fun getIngredients(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until (binding.llIngreContainer.childCount)) {
      val childView = binding.llIngreContainer.getChildAt(i)
      if (childView is CardView) {
        val editText = childView.findViewById<EditText>(R.id.etUserInput)
        if (!editText.text.isNullOrEmpty()) {
          list.add(editText.text.toString())
        }
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
      Toast.makeText(context, "Clicked on: #${hashtag.body}", Toast.LENGTH_SHORT).show()
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
    // Check if the chip already exists
    if (!chipExists(hashtag)) {
      // Create a new chip
      val chip = Chip(context).apply {
        text = "$hashtag"
        isCloseIconVisible = true // Enable close icon to remove chip
        setOnCloseIconClickListener { removeChip(this) }
      }

      // Add chip to the ChipGroup
      binding.cgChips.addView(chip)


      // Clear the EditText after adding the chip
      binding.etTags.setText("") // Clear the input after adding the chip
//      binding.etTags.requestFocus()
//      binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
//        ViewTreeObserver.OnGlobalLayoutListener {
//        override fun onGlobalLayout() {
//          // Scroll to the EditText to keep it in view
//          binding.root.smoothScrollTo(0, binding.root.bottom)
//          binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
//        }
//      })
    } else {
      Toast.makeText(context, "Hashtag already added", Toast.LENGTH_SHORT).show()
    }
  }

  // Method to check if chip already exists
  private fun chipExists(hashtag: String): Boolean {
    for (i in 0 until binding.cgChips.childCount) {
      val chip = binding.cgChips.getChildAt(i) as Chip
      if (chip.text.toString().equals("$hashtag", ignoreCase = true)) {
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
}