package pl.put.swWrotaDlaKota.ui.components.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pl.put.swWrotaDlaKota.di.DefaultDispatcher
import pl.put.swWrotaDlaKota.network.KotApi
import pl.put.swWrotaDlaKota.network.model.AnimalName
import pl.put.swWrotaDlaKota.network.model.Animal
import pl.put.swWrotaDlaKota.network.model.ConnectedState
import pl.put.swWrotaDlaKota.network.model.AnimalPhoto
import pl.put.swWrotaDlaKota.network.model.AnimalPhotoList
import pl.put.swWrotaDlaKota.network.model.DoorState
import pl.put.swWrotaDlaKota.network.model.DoorValue
import pl.put.swWrotaDlaKota.network.model.PhotoEvery
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class MainViewModel @Inject constructor(
    private val kotApi: KotApi,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel(){
    var isLoading: Boolean = false

    var stateChanging: Boolean = false
    val transitionState = mutableStateOf(TransitionState.Showed)

    private val _connected = MutableStateFlow<ApiState<ConnectedState>>(ApiState.Loading)
    val connectedStateResponse : StateFlow<ApiState<ConnectedState>> = _connected

    private val _animals = MutableStateFlow<ApiState<List<Animal>>>(ApiState.Loading)
    val animalsResponse: StateFlow<ApiState<List<Animal>>> = _animals

    private val _animalsPhotos = MutableStateFlow<ApiState<AnimalPhotoList>>(ApiState.Loading)
    val animalPhotos: StateFlow<ApiState<AnimalPhotoList>> = _animalsPhotos

    // TODO TOSTY
    private val _addPhotosPassed = MutableStateFlow<ApiState<Boolean>>(ApiState.Loading)
        val addPhotosPassed: StateFlow<ApiState<Boolean>> = _addPhotosPassed

    private val _popAnimalPhotosPassed = MutableStateFlow<ApiState<Boolean>>(ApiState.Loading)
    val popAnimalPhotosPassed: StateFlow<ApiState<Boolean>> = _popAnimalPhotosPassed

    private val _addAnimalPassed = MutableStateFlow<ApiState<Boolean>>(ApiState.Loading)
    val addAnimalPassed: StateFlow<ApiState<Boolean>> = _addAnimalPassed


    fun clear(){
        viewModelScope.launch {
            _addPhotosPassed.value = ApiState.Loading
            _popAnimalPhotosPassed.value = ApiState.Loading
            _addAnimalPassed.value = ApiState.Loading
        }
    }
    fun fetchConnectedState() {
        stateChanging = true
        viewModelScope.launch {
            try {
                val response = kotApi.getConnectedState()
                _connected.value = ApiState.Success(response)
                stateChanging = false
            } catch (e: Exception) {
                _connected.value = ApiState.Error(e.message)
            }
        }
    }

    fun fetchAnimalsList() {
        isLoading = true
        viewModelScope.launch {
           try {
                val response = kotApi.getAnimalsList()
                _animals.value = ApiState.Success(response)
               isLoading = false
            } catch (e: Exception) {
                _animals.value = ApiState.Error(e.message)
            }
        }
    }

        fun fetchAnimalsPhotos(animalName: AnimalName) {
            viewModelScope.launch {
                try {
                    val response = kotApi.getAnimalPhotos(animalName)
                    _animalsPhotos.value = ApiState.Success(response)
                } catch (e: Exception) {
                    _animalsPhotos.value = ApiState.Error(e.message)
                }
            }
        }
    // JEZUS MARIA MATKO CHRYSTE, TA FUNKCJA POWINNA PÓJŚĆ DO JAKIEGOŚ EGZORCYSTY
    fun postAnimalsPhotos(animalName: String, fileName: List<String?>, files: List<ByteArray>){
        viewModelScope.launch {
            try{
                val body = files.mapIndexed { index, it ->
                    val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                    MultipartBody.Part.createFormData("file",fileName[index],requestBody)
                }
                kotApi.addAnimalPhoto(animalName,body)
                _addPhotosPassed.value = ApiState.Success(true)
            }
            catch (e : Exception){
                _addPhotosPassed.value = ApiState.Error(e.message)
            }


        }
    }

    fun popAnimalPhoto(photoName: AnimalPhoto){
        viewModelScope.launch {
            try {
                kotApi.popAnimalPhoto(photoName)
                _popAnimalPhotosPassed.value = ApiState.Success(true)
            }
            catch (e : Exception){
                _popAnimalPhotosPassed.value = ApiState.Error(e.message)
            }
        }
    }



    fun addAnimal(animalName: AnimalName){
        viewModelScope.launch {
            try {
                kotApi.addAnimal(animalName)
                _addAnimalPassed.value = ApiState.Success(true)
            }
            catch (e : Exception){
                _addAnimalPassed.value = ApiState.Error(e.message)
            }
        }
    }

    fun popAnimal(animalName: AnimalName){
        viewModelScope.launch {
            try {
                kotApi.popAnimal(animalName)
                _addAnimalPassed.value = ApiState.Success(true)
            }
            catch (e : Exception){
                _addAnimalPassed.value = ApiState.Error(e.message)
            }
        }
    }

    // Door
    private val _getDoorCutOff = MutableStateFlow<ApiState<DoorValue>>(ApiState.Loading)
    val getDoorCutOff : StateFlow<ApiState<DoorValue>> = _getDoorCutOff

    private val _getDoorState = MutableStateFlow<ApiState<DoorState>>(ApiState.Loading)
    val getDoorState : StateFlow<ApiState<DoorState>> = _getDoorState

    fun doorCutOff(){
        viewModelScope.launch {
            try{
                val value = kotApi.getDoorCutOff()
                _getDoorCutOff.value = ApiState.Success(value)
            }
            catch (e: Exception){
                _getDoorCutOff.value = ApiState.Error(e.message)
            }
        }
    }

    fun doorState(){
        viewModelScope.launch {
            try{
                val value = kotApi.getDoorState()
                _getDoorState.value = ApiState.Success(value)
            }
            catch (e: Exception){
                _getDoorState.value = ApiState.Error(e.message)
            }
        }
    }

    fun setDoorCutOff(value: Float){
        viewModelScope.launch {
            kotApi.setDoorCutOff(DoorValue(value))
        }
    }

    fun setDoorState(value: String){
        viewModelScope.launch {
            kotApi.setActionDoor(DoorState(value))
        }
    }

    private val _getPhotoEvery = MutableStateFlow<ApiState<PhotoEvery>>(ApiState.Loading)
    val getPhotoEvery : StateFlow<ApiState<PhotoEvery>> = _getPhotoEvery

    fun setPhotoEvery(value: Float){
        viewModelScope.launch {
            kotApi.setPhotoEvery(PhotoEvery(value))
        }
    }

    fun photoEvery(){
        viewModelScope.launch {
            try {
                val value = kotApi.getPhotoEvery()
                _getPhotoEvery.value = ApiState.Success(value)
            }
            catch (e:Exception){
                _getPhotoEvery.value = ApiState.Error(e.message)
            }
        }
    }

    fun updateModel(){
        viewModelScope.launch {
            kotApi.updateModel()
        }
    }

}

sealed class ApiState<out T>{
    object Loading : ApiState<Nothing>()
    data class Success<T>(val data:T) : ApiState<T>()
    data class Error(val msg: String?) : ApiState<Nothing>()
}

enum class TransitionState {Showed, Completed}