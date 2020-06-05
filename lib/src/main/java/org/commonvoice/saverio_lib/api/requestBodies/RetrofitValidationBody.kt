package org.commonvoice.saverio_lib.api.requestBodies

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.commonvoice.saverio_lib.models.Validation

@JsonClass(generateAdapter = true)
data class RetrofitValidationBody(

    @Json(name = "isValid")
    val isValid: Boolean,

    @Json(name = "challenge")
    val challenge: String? = null

) {

    companion object {

        fun fromValidation(validation: Validation) = RetrofitValidationBody(validation.isValid)

    }

}