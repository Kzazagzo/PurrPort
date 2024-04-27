package pl.put.swWrotaDlaKota.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.put.swWrotaDlaKota.ui.components.viewModels.ApiState
import pl.put.swWrotaDlaKota.ui.components.viewModels.MainViewModel

@Composable
fun ApiErrorScreen(viewModel : MainViewModel,
                   error : String?){
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(all = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(text = "Aplikacja wymaga połączenia z serwerem",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)


        Text(text = "Upewnij się że serwer jest włączony.. bo nie jest",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)


        Text(text = "Status serwera:",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)

        Text(text = "DALEJ NIE DZIAŁA",
            color = MaterialTheme.colorScheme.secondary
            ,textAlign = TextAlign.Center)

        Button(onClick = {viewModel.fetchConnectedState()},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(text = "Odśwież",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(5.dp))
        }

        Text(
            text = "$error",
            textAlign = TextAlign.Center
        )

    }
}