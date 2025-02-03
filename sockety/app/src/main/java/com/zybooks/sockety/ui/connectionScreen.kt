package com.zybooks.sockety.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Person
import com.zybooks.sockety.ui.ConnectionViewModel
import androidx.compose.material3.TextButton
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.filled.Settings


@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ConnectionScreen(
    connectionViewModel: ConnectionViewModel = viewModel()
) {
    var presses by remember { mutableIntStateOf(0) }


    Scaffold(
        topBar = {
            ConnectionTopBar()
        },
        bottomBar = {
            ConnectionBottomBar()

        },
        floatingActionButton = {
            FloatingActionButton(onClick = { presses++ }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (connectionViewModel.role) {
                    "student" -> {
                        StudentPrompt(connectionViewModel.inputId, {it -> connectionViewModel.inputId = it})

                    }

                    "teacher" -> {
                        TeacherPrompt(connectionViewModel.serverId, {connectionViewModel.serverId = connectionViewModel.generate6DigitUUID()} )
                    }

                    else -> {
                        StartPrompt(onRoleSelected = { newRole -> connectionViewModel.role = newRole;
                            connectionViewModel.serverId = connectionViewModel.generate6DigitUUID() })
                    }
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionTopBar(){
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text("Top app bar")
        }
    )
}

@Composable
fun ConnectionBottomBar(){
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
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
    ) {
        Text("Start!")
    }
    RedoRoleButton()
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
    ) {
        Text("Connect!")
    }
    RedoRoleButton()
}

@Composable
fun StartPrompt(onRoleSelected: (String) -> Unit){
    Text("You are a")
    Button(onClick = {onRoleSelected("teacher")}) {
        Text("Teacher!")
    }
    Button(onClick = {onRoleSelected("student")}) {
        Text("Student!")
    }
}

@Composable
fun RedoRoleButton(
    connectionViewModel: ConnectionViewModel = viewModel()
){
    TextButton(onClick = {connectionViewModel.role = ""}) {
        Text("<- Back")
    }
}


