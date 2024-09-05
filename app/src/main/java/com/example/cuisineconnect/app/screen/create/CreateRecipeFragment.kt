package com.example.cuisineconnect.app.screen.create

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentCreateRecipeBinding
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class CreateRecipeFragment : Fragment() {

  private lateinit var binding: FragmentCreateRecipeBinding

  private val editTextIngreList = mutableListOf<EditText>()
  private val editTextStepList = mutableListOf<EditText>()

  private var imageUri: Uri? = null
  private val storageReference = FirebaseStorage.getInstance().reference

  var SELECT_PICTURE: Int = 200

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // Inflate the layout for this fragment
    binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)

    setupToolbar()

    binding.btnAddIngre.setOnClickListener {
      addNewIngredient()
    }

    binding.btnAddStep.setOnClickListener {
      addNewStep()
    }

    binding.btnSubmitIngre.setOnClickListener {
      printAll()
    }

    binding.btnSubmitStep.setOnClickListener {
      printAllStep()
    }

    binding.btnAddImage.setOnClickListener {
      val i = Intent()
      i.setType("image/*")
      i.setAction(Intent.ACTION_GET_CONTENT)

      startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }

    return binding.root
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
          Glide.with(this)
            .load(imageUri)   // Load the image URL into the ImageView
            .into(binding.ivImage)
          binding.ivImage.visibility = View.VISIBLE
        }
      }
    }
  }

  private fun uploadImage() {
    if (imageUri != null) {
      // Unique name for the image
      val fileName = UUID.randomUUID().toString()
      val ref = storageReference.child("images/$fileName")

      ref.putFile(imageUri!!)
        .addOnSuccessListener {
          // Get download URL and display it
          ref.downloadUrl.addOnSuccessListener {
            uri ->
            Log.d("FirebaseStorage", "Image URL: $uri")
            Toast.makeText(context, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
          }
        }
        .addOnFailureListener { e ->
          Log.e("FirebaseStorage", "Upload Failed", e)
          Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
        }
    } else {
      Toast.makeText(context, "No Image Selected", Toast.LENGTH_SHORT).show()
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

  private fun addNewIngredient() {
      val customCardView = LayoutInflater.from(context).inflate(R.layout.item_edit_text_input, binding.llIngreContainer, false)
      val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
      etUserInput.hint = "2 Carrots"

    // Add the new EditText to the container
    binding.llIngreContainer.addView(customCardView)

    // Add the new EditText to the list
    editTextIngreList.add(etUserInput)
  }

  private fun addNewStep() {
    val customCardView = LayoutInflater.from(context).inflate(R.layout.item_edit_text_input, binding.llStepContainer, false)
    val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
    etUserInput.hint = "Start by putting all the ingredients together..."
    etUserInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

    // Add the new EditText to the container
    binding.llStepContainer.addView(customCardView)

    // Add the new EditText to the list
    editTextStepList.add(etUserInput)
  }

  private fun printAll() {
    Log.d("brobruh", "${(binding.llIngreContainer?.childCount ?: 0)}")

    for (i in 0 until (binding.llIngreContainer.childCount ?: 0)) {
      val childView = binding.llIngreContainer.getChildAt(i)

      // Check if the child is a CardView and contains the EditText
      if (childView is CardView) {
        val editText = childView.findViewById<EditText>(R.id.etUserInput)
        if (editText != null) {
          Log.d("brobruh", "ingre: ${editText.layout.text}")
        }
      }
    }
  }

  private fun printAllStep() {
    Log.d("brobruh", "${(binding.llStepContainer?.childCount ?: 0)}")

    for (i in 0 until (binding.llStepContainer.childCount ?: 0)) {
      val childView = binding.llStepContainer.getChildAt(i)

      // Check if the child is a CardView and contains the EditText
      if (childView is CardView) {
        val editText = childView.findViewById<EditText>(R.id.etUserInput)
        if (editText != null) {
          Log.d("brobruh", "step: ${editText.layout.text}")
        }
      }
    }
  }

}