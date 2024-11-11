package com.example.cuisineconnect.app.screen.profile.edit

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.alfaresto_customersapp.data.network.NetworkUtils
import com.example.cuisineconnect.R
import com.example.cuisineconnect.app.screen.profile.ProfileViewModel
import com.example.cuisineconnect.databinding.FragmentProfileEditBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileEditFragment : Fragment() {

  private lateinit var binding: FragmentProfileEditBinding

  private val args: ProfileEditFragmentArgs by navArgs()

  private val profileViewModel: ProfileViewModel by activityViewModels()

  private var imageUri: Uri? = null
  var SELECT_PICTURE: Int = 200

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentProfileEditBinding.inflate(inflater, container, false)

    lifecycleScope.launch {
      delay(2000)
      setConnectionBehaviour()
    }
    binding.inclInternet.btnInetTryAgain.setOnClickListener {
      setConnectionBehaviour()
    }

    val userId = args.userId

    setupToolbar()
    populateProfile()
    onSaveClicked()

    // Inflate the layout for this fragment
    return binding.root
  }



  private fun onSaveClicked() {
    binding.run {
      btnSave.setOnClickListener {
        val username = etDisplayName.text.toString()
//        val email = etEmail.text.toString()
//        val password = etPassword.text.toString()

        if (username.isEmpty()) {
          Toast.makeText(activity, "Please enter your username", Toast.LENGTH_SHORT)
            .show()
          return@setOnClickListener
        }

        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Are you sure with the changes?")
        builder.setPositiveButton("Yes") { dialog, _ ->
          applySaveEdit(username)
          dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
          dialog.dismiss()
        }
        builder.create().show()
      }
    }
  }

  private fun FragmentProfileEditBinding.applySaveEdit(username: String) {
    llLoading.root.visibility = View.VISIBLE
    profileViewModel.updateUser(username, "", "", imageUri) {
      llLoading.root.visibility = View.GONE
      Toast.makeText(activity, "Successfully updated your profile", Toast.LENGTH_SHORT).show()
      findNavController().navigateUp() // Navigate back to the previous fragment
    }
  }

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

  private fun populateProfile() {
    lifecycleScope.launch {
      profileViewModel.user.collectLatest { user ->
        binding.run {

          Glide.with(root)
            .load(user.image)
            .placeholder(R.drawable.ic_bnv_profile)
            .into(ivProfilePicture)

          etDisplayName.setText(user.displayName)
//          etEmail.setText(user.email)
//          etPassword.setText(user.password)

          ivProfilePicture.setOnClickListener {
              val i = Intent()
              i.setType("image/*")
              i.setAction(Intent.ACTION_GET_CONTENT)

              startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
          }
        }
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
          Glide.with(this)
            .load(imageUri)   // Load the image URL into the ImageView
            .into(binding.ivProfilePicture)
        }
      }
    }
  }

  private fun setConnectionBehaviour() {
    if (NetworkUtils.isConnectedToNetwork.value == false) {
      binding.inclInternet.root.visibility = View.VISIBLE
      binding.svEditProfile.visibility = View.GONE
      Toast.makeText(requireContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
    } else {
      binding.inclInternet.root.visibility = View.GONE
      binding.svEditProfile.visibility = View.VISIBLE
    }
  }
}