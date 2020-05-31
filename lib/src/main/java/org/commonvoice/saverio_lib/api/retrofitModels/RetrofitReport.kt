package org.commonvoice.saverio_lib.api.retrofitModels

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.commonvoice.saverio_lib.models.Report

@JsonClass(generateAdapter = true)
data class RetrofitReport(

    @Json(name = "id")
    var sentenceId: String,

    @Json(name = "kind")
    var kind: String,

    @Json(name = "reasons")
    var reasons: List<String>

) {

    constructor(report: Report) : this(report.sentenceId, report.kind, report.reasons)

}