package com.example.cuisineconnect.data.response

import com.example.cuisineconnect.domain.model.Step
import com.example.cuisineconnect.domain.model.User
import com.google.firebase.firestore.PropertyName

data class StepResponse(
  @get:PropertyName("step_id")
  @set:PropertyName("step_id")
  var id: String = "",

  @get:PropertyName("step_body")
  @set:PropertyName("step_body")
  var body: String = "",

  @get:PropertyName("step_no")
  @set:PropertyName("step_no")
  var no: Int = 0,

  @get:PropertyName("step_images")
  @set:PropertyName("step_images")
  var images: List<String> = listOf(),

) {

  constructor() : this("")

  companion object {
    fun transform(stepResponse: StepResponse): Step {
      return Step(
        id = stepResponse.id,
        body = stepResponse.body,
        no = stepResponse.no,
        images = stepResponse.images
      )
    }

    fun transform(step: Step): StepResponse {
      return StepResponse(
        id = step.id,
        body = step.body,
        no = step.no,
        images = step.images
      )
    }
  }
}