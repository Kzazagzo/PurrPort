package pl.put.swWrotaDlaKota.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.put.swWrotaDlaKota.ui.components.viewModels.ApiState
import pl.put.swWrotaDlaKota.ui.components.viewModels.MainViewModel


@Composable
fun ConfigurationSceen(viewModel: MainViewModel, modifier: Modifier){
    val connected by viewModel.connectedStateResponse.collectAsState()

    LaunchedEffect(Unit){
        viewModel.fetchConnectedState()
    }

    when(connected){
        is ApiState.Error ->{
            ApiErrorScreen(viewModel = viewModel,
                (connected as ApiState.Error).msg)
        }

        is ApiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ){
                CircularProgressIndicator()
            }
        }
        is ApiState.Success -> {

            MenuScreen(
                modifier = modifier,
                viewModel = viewModel
            )
        }
    }



}

