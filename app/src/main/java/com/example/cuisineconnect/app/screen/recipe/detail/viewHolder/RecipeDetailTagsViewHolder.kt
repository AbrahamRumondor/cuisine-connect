import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cuisineconnect.R
import com.example.cuisineconnect.databinding.RecipeDetailTagsBinding
import com.example.cuisineconnect.domain.model.Hashtag
import com.google.android.material.chip.Chip

class RecipeDetailTagsViewHolder(
  private val view: RecipeDetailTagsBinding,
) : RecyclerView.ViewHolder(view.root) {

  fun bind(tags: List<String>) {
    view.cgChips.removeAllViews() // Clear any existing chips before adding new ones

    tags.forEach { hashtag ->
      val chip = Chip(view.root.context).apply {
        text = hashtag
        isClickable = false // Disable click if not needed
        isCheckable = false
        chipStrokeColor = ContextCompat.getColorStateList(context, R.color.white)
        chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.cc_text_dark_green) // Set background color
        setTextColor(ContextCompat.getColor(context, R.color.white)) // Set text color
      }
      view.cgChips.addView(chip)
    }
  }

}