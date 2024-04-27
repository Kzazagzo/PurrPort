package pl.put.swWrotaDlaKota.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import kotlinx.coroutines.  delay
import plt.put.swWrotaDlaKota.R


private const val loadingTime: Long = 500


@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier, onTimeout: () -> Unit){
    val context = LocalContext.current

    val valOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(Unit){

        delay(loadingTime)
        valOnTimeout()
    }
    Image(
        painterResource(id = R.drawable.ic_loading),
        contentDescription = null,
        modifier
            .fillMaxSize()
            .wrapContentSize()
    )
}