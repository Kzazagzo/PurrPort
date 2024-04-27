package pl.put.swWrotaDlaKota.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import pl.put.swWrotaDlaKota.di.ip
import pl.put.swWrotaDlaKota.network.model.AnimalName
import pl.put.swWrotaDlaKota.network.model.Animal
import pl.put.swWrotaDlaKota.network.model.AnimalPhoto
import pl.put.swWrotaDlaKota.ui.components.viewModels.ApiState
import pl.put.swWrotaDlaKota.ui.components.viewModels.MainViewModel
import plt.put.swWrotaDlaKota.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier,
    viewModel: MainViewModel,
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            KotNavigationBar(
                navController = navController
            )
        },
    ) { innerPadding ->

        NavHost(
            navController = navController, startDestination = TabItem.Animal.navGraphRoute,
            Modifier.padding(innerPadding)
        ) {
            composable(TabItem.Animal.navGraphRoute) { Animal(navController, viewModel = viewModel) }
            composable(TabItem.Door.navGraphRoute) { DoorScreen(navController,viewModel) }
            composable(TabItem.Camera.navGraphRoute) { CameraScreen(viewModel) }
            composable(TabItem.AnimalView.navGraphRoute + "/{animalName}",
                arguments = listOf(
                    navArgument("animalName"){type = NavType.StringType}))
                    //navArgument("photos"){type = NavType.StringArrayType}x
            {
                AnimalScreen(navController, viewModel = viewModel,
                    animalName = it.arguments?.getString("animalName")!!)
            }
        }
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {


        }

    }

}

internal enum class TabItem(
    val title: String,
    val navGraphRoute: String,
) {
    Animal("Zwierzęta", "animal_graph"),
    Door("Drzwiczki", "door_graph"),
    Camera("Kamera", "camera_graph"),
    AnimalView("Zwierze","animal_view"),
    PhotoView("Zdjęcie", "photo_view")
}

@Composable
internal fun KotNavigationBar(
    navController: NavController
) {
    val bottomBarItems = listOf(TabItem.Animal, TabItem.Door, TabItem.Camera)
    val bottomBarImages = listOf<Int>(R.drawable.cat_svgrepo_com, R.drawable.baseline_door_back_24, R.drawable.baseline_camera_outdoor_24, R.drawable.baseline_text_snippet_24)
    val currentTopLevelDestination by navController.currentTabItemAsState()

    NavigationBar {
        bottomBarItems.forEachIndexed  { index,item ->

            val isTabAlreadySelected = item == currentTopLevelDestination
            NavigationBarItem(
                icon = { Icon(painter = painterResource(bottomBarImages[index]), contentDescription = null) },
                label = { Text(item.title) },
                selected = isTabAlreadySelected,
                onClick = {
                    navController.navigate(item.navGraphRoute) {
                        launchSingleTop = true
                        restoreState = true

                    }
                }

            )
        }
    }
}


@Composable
private fun NavController.currentTabItemAsState(): State<TabItem> {
    val selectedItem = remember { mutableStateOf(TabItem.Animal) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.hierarchy.any { it.route == TabItem.Animal.navGraphRoute } -> {
                    selectedItem.value = TabItem.Animal
                }

                destination.hierarchy.any { it.route == TabItem.Door.navGraphRoute } -> {
                    selectedItem.value = TabItem.Door
                }

                destination.hierarchy.any { it.route == TabItem.Camera.navGraphRoute } -> {
                    selectedItem.value = TabItem.Camera
                }


            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun Animal(
    navController: NavController,
    viewModel: MainViewModel
) {
    val connectedAnimal by viewModel.animalsResponse.collectAsState()
    val animalAdded by viewModel.addAnimalPassed.collectAsState()
    val openDialog = remember { mutableStateOf(false) }
    val openDialogSi = remember {mutableStateOf(false)    }

    LaunchedEffect(connectedAnimal, animalAdded) {
        viewModel.fetchAnimalsList()
        viewModel.clear()
    }

    if(openDialog.value){
        AddAnimalDialog(viewModel, openDialog)
    }

    if(openDialogSi.value){
        SiDialog(viewModel = viewModel, openAlertDialog = openDialogSi)
    }

    when (connectedAnimal) {
        is ApiState.Error -> {
            ApiErrorScreen(
                viewModel = viewModel,
                (connectedAnimal as ApiState.Error).msg
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
            val showDeleteDialog = remember { mutableStateOf("")}

            if(showDeleteDialog.value != ""){
                DeleteAnimalDialog(viewModel,showDeleteDialog)
            }

            Scaffold(
                topBar = {Box(Modifier)},
                bottomBar = {
                    Row(modifier = Modifier.padding(20.dp)){
                        ExtendedFloatingActionButton(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                            onClick = { openDialogSi.value = true},
                        ) {
                            Icon(Icons.Filled.Edit, "Zaktualizuj model SI")
                            Text(text = "      Zaktualizuj model SI")
                        }
                        Spacer(Modifier.weight(1f))
                        FloatingActionButton(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                            onClick = { openDialog.value = true},
                        ) {
                            Icon(Icons.Filled.Add, "Floating action button.")
                        }
                    }
},

            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    (connectedAnimal as ApiState.Success<List<Animal>>).data.forEach { animal ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                                .background(color = MaterialTheme.colorScheme.surface)
                                .combinedClickable(
                                    onClick = { navController.navigate("${TabItem.AnimalView.navGraphRoute}/${animal.name}") },
                                    onLongClick = { showDeleteDialog.value = animal.name }
                                )
                                .border(
                                    2.dp,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clip(RoundedCornerShape(20.dp))

                        ) {
                            Scaffold(
                                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                topBar = {Box(Modifier)}, // Jakoś magicznie wtedy jest puste
                                bottomBar = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(30.dp)
                                            .background(color = MaterialTheme.colorScheme.tertiary)
                                            .align(Alignment.End)
                                    ) {
                                        Text(
                                            text = animal.name,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .align(Alignment.BottomCenter),
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                    }
                                }
                            ) {innerPadding ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    animal.photo.take(animal.photo.size.coerceAtMost(4)).forEach { photo ->
                                        AsyncImage(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            model = ip.address + "animal/getPhoto?path=" + photo,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop

                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DeleteAnimalDialog(
    viewModel: MainViewModel,
    openAlertDialog: MutableState<String>
){
    AlertDialog(
        icon = {Icon(painter = painterResource(R.drawable.baseline_delete_24),
            'a'.toString()
        )},
        title = {Text(text = "Czy jesteś pewny?")},
        text = {Text(text="Czy na pewno chcesz usunąć zwierze?")},
        onDismissRequest = { openAlertDialog.value = "" },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.popAnimal(AnimalName(openAlertDialog.value))
                    viewModel.fetchAnimalsList()
                    openAlertDialog.value = ""
                }
            ) {
                Text("Usuń")
            }

        },
        dismissButton = {
            TextButton(
                onClick = {
                    openAlertDialog.value = ""
                }
            ) {
                Text("Anuluj")
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalDialog(
    viewModel: MainViewModel,
    openAddDialog: MutableState<Boolean>
){
    var animalName = remember { mutableStateOf("") }

    AlertDialog(
        icon = {Icon(painter = painterResource(R.drawable.baseline_add_box_24),
            'a'.toString()
        )},
        title = {Text(text = "Dodawanie zwierzęcia")},
        text = {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = animalName.value,
                onValueChange = { newText: String ->
                    animalName.value = newText },
                label = {Text("Podaj imie zwierzęcia")
                }
            )},
        onDismissRequest = { openAddDialog.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.addAnimal(AnimalName(animalName.value))
                    openAddDialog.value = false
                }
            ) {
                Text("Utwórz")
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

@Composable
fun SiDialog(
    viewModel: MainViewModel,
    openAlertDialog: MutableState<Boolean>
) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_precision_manufacturing_24),
                'a'.toString()
            )
        },
        title = { Text(text = "Zaktualizuj model SI") },
        text = { Text(text = "Do wykrywania zwierzątek aplikacja używa sztucznej inteligencji. \n\n" +
                "Po każdorazowym dodaniu nowych zdjęć lub ich usunięciu, należy zaktulizować model. " +
                "Proces ten odbywa się na zewnętrznym serwerze i może okazać się długotrwały - zależy od ilości zdjęć zwierzaków" +
                " (ok. 5 - 30 minut). \n\nAplikacja do tego czasu będzie działała na starym modelu, aż do aktualizacji danych z serwera. ") },
        onDismissRequest = { openAlertDialog.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.updateModel()
                    openAlertDialog.value = false
                }
            ) {
                Text("Zaktualizuj model")
            }

        },
        dismissButton = {
            TextButton(
                onClick = {
                    openAlertDialog.value = false
                }
            ) {
                Text("Anuluj")
            }
        })
}