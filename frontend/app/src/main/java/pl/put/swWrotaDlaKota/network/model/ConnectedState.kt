package pl.put.swWrotaDlaKota.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConnectedState (
    @Json(name = "connected")
    val connected : String = "",
)