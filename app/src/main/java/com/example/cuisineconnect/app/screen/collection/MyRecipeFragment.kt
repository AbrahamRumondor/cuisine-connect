package com.example.cuisineconnect.app.screen.collection

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cuisineconnect.R
import com.example.cuisineconnect.domain.model.Recipe

/**
 * A fragment representing a list of Items.
 */
class MyRecipeFragment : Fragment() {

  private var columnCount = 1

  val ITEMS: List<Recipe> = listOf(
    Recipe("1", "A Sweet and Sour Dessert?! Under 10$", "Description 1 This is a very long description that might take more than two lines. If it does, it will be truncated with an aaewfjioawejfoawefjawdjfkladjf ellipsis." ),
    Recipe("2", "Recipe 2", "Description 2 This is a very long description that might take more than two lines. If it does, it will be truncated with an aaewfjioawejfoawefjawdjfkladjf ellipsis." ),
    Recipe("3", "Recipe 3", "Description 3 This is a very long description that might take more than two lines. If it does, it will be truncated with an aaewfjioawejfoawefjawdjfkladjf ellipsis."),
    Recipe("1", "Recipe 1", "Description 1" ),
    Recipe("2", "Recipe 2", "Description 2This is a very long description that might take more than two lines. If it does, it will be truncated with an aaewfjioawejfoawefjawdjfkladjf ellipsis." ),
    Recipe("3", "Recipe 3", "Description 3"),
    Recipe("1", "Recipe 1", "Description 1" ),
    Recipe("2", "Recipe 2", "Description 2 This is a very long description that might take more than two lines. If it does, it will be truncated with an aaewfjioawejfoawefjawdjfkladjf ellipsis." ),
    Recipe("3", "Recipe 3", "Description 3"),
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      columnCount = it.getInt(ARG_COLUMN_COUNT)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_my_recipe_list, container, false)

    // Set the adapter
    if (view is RecyclerView) {
      with(view) {
        layoutManager = when {
          columnCount <= 1 -> LinearLayoutManager(context)
          else -> GridLayoutManager(context, columnCount)
        }
        adapter = MyRecipeRecyclerViewAdapter(ITEMS)
      }
    }

    view.isNestedScrollingEnabled = true
    return view
  }

  companion object {

    // TODO: Customize parameter argument names
    const val ARG_COLUMN_COUNT = "column-count"

    // TODO: Customize parameter initialization
    @JvmStatic
    fun newInstance(columnCount: Int) =
      MyRecipeFragment().apply {
        arguments = Bundle().apply {
          putInt(ARG_COLUMN_COUNT, columnCount)
        }
      }
  }
}