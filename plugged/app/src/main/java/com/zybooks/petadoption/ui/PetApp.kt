package com.zybooks.petadoption.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.zybooks.petadoption.data.Pet
import com.zybooks.petadoption.data.PetDataSource
import com.zybooks.petadoption.data.PetGender
import com.zybooks.petadoption.ui.theme.PetAdoptionTheme
import kotlinx.serialization.Serializable
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person

import androidx.compose.ui.graphics.Color

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextField

sealed class Routes {
   @Serializable
   data object Start

   @Serializable
   data class Connect(
      val role: String
   )

   @Serializable
   data class Interact(
      val classCode: String,
      val role: String
   )

   @Serializable
   data class Adopt(
      val petId: Int
   )
}


@Composable
fun PluggedApp() {
   val navController = rememberNavController()

   NavHost(
      navController = navController,
      startDestination = Routes.Start
   ) {
      composable<Routes.Start> {
//         ListScreen(
//            onImageClick = { pet ->
//               navController.navigate(
//                  Routes.Detail(pet.id)
//               )
//            }
//         )
         StartScreen(
            onRoleSelected = {
               role -> navController.navigate(
                  Routes.Connect(role)
               )
            }
         )
      }

      composable<Routes.Connect> { backstackEntry ->
         val details: Routes.Connect = backstackEntry.toRoute()

         ConnectScreen(
            role = details.role,

            onUpClick = {
               navController.navigateUp()
            },

            onConnect = {
                  classCode -> navController.navigate(
               Routes.Interact(classCode, details.role)
               )
            }
         )
      }

      composable<Routes.Interact> { backstackEntry ->
         val details: Routes.Interact = backstackEntry.toRoute()


         InteractScreen(
            onUpClick = {
               navController.navigateUp()
            },
            classCode = details.classCode,
            role=details.role
         )


      }

      composable<Routes.Adopt> { backstackEntry ->
         val adopt: Routes.Adopt = backstackEntry.toRoute()

         AdoptScreen(
            petId = adopt.petId,
            onUpClick = {
               navController.navigateUp()
            }
         )
      }
   }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetAppBar(
   title: String,
   modifier: Modifier = Modifier,
   canNavigateBack: Boolean = false,
   onUpClick: () -> Unit = { },

   ) {
   TopAppBar(
      title = { Text(title) },
      colors = TopAppBarDefaults.topAppBarColors(
         containerColor = MaterialTheme.colorScheme.primaryContainer
      ),
      modifier = modifier,
      navigationIcon = {
         if (canNavigateBack) {
            IconButton(onClick = onUpClick) {
               Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
         }
      }

   )
}

@Composable
fun InteractScreen(
   connectionViewModel: ConnectionViewModel = viewModel(),
   onUpClick: () -> Unit = { },
   classCode: String,
   role: String
) {
   var presses by remember { mutableIntStateOf(0) }

   Scaffold(
      topBar = {
         PluggedTopBar(
            canNavigateBack = true,
            onUpClick = onUpClick
         )
      },
      bottomBar = {
         PluggedBottomBar()
      },
      floatingActionButton = {
         FloatingActionButton(onClick = { presses++ }) {
            Icon(Icons.Default.Add, contentDescription = "Add")
         }
      }
   ) { innerPadding ->
      Box(
         modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(innerPadding)
      ) {
         // Align InteractTopBar to the top-center
         InteractTopBar(classCode = classCode)
         // Center content in the Box
         Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().align(Alignment.Center) // Ensure the column fills the available space
         ) {
            when(role){
               "student" -> StudentContent()
               "teacher" -> TeacherContent()

            }
         }
      }
   }
}
@Composable
fun StudentContent(){
   Column() {
      GenericTextInput(
          text = "",
          onTextChange = {},
          labelText = "Question",
          buttonText = "Ask!",
          onButtonClick = {},
      )
      GenericTextInput(
         text = "",
         onTextChange = {},
         labelText = "Feedback",
         buttonText = "Send!",
         onButtonClick = {},
      )
      HorizontalDivider(
         modifier = Modifier.padding(horizontal = 16.dp), // Adds padding on the sides
         thickness = 2.dp, // Thicker line
         color = Color.Gray // Custom color
      )
      Text("Quick Feedback", fontSize = MaterialTheme.typography.titleLarge.fontSize,)
      Row() {
         Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            ) {
            Text("Slow Down Please")
         }
         Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
         ) {
            Text("I Am Confused")
         }
      }
   }
}
@Composable
fun TeacherContent(){
   val textInput = ""

   ItemWithCaption(
      caption = "Class Code",
      item = {
         TextField(
            value = textInput,
            onValueChange = {},
            singleLine = true,
            keyboardOptions = KeyboardOptions(
               keyboardType = KeyboardType.Number),
         )
      }
   )

   Button(
      onClick = {},
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
   ) {
      Text("Connect!")
   }

}
@Composable
fun InteractTopBar(
   classCode: String,

   ) {
   Column(modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)

   ) {
      Row(modifier = Modifier
         .fillMaxWidth()
         .padding(vertical = 8.dp)
         ,horizontalArrangement = Arrangement.SpaceBetween,) {
         Column() {
            Text(
               text = "Class Code",
               style = MaterialTheme.typography.titleSmall, // Making it small
            )
            Text(
               text = classCode,
               style = MaterialTheme.typography.titleLarge,
            )

         }
         GenericAvatar()
      }
      HorizontalDivider(
         modifier = Modifier.padding(horizontal = 16.dp), // Adds padding on the sides
         thickness = 2.dp, // Thicker line
         color = Color.Gray // Custom color
      )

   }

}
@Composable
fun ConnectScreen(
   connectionViewModel: ConnectionViewModel = viewModel(),
   onUpClick: () -> Unit = { },
   role: String,
   onConnect: (String) -> Unit
) {
   var presses by remember { mutableIntStateOf(0) }


   Scaffold(
      topBar = {
         PluggedTopBar(
            canNavigateBack = true,
            onUpClick = onUpClick
         )      },
      bottomBar = {
         PluggedBottomBar()

      },
      floatingActionButton = {
         FloatingActionButton(onClick = { presses++ }) {
            Icon(Icons.Default.Add, contentDescription = "Add")
         }
      }
   ) { innerPadding ->
      Box(
         modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(innerPadding)
         ,
         contentAlignment = Alignment.Center
      ) {
         Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
         ) {

         }
         when (role) {
            "student" -> {
               ItemWithCaption(
                  caption = "Class Code",
                  item = {
                     TextField(
                        value = "123",
                        onValueChange = {},
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                           keyboardType = KeyboardType.Number),
                     )
                  }
               )
               Button(
                  onClick = {onConnect("123")},
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
               ) {
                  Text("Connect!")
               }

            }

            "teacher" -> {
               ItemWithCaption(
                  caption="Session Id",
                  item = {
                     Row( verticalAlignment = Alignment.CenterVertically ) {
                        Text("123")
                        RefreshButton(onRefresh = {})
                     }
                  }
               )

               Button(
                  onClick = {onConnect("123")},
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
               ) {
                  Text("Start!")
               }
            }
         }

      }
   }
}

