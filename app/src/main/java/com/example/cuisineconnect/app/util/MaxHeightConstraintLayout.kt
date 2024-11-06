package com.example.cuisineconnect.app.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.cuisineconnect.R
import kotlin.math.min

class MaxHeightConstraintLayout @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val maxHeightDp = 600 // Maximum height in dp
  private val maxHeightPx = (maxHeightDp * resources.displayMetrics.density).toInt()

  private val shortHeightDp = 150 // height for short in dp
  private val shortHeightPx = (shortHeightDp * resources.displayMetrics.density).toInt()

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    // Measure the layout height and cap it at maxHeightPx if exceeded
    val heightSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
    super.onMeasure(widthMeasureSpec, heightSpec)

    // Apply the minimum height between measured and max height
    val newHeight = min(maxHeightPx, measuredHeight)
    setMeasuredDimension(measuredWidth, newHeight)

    // Control the visibility of fl_shadow based on height
    val flShadow = findViewById<View>(R.id.fl_shadow)
    flShadow?.visibility = if (newHeight < maxHeightPx) View.GONE else View.VISIBLE

    val tvPost = findViewById<TextView>(R.id.tv_constant_post)
    if (newHeight >= shortHeightPx){
      tvPost?.layoutParams = tvPost?.layoutParams?.apply {
        width = (7 * resources.displayMetrics.density).toInt()
      }
      tvPost?.requestLayout()
    }
  }
}