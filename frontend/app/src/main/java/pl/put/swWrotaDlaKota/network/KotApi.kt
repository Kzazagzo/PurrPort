package pl.put.swWrotaDlaKota.network

import okhttp3.MultipartBody
import pl.put.swWrotaDlaKota.network.model.AnimalName
import pl.put.swWrotaDlaKota.network.model.Animal
import pl.put.swWrotaDlaKota.network.model.ConnectedState
import pl.put.swWrotaDlaKota.network.model.AnimalPhoto
import pl.put.swWrotaDlaKota.network.model.AnimalPhotoList
import pl.put.swWrotaDlaKota.network.model.DoorState
import pl.put.swWrotaDlaKota.network.model.DoorValue
import pl.put.swWrotaDlaKota.network.model.PhotoEvery
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface KotApi {

    @GET("/manual/connect")
    suspend fun getConnectedState(
    ): ConnectedState

    @GET("/animal/getAnimalsList")
    suspend fun getAnimalsList(
    ): List<Animal>

    @POST("/animal/getAnimalPhotos")
    suspend fun getAnimalPhotos(
        @Body animalName: AnimalName
    ): AnimalPhotoList

    @POST("animal/popAnimalPhoto")
    suspend fun popAnimalPhoto(
      @Body animalPhoto: AnimalPhoto
    )

    @POST("animal/addAnimal")
    suspend fun addAnimal(
       @Body animalName: AnimalName
    )

    @POST("animal/popAnimal")
    suspend fun popAnimal(
        @Body animalName: AnimalName
    )

    @Multipart
    @POST("/animal/addAnimalPhoto")
    suspend fun addAnimalPhoto(
        @Part("animalName") animalName: String,
        @Part file: List<MultipartBody.Part>
    )

    @GET("/manual/getDoorCutOff")
    suspend fun getDoorCutOff(
    ): DoorValue

    @GET("/manual/getDoorState")
    suspend fun getDoorState(
    ): DoorState

    @POST("/manual/setDoorCutOff")
    suspend fun setDoorCutOff(
        @Body doorValue: DoorValue
    )

    @POST("/manual/setActionDoor")
    suspend fun setActionDoor(
        @Body doorState: DoorState
    )

    @GET("/manual/getPhotoEvery")
    suspend fun getPhotoEvery(
    ): PhotoEvery

    @POST("/manual/setPhotoEvery")
    suspend fun setPhotoEvery(
        @Body doorValue: PhotoEvery
    )

    @POST("si/updateModel")
    suspend fun updateModel(
    )
}


