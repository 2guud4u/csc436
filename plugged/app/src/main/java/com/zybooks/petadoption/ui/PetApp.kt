package com.zybooks.petadoption.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable


//sealed class Routes {
//   @Serializable
//   data object Start
//
//   @Serializable
//   data class Connect(
//      val role: String
//   )
//
//   @Serializable
//   data class Interact(
//      val classCode: String,
//      val role: String
//   )
//
//   @Serializable
//   data class Adopt(
//      val petId: Int
//   )
//}


@Composable
fun PluggedApp(ipAddress: String) {
   val navController = rememberNavController()

//   NavHost(
//      navController = navController,
//      startDestination = Routes.Start
//   ) {
//      composable<Routes.Start> {
//         StartScreen(
//            onRoleSelected = {
//               role -> navController.navigate(
//                  Routes.Connect(role)
//               )
//            }
//         )
//      }
//
//      composable<Routes.Connect> { backstackEntry ->
//         val details: Routes.Connect = backstackEntry.toRoute()
//
//         ConnectScreen(
//            role = details.role,
//
//            onUpClick = {
//               navController.navigateUp()
//            },
//
//            onConnect = {
//                  classCode -> navController.navigate(
//               Routes.Interact(classCode, details.role)
//               )
//            }
//         )
//      }
//
//      composable<Routes.Interact> { backstackEntry ->
//         val details: Routes.Interact = backstackEntry.toRoute()
//
//
//         InteractScreen(
//            onUpClick = {
//               navController.navigateUp()
//            },
//            classCode = details.classCode,
//            role=details.role
//         )
//
//
//      }
//   }
}

//Socket functionality
//@Composable
//fun InteractScreen(
//   connectionViewModel: ConnectionViewModel = viewModel(),
//   onUpClick: () -> Unit = { },
//   classCode: String,
//   role: String
//) {
//   var presses by remember { mutableIntStateOf(0) }
//
//   Scaffold(
//      topBar = {
//         PluggedTopBar(
//            canNavigateBack = true,
//            onUpClick = onUpClick
//         )
//      },
//      bottomBar = {
//         PluggedBottomBar()
//      },
//      floatingActionButton = {
//         FloatingActionButton(onClick = { presses++ }) {
//            Icon(Icons.Default.Add, contentDescription = "Add")
//         }
//      }
//   ) { innerPadding ->
//      Box(
//         modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.primaryContainer)
//            .padding(innerPadding)
//      ) {
//         // Align InteractTopBar to the top-center
//         InteractTopBar(classCode = classCode)
//         // Center content in the Box
//         Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center,
//            modifier = Modifier.fillMaxSize().align(Alignment.Center) // Ensure the column fills the available space
//         ) {
//            when(role){
//               "student" -> StudentContent()
//               "teacher" -> TeacherContent()
//
//            }
//         }
//      }
//   }
//}


@Composable
fun InteractTopBar(
   classCode: String,
   count: Int = 0,
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
               text = "Class IP",
               style = MaterialTheme.typography.titleSmall, // Making it small
            )
            Text(
               text = classCode,
               style = MaterialTheme.typography.titleLarge,
            )

         }
         PeopleCount(count)
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
               Column(
                  horizontalAlignment = Alignment.CenterHorizontally
               ) {
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
                     colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),

                  ) {
                     Text("Connect!")
                  }
               }


            }

            "teacher" -> {
               Column(
                  horizontalAlignment = Alignment.CenterHorizontally
               ){
                  ItemWithCaption(
                     caption="Session Id",
                     item = {
                        Row( verticalAlignment = Alignment.CenterVertically ) {
                           Text("123343")
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
}

@Composable
fun StartScreen(
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