@Composable
fun StartScreen(
   connectionViewModel: ConnectionViewModel = viewModel(),
   onRoleSelected: (String) -> Unit
) {
   var presses by remember { mutableIntStateOf(0) }


   Scaffold(
      topBar = {
         PluggedTopBar()},
      bottomBar = {
         PluggedBottomBar()

      },
      floatingActionButton = {
         FloatingActionButton(onClick = { presses++ }) {
            Icon(Icons.Default.Add, contentDescription = "Add")
         }
      }
   ) { innerPadding ->
      Box(
         modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(innerPadding)
            ,
         contentAlignment = Alignment.Center
      ) {
         Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
         ) {
            Text("You are a")
            Button(onClick = {onRoleSelected("teacher")},
               colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
               Text("Teacher!")
            }
            Button(onClick = {onRoleSelected("student")},
               colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
               Text("Student!")
            }
         }


      }
   }
}

@Composable
fun TeacherPrompt(serverId: String, onRefresh:()->Unit){

   ItemWithCaption(
      caption="Session Id",
      item = {
         Row( verticalAlignment = Alignment.CenterVertically ) {
            Text(serverId)
            RefreshButton(onRefresh = onRefresh)
         }
      }
   )

   Button(
      onClick = {},
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
   ) {
      Text("Start!")
   }
//   RedoRoleButton()
}

@Composable
fun StudentPrompt(textInput: String, onValueChange: (String)-> Unit,){
   ItemWithCaption(
      caption = "Class Code",
      item = {
         TextField(
            value = textInput,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
               keyboardType = KeyboardType.Number),
         )
      }
   )
   Button(
      onClick = {},
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
   ) {
      Text("Connect!")
   }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluggedTopBar(
   canNavigateBack: Boolean = false,
   onUpClick: () -> Unit = { },
){
   CenterAlignedTopAppBar(
      title = { Text("Plugged", style = MaterialTheme.typography.titleLarge) },
      navigationIcon = {

         if (canNavigateBack) {
            IconButton(onClick = onUpClick) {
               Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
         } else{
            IconButton(onClick = { /* Handle menu */ }) {
               Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
         }
      },
      actions = {
         IconButton(onClick = { /* Handle settings */ }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
         }
      },
      colors = TopAppBarDefaults.smallTopAppBarColors(
         containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
         titleContentColor = Color.Black,
         navigationIconContentColor = Color.Black,
         actionIconContentColor = Color.Black
      )
   )
}

@Composable
fun PluggedBottomBar(){
   BottomAppBar(
      containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
      contentColor = MaterialTheme.colorScheme.primary,
   ) {
      Row(
         horizontalArrangement = Arrangement.Center, // Center content horizontally
         modifier = Modifier.fillMaxWidth(), // Ensure the Row takes up the full width
         verticalAlignment = Alignment.CenterVertically // Ensure vertical alignment
      ){
         Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Person Icon",
            modifier = Modifier.size(40.dp), // Size of the icon
            tint = Color.Black // You can change the color here
         )
         Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Person Icon",
            modifier = Modifier.size(40.dp), // Size of the icon
            tint = Color.Black // You can change the color here
         )
      }


      // History Icon

   }
}
@Composable
fun ListScreen(
   viewModel: ListViewModel = viewModel(),
   onImageClick: (Pet) -> Unit,
   modifier: Modifier = Modifier
) {
   Scaffold(
      topBar = {
         PetAppBar(
            title = "Find a Friend"
         )
      }
   ) { innerPadding ->
      LazyVerticalGrid(
         columns = GridCells.Adaptive(minSize = 128.dp),
         contentPadding = PaddingValues(0.dp),
         modifier = modifier.padding(innerPadding)
      ) {
         items(viewModel.petList) { pet ->
            Image(
               painter = painterResource(id = pet.imageId),
               contentDescription = "${pet.type} ${pet.gender}",
               modifier = Modifier.clickable(
                  onClick = { onImageClick(pet) },
                  onClickLabel = "Select the pet"
               )
            )
         }
      }
   }
}

@Preview
@Composable
fun PreviewListScreen() {
   PetAdoptionTheme {
      ListScreen(
//         petList = PetDataSource().loadPets(),
         onImageClick = { }
      )
   }
}

@Composable
fun DetailScreen(
   viewModel: DetailViewModel = viewModel(),
   onAdoptClick: () -> Unit,
   modifier: Modifier = Modifier,
   onUpClick: () -> Unit = { },
   petId: Int,
) {
   val pet = viewModel.getPet(petId)
   val gender = if (pet.gender === PetGender.MALE) "Male" else "Female"

   Scaffold(
      topBar = {
         PetAppBar(
            title = "Details",
            canNavigateBack = true,
            onUpClick = onUpClick

         )
      }
   ) { innerPadding ->
      Column(
         modifier = modifier.padding(innerPadding)
      ) {
         Image(
            painter = painterResource(pet.imageId),
            contentDescription = pet.name,
            contentScale = ContentScale.FillWidth
         )
         Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = modifier.padding(6.dp)
         ) {
            Row(
               horizontalArrangement = Arrangement.SpaceBetween,
               verticalAlignment = Alignment.CenterVertically,
               modifier = modifier.fillMaxWidth()
            ) {
               Text(
                  text = pet.name,
                  style = MaterialTheme.typography.headlineMedium
               )
               Button(onClick = onAdoptClick) {
                  Text("Adopt Me!")
               }
            }
            Text(
               text = "Gender: $gender",
               style = MaterialTheme.typography.bodyLarge
            )
            Text(
               text = "Age: ${pet.age}",
               style = MaterialTheme.typography.bodyLarge
            )
            Text(
               text = pet.description,
               style = MaterialTheme.typography.bodyMedium
            )
         }
      }
   }
}

@Preview
@Composable
fun PreviewDetailScreen() {
   val pet = PetDataSource().loadPets()[0]
   PetAdoptionTheme {
      DetailScreen(
         petId = pet.id,
         onAdoptClick = { }
      )
   }
}

@Composable
fun AdoptScreen(
   petId: Int,
   modifier: Modifier = Modifier,
   onUpClick: () -> Unit = { },
   viewModel: AdoptViewModel = viewModel(),
) {
   val pet = viewModel.getPet(petId)
   val context = LocalContext.current
   Scaffold(
      topBar = {
         PetAppBar(
            title = "Thank You!",
            canNavigateBack = true,
            onUpClick = onUpClick

         )
      }
   ) { innerPadding ->
      Column(
         modifier = modifier.padding(innerPadding)
      ) {
         Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
               painter = painterResource(pet.imageId),
               contentDescription = pet.name,
               modifier = modifier.size(150.dp)
            )
            Text(
               text = "Thank you for adopting ${pet.name}!",
               modifier = modifier.padding(horizontal = 28.dp),
               textAlign = TextAlign.Center,
               style = MaterialTheme.typography.headlineLarge,
            )
         }
         Text(
            text = "Please pick up your new family member during business hours.",
            modifier = modifier.padding(6.dp),
         )
         Button(
            onClick = { shareAdoption(context, pet) },
            modifier = modifier.padding(6.dp)
         ) {
            Icon(Icons.Default.Share, null)
            Text("Share", modifier = modifier.padding(start = 8.dp))
         }
      }
   }
}

@Preview
@Composable
fun PreviewAdoptScreen() {
   val pet = PetDataSource().loadPets()[0]
   PetAdoptionTheme {
      AdoptScreen(pet.id)
   }
}

fun shareAdoption(context: Context, pet: Pet) {
   val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, "Meet ${pet.name}!")
      putExtra(Intent.EXTRA_TEXT, "I've adopted ${pet.name}!")
   }

   context.startActivity(
      Intent.createChooser(intent, "Pet Adoption")
   )
}