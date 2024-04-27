package pl.put.swWrotaDlaKota.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import pl.put.swWrotaDlaKota.di.ip
import pl.put.swWrotaDlaKota.network.model.AnimalName
import pl.put.swWrotaDlaKota.network.model.AnimalPhoto
import pl.put.swWrotaDlaKota.network.model.AnimalPhotoList
import pl.put.swWrotaDlaKota.ui.components.viewModels.ApiState
import pl.put.swWrotaDlaKota.ui.components.viewModels.MainViewModel
import plt.put.swWrotaDlaKota.R
import java.io.ByteArrayOutputStream

@Composable
fun AnimalDialog(
    viewModel: MainViewModel,
    animalName: String, animalPhoto: String,
    openAlertDialog: MutableState<String?>
) {

    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_delete_24),
                'a'.toString()
            )
        },
        title = { Text(text = "Czy jesteś pewny?") },
        text = { Text(text = "Czy na pewno chcesz usunąć zdjęcie?") },
        onDismissRequest = { openAlertDialog.value = null },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.popAnimalPhoto(AnimalPhoto(animalPhoto))
                    openAlertDialog.value = null
                }
            ) {
                Text("Usuń")
            }

        },
        dismissButton = {
            TextButton(
                onClick = {
                    openAlertDialog.value = null
                }
            ) {
                Text("Anuluj")
            }
        })
}


@Composable
fun AddPhotoDialog(
    viewModel: MainViewModel,
    animalName: String,
    openAddDialog: MutableState<Boolean>
) {
    val context = LocalContext.current

    val selectedImage = remember { mutableStateListOf<Uri?>(null) }
    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { it ->
            selectedImage.apply {
                clear()
                addAll(it)
            }
            val fileName = it.map {
                DocumentFile.fromSingleUri(context, it)!!.name
            }

            val files = it.map {
                val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                val bytes = byteArrayOutputStream.toByteArray()

                bytes
            }


            viewModel.postAnimalsPhotos(animalName, fileName, files)
            openAddDialog.value = false
        }
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_add_photo_alternate_24),
                'a'.toString()
            )
        },
        title = { Text(text = "Dodawanie zdjęć") },
        text = {
            Text(
                text = "Pamiętaj, aby dodane zdjęcia były ostre i przedstawiały przede wszystkim " +
                        "głowę zwierzęcia w różnych rozmiarach (tj. zdjęcia z różnych perspektyw). " +
                        "Minimum do poprawnego działania to ok. 15 zdjęć, w celu zwiększenia efektywności należy dodać więcej zdjęć."
            )
        },
        onDismissRequest = { openAddDialog.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    galleryLauncher.launch("image/*")
                }
            ) {
                Text("Przejdź do dodania zdjeć")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    openAddDialog.value = false
                }
            ) {
                Text("Anuluj")
            }
        })
}


@OptIn(
    ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun AnimalScreen(
    navController: NavController,
    viewModel: MainViewModel,
    animalName: String,
) {
    val openAddDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val photosState by viewModel.animalPhotos.collectAsState()
    val poped by viewModel.popAnimalPhotosPassed.collectAsState()
    val posted by viewModel.addPhotosPassed.collectAsState()




    LaunchedEffect(photosState,poped,posted) {
        viewModel.fetchAnimalsPhotos(AnimalName(animalName))
        viewModel.clear()
    }

    if (openAddDialog.value) {
        AddPhotoDialog(viewModel, animalName, openAddDialog)
    }

    when (photosState) {
        is ApiState.Error-> {
            ApiErrorScreen(
                viewModel = viewModel,
                (photosState as ApiState.Error).msg
            )
        }

        is ApiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is ApiState.Success -> {
            Scaffold(
                bottomBar = { Box(Modifier) },
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .background(color = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text(
                            text = animalName,
                            modifier = Modifier
                                .padding(5.dp)
                                .align(Alignment.BottomStart),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                        onClick = {
                            openAddDialog.value = true
                        },
                    ) {
                        Icon(Icons.Filled.Add, "Floating action button.")
                    }
                }
            ) { innerPadding ->
                val openAlertDialog = remember { mutableStateOf<String?>(null) }
                val selectedPhoto = remember { mutableStateOf<String?>(null) }

                if (openAlertDialog.value != null) {
                    AnimalDialog(
                        viewModel,
                        animalName = animalName,
                        animalPhoto = openAlertDialog.value!!,
                        openAlertDialog
                    )
                }

                when (photosState) {

                    is ApiState.Error -> {
                        ApiErrorScreen(
                            viewModel = viewModel,
                            (photosState as ApiState.Error).msg
                        )
                    }

                    is ApiState.Loading -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is ApiState.Success -> {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.padding(innerPadding),
                            content = {
                                (photosState as ApiState.Success<AnimalPhotoList>).data.animalPhoto.forEach { photo ->
                                        if (!viewModel.isLoading) {
                                    item {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .aspectRatio(1f)
                                                    .combinedClickable(
                                                        onClick = {
                                                            selectedPhoto.value =
                                                                "${ip.address}animal/getPhoto?path=$photo"
                                                        },
                                                        onLongClick = {
                                                            openAlertDialog.value = photo
                                                        }
                                                    ),
                                                model = "${ip.address}animal/getPhoto?path=$photo"
                                                        ,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            })

                    }
                }

            }

        }
    }

}



