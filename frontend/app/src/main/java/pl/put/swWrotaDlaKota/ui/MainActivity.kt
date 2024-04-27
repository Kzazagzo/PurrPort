package pl.put.swWrotaDlaKota.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.put.swWrotaDlaKota.ui.theme.SwWrotaDlaKotaTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import pl.put.swWrotaDlaKota.ui.components.ConfigurationSceen
import pl.put.swWrotaDlaKota.ui.components.LoadingScreen
import pl.put.swWrotaDlaKota.ui.components.viewModels.MainViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import pl.put.swWrotaDlaKota.ui.components.viewModels.TransitionState


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)


        setContent {
            SwWrotaDlaKotaTheme {

                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.Home.route) {
                    composable(Routes.Home.route){
                        val mainViewModel = hiltViewModel<MainViewModel>()
                        MainScreen(
                            mainViewModel = mainViewModel
                        )
                    }
                }
            }
        }
    }
}

@VisibleForTesting
@Composable
fun MainScreen(
    mainViewModel: MainViewModel
){
    Surface (
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.Start + WindowInsetsSides.End)
        ),
            color = MaterialTheme.colorScheme.background
        ){
        val transitionState = remember {MutableTransitionState(mainViewModel.transitionState.value)}
        val transition = updateTransition(transitionState, label="transitionState")
        val splashAlpha by transition.animateFloat(
            transitionSpec = {tween(durationMillis = 100)}, label="splashAlpha")
            {if(it == TransitionState.Showed)1f else 0f}
        val fadeIn by transition.animateFloat (
            transitionSpec = { tween(durationMillis = 300) }, label = "fadeIn"
        ){if(it == TransitionState.Showed) 0f else 1f}
        Box{
            LoadingScreen(
                modifier = Modifier.alpha(splashAlpha),
                onTimeout = {
                    transitionState.targetState = TransitionState.Completed
                    mainViewModel.transitionState.value = TransitionState.Completed
                }
            )
            MainContent(
                viewModel = mainViewModel,
                modifier = Modifier.alpha(fadeIn)
            )
        }
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp,
    viewModel: MainViewModel
){
    Column(modifier = modifier){
        Spacer(Modifier.padding(top = topPadding))
        ConfigurationSceen(
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

sealed class Routes(val route: String){
    object Home: Routes("home")
}



