package pl.put.swWrotaDlaKota.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.onFocusedBoundsChanged
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Label
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.delay
import pl.put.swWrotaDlaKota.network.model.AnimalPhotoList
import pl.put.swWrotaDlaKota.network.model.DoorState
import pl.put.swWrotaDlaKota.network.model.DoorValue
import pl.put.swWrotaDlaKota.ui.components.viewModels.ApiState
import pl.put.swWrotaDlaKota.ui.components.viewModels.MainViewModel
import java.util.Timer
import java.util.TimerTask
import java.util.logging.Handler
import kotlin.concurrent.timerTask
import kotlin.io.path.Path
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorScreen(
    navController: NavController,
    viewModel: MainViewModel,
) {
    val doorState by viewModel.getDoorState.collectAsState()
    val doorCutOff by viewModel.getDoorCutOff.collectAsState()

    LaunchedEffect(doorState, doorCutOff) {
        while(true){
            delay(1000)
            viewModel.doorState()
            viewModel.doorCutOff()
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(1f)
                .padding(20.dp)
        ) {


            when(doorCutOff){
                is ApiState.Error -> {
                    ApiErrorScreen(
                        viewModel = viewModel,
                        (doorCutOff as ApiState.Error).msg
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
                    Text(
                        text = "Podaj czas w senkundach jak długo" +
                                " drzwiczki mają pozostać otwarte, po wykryciu zwirzęcia",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    var sliderPosition by remember { mutableFloatStateOf((doorCutOff as ApiState.Success<DoorValue>).data.value) }

                    val interactionSource: MutableInteractionSource =
                        remember { MutableInteractionSource() }

                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                        },
                        modifier =
                        Modifier
                            .scale(1.5f)
                            .offset(0.dp, LocalConfiguration.current.screenHeightDp.dp / 4)
                            .rotate(-90f)
                            .width(1000.dp),
                        valueRange = 1f..10f,
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
                                            .offset(x = (-12).dp, y = (-14).dp)
                                            .clip(RoundedCornerShape(topEnd = 40.dp, bottomEnd = 40.dp)),
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
                            viewModel.setDoorCutOff(sliderPosition)
                        },
                        modifier = Modifier.size(200.dp, 50.dp).align(Alignment.BottomCenter),
                    ) {
                        Text("Wyślij opóźnienie",textAlign = TextAlign.Center)
                    }
                }
            }

        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            val status by viewModel.getDoorState.collectAsState()

            LaunchedEffect(status) {
                viewModel.doorState()
            }

            when (status) {
                is ApiState.Error -> {
                    ApiErrorScreen(
                        viewModel = viewModel,
                        (status as ApiState.Error).msg
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
                    Column ( modifier = Modifier
                        .height(125.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(
                            text = "Zdalne sterowanie drzwiczkami",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Aktualny status",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text =
                                when((status as ApiState.Success<DoorState>).data.state){
                                    "closed" -> ("zamknięte")"opened" -> ("otwarte")"locked" -> ("zablokowane") "unlocked" -> ("odblokowane")
                                    else -> ("otwarte")},
                            textAlign = TextAlign.Center,
                            color =
                                when((status as ApiState.Success<DoorState>).data.state) {
                                    "closed" -> MaterialTheme.colorScheme.onBackground "opened" -> MaterialTheme.colorScheme.onBackground "locked" -> Color.Red "unlocked" -> Color.Green
                                    else -> Color.Black }
                        )

                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.setDoorState("unlocked")
                            },
                            modifier = Modifier.size(200.dp, 100.dp),
                            enabled = (status as ApiState.Success<DoorState>).data.state != "unlocked",
                            colors = ButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Green,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.Gray
                            ),
                        ) {
                            Text("Odblokuj",textAlign = TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                        // Tak wiem to głupie
                            onClick = {
                                when((status as ApiState.Success<DoorState>).data.state){
                                    "locked" -> viewModel.setDoorState("closed") "unlocked" -> viewModel.setDoorState("closed") "opened" ->viewModel.setDoorState("closed")
                                    else -> viewModel.setDoorState("opened")}
                            },
                            modifier = Modifier.size(200.dp, 100.dp),
                            colors = ButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.Gray
                            ),
                        ) {
                            Text(text=
                            when((status as ApiState.Success<DoorState>).data.state){
                                "locked" -> ("Zresetuj") "unlocked" -> ("Zresetuj") "opened" -> "Zamknij"
                                else -> ("Otwórz")},
                                textAlign = TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.setDoorState("locked")
                            },
                            modifier = Modifier.size(200.dp, 100.dp),
                            enabled = (status as ApiState.Success<DoorState>).data.state != "locked",
                            colors = ButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Red,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.Gray
                            ),
                        ) {
                            Text("Zablokuj")
                        }
                    }

                }
            }
        }
    }

}

