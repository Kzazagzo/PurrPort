package pl.put.swWrotaDlaKota.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DoorState (
    @Json(name = "state")
    val state : String = ""
)

@JsonClass(generateAdapter = true)
data class DoorValue (
    @Json(name = "value")
    val value : Float
)

@JsonClass(generateAdapter = true)
data class PhotoEvery (
    @Json(name = "value")
    val value : Float
)

