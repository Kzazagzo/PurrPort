package pl.put.swWrotaDlaKota.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideSubcomposition
import com.bumptech.glide.integration.compose.RequestState
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.delay
import pl.put.swWrotaDlaKota.di.ip
import pl.put.swWrotaDlaKota.network.model.DoorValue
import pl.put.swWrotaDlaKota.network.model.PhotoEvery
import pl.put.swWrotaDlaKota.ui.components.viewModels.ApiState
import pl.put.swWrotaDlaKota.ui.components.viewModels.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: MainViewModel
) {

    val photoEvery by viewModel.getPhotoEvery.collectAsState()

    LaunchedEffect(photoEvery){
        while (true){
            delay(1000)
            viewModel.photoEvery()
        }
    }

    val interactionSource: MutableInteractionSource =
        remember { MutableInteractionSource() }


    when(photoEvery){
        is ApiState.Error -> {
            ApiErrorScreen(
                viewModel = viewModel,
                (photoEvery as ApiState.Error).msg
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
            val key = remember { mutableStateOf("${ip.address}manual/takePhoto?reload=${System.nanoTime()}") }
            val keyOld = remember { mutableStateOf("${ip.address}manual/takePhoto?reload=${System.nanoTime()}") }

            Box {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-40).dp)
                ) {
                    LaunchedEffect(true) {
                        while(true) {
                            delay((photoEvery as ApiState.Success<PhotoEvery>).data.value.toLong() * 1000L)
                            keyOld.value = key.value
                            key.value = "${ip.address}manual/takePhoto?reload=${System.nanoTime()}"
                        }
                    }
                    GlideSubcomposition(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                        model = key.value
                    ) {
                        when(state){
                            RequestState.Failure -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    CircularProgressIndicator()
                                }
                        }
                            is RequestState.Loading -> {
                                GlideImage(   modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f),model = keyOld.value, contentDescription = "")
                            }
                            is RequestState.Success -> {
                                GlideImage(   modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f),model = key.value, contentDescription = "")
                            }
                        }
                    }
//                    GlideImage(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .aspectRatio(1f),
//                        model =key.value,
//                        contentDescription = null,
//                        loading = placeholder {  } keyOld.value,
//                        contentScale = ContentScale.Crop,
//
//                    )
                }

                var sliderPosition by remember { mutableFloatStateOf((photoEvery as ApiState.Success<PhotoEvery>).data.value) }
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                    },
                    modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .offset(y = (-80).dp),
                    valueRange = 2.5f..15f,
                    interactionSource = interactionSource,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    thumb = {
                        Label(
                            label = {
                                PlainTooltip(

                                    modifier = Modifier
                                        .requiredSize(60.dp, 25.dp)
                                        .wrapContentWidth()
                                        .offset(x = 5.dp, y = (-4).dp)
                                        .clip(
                                            RoundedCornerShape(
                                                bottomStart = 40.dp,
                                                bottomEnd = 40.dp
                                            )
                                        ),
                                ) {
                                    val roundedEnd =
                                        (sliderPosition * 100.0).roundToInt() / 100.0
                                    Text("${roundedEnd.toString().format(2)} s")
                                }
                            },
                            interactionSource = interactionSource
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp) // square size
                                    .clip(CircleShape)
                            )
                        }

                    },
                )
                OutlinedButton(
                    onClick = {
                        viewModel.setPhotoEvery(sliderPosition)
                    },
                    modifier = Modifier
                        .size(200.dp, 50.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = (-30).dp),
                ) {
                    Text("Wyślij opóźnienie",textAlign = TextAlign.Center)
                }
            }
        }
    }


}