package com.example.cuisineconnect.app.screen.create

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.FragmentCreateRecipeBinding
import timber.log.Timber

class CreateRecipeFragment : Fragment() {

  private lateinit var binding: FragmentCreateRecipeBinding

  private val editTextList = mutableListOf<EditText>()

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

    binding.btnAddStep.setOnClickListener {
      addNewEditText()
    }

    binding.btnSubmit.setOnClickListener {
      Log.d("brobruh", "masuk ke click listener")
      printAll()
    }

    return binding.root
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

  private fun addNewEditText() {
      val customCardView = LayoutInflater.from(context).inflate(R.layout.item_ingredient_input, binding.llStepContainer, false)
      val etUserInput: EditText = customCardView.findViewById(R.id.etUserInput)
      etUserInput.hint = "Enter text for item"

    // Add the new EditText to the container
    binding.llStepContainer.addView(customCardView)

    // Add the new EditText to the list
    editTextList.add(etUserInput)
  }

  private fun printAll() {
    Log.d("brobruh", "${(binding.llStepContainer?.childCount ?: 0)}")

    for (i in 0 until (binding.llStepContainer.childCount ?: 0)) {
      val childView = binding.llStepContainer.getChildAt(i)

      // Check if the child is a CardView and contains the EditText
      if (childView is CardView) {
        val editText = childView.findViewById<EditText>(R.id.etUserInput)
        if (editText != null) {
          Log.d("brobruh", "coba: ${editText.layout.text}")
        }
      }
    }
  }

}