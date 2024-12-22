package com.example.cuisineconnect.app.util

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.example.cuisineconnect.R
import java.util.regex.Pattern

class ClickableTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

  fun setHashtagText(text: String, hashtagClickListener: (String) -> Unit) {
    val spannableString = SpannableStringBuilder(text)

    // Regular expression to find hashtags
    val hashtagPattern = Pattern.compile("#(\\w+)")
    val matcher = hashtagPattern.matcher(text)

    // Get green color from resources
    val greenColor = ContextCompat.getColor(context, R.color.cc_text_dark_green)

    // Loop through all matches and apply both ClickableSpan and ForegroundColorSpan to each hashtag
    while (matcher.find()) {
      val hashtag = matcher.group() // The matched hashtag text
      val start = matcher.start()
      val end = matcher.end()

      // Set ClickableSpan and color span for each hashtag
      spannableString.setSpan(object : ClickableSpan() {
        override fun onClick(widget: View) {
          // Trigger the click listener when hashtag is clicked
          hashtagClickListener(hashtag ?: "")
        }
      }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

      // Apply green color to the hashtag
      spannableString.setSpan(ForegroundColorSpan(greenColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // Set the styled text and enable link movement
    setText(spannableString)
    movementMethod = LinkMovementMethod.getInstance()
  }
}

class NonClickableTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

  fun setHashtagText(text: String) {
    val spannableString = SpannableStringBuilder(text)

    // Regular expression to find hashtags
    val hashtagPattern = Pattern.compile("#(\\w+)")
    val matcher = hashtagPattern.matcher(text)

    // Get green color from resources
    val greenColor = ContextCompat.getColor(context, R.color.cc_text_dark_green)

    // Loop through all matches and apply color span to each hashtag
    while (matcher.find()) {
      val start = matcher.start()
      val end = matcher.end()

      // Apply green color to the hashtag
      spannableString.setSpan(ForegroundColorSpan(greenColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // Set the styled text
    setText(spannableString)
  }

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    // Return false to allow touch events to pass through to the parent view
    return false
  }
}