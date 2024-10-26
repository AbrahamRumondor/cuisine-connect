import androidx.recyclerview.widget.RecyclerView
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
      }
      view.cgChips.addView(chip)
    }
  }
}