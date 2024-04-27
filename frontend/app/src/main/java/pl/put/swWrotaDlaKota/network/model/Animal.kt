package pl.put.swWrotaDlaKota.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Animal (
    @Json(name = "name")
    val name : String = "",
    @Json(name = "photos")
    val photo : List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AnimalPhoto(
    @Json(name = "animalPhoto")
    val animalPhoto: String = ""
)

@JsonClass(generateAdapter = true)
data class AnimalPhotoList(
    @Json(name = "animalPhoto")
    val animalPhoto: List<String> = emptyList()
)


@JsonClass(generateAdapter = true)
data class AnimalName(
    @Json(name = "animalName")
    val animalName: String
)