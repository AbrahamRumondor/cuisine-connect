package com.example.cuisineconnect.app.util

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.min

class MaxHeightConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val maxHeightDp = 700 // Maximum height in dp
    private val maxHeightPx = (maxHeightDp * resources.displayMetrics.density).toInt()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Measure the layout height and cap it at maxHeightPx if exceeded
        val heightSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightSpec)
        
        // Apply the minimum height between measured and max height
        val newHeight = min(maxHeightPx, measuredHeight)
        setMeasuredDimension(measuredWidth, newHeight)
    }
}