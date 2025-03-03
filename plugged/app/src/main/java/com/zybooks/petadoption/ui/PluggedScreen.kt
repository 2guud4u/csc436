package com.zybooks.petadoption.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Send
import kotlinx.coroutines.launch


import java.text.SimpleDateFormat
import java.util.*

sealed class Routes {
    @Serializable
    data object Start

    @Serializable
    data object Connect

    @Serializable
    data object Interact
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluggedScreen(viewModel: PluggedViewModel, ipAddress: String) {
    // Collect state from ViewModel
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val mode by remember { viewModel.connectionMode }
    val serverIp by remember { viewModel.serverIp }
    val port by remember { viewModel.port }

    val isConnected by remember { viewModel.isConnected }
    val logMessages = viewModel.questionsList
    val navController = rememberNavController()

    fun postSnackBar(msg: String){
        scope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    topBar = {
                        PluggedTopBar()},
                    bottomBar = {
                        PluggedBottomBar()

                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { postSnackBar("floaty")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                ) { innerPadding ->
                    // Main content area with flexible height
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(innerPadding)
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.Start
                ) {
                    composable<Routes.Start> {

                    ModeSelectionScreen(
                        viewModel,
                        onSelect={
                            mode ->
                            navController.navigate(
                                Routes.Connect
                            )
                            viewModel.setMode(mode);

                        }

                    )


                    }

                    composable<Routes.Connect> { backstackEntry ->
                        if (mode.isNotEmpty() && !isConnected) {
                            ConnectionSettingsScreen(
                                mode = mode,
                                ipAddress = ipAddress,
                                serverIp = serverIp,
                                port = port,
                                onServerIpChange = { viewModel.serverIp.value = it },
                                onPortChange = { viewModel.port.value = it },
                                onStartServer = { viewModel.startServer(
                                    port, ipAddress,
                                    postSnackBar = {msg -> postSnackBar(msg)}
                                )
                                    navController.navigate(
                                        Routes.Interact
                                        )},
                                onConnectToServer = { viewModel.connectToServer(serverIp, port, postSnackBar = {msg -> postSnackBar(msg)})
                                    navController.navigate(
                                        Routes.Interact
                                    )},
                                onBack = {navController.navigate(
                                    Routes.Start
                                )}


                            )
                        }

                    }
                    composable<Routes.Interact> { backstackEntry ->
                        if(viewModel.isConnected.value){
                            InteractionScreen(viewModel, ipAddress =ipAddress,
                                logMessages,
                                onDisconnect={
                                    navController.navigate(
                                        Routes.Start
                                    )
                                    viewModel.connectionMode.value = ("")
                                }
                            )
                        } else{
                            Column {
                                Text("Connection Failed")
                                Button(
                                    onClick = {navController.navigate(
                                        Routes.Start
                                    )}
                                ) {
                                    Text("Try Again")
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
}
@Composable
fun MessageItem(message: PluggedViewModel.LogMessage, onDeleteClick: ()-> Unit) {

    //only log question
    if (message.type == PluggedViewModel.LogMessage.TYPE_QUESTION){
        NotifCard(
            title = "Question",
            subtitle = "Anon",
            description = message.text,
            imageUrl = "",
            onDeleteClick = onDeleteClick,
            isElevated = false,
        )
    }


}
@Composable
fun ModeSelectionScreen(viewModel: PluggedViewModel, onSelect: (String) -> Unit) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("You are a")
                Button(onClick = {onSelect("server")}
                ,
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("Teacher!")
                }
                Button(onClick = {onSelect("client")},
                    colors = ButtonDefaults.buttonColors()) {
                    Text("Student!")
                }
            }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsScreen(
    mode: String,
    ipAddress: String,
    serverIp: String,
    port: String,
    onServerIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onStartServer: () -> Unit,
    onConnectToServer: () -> Unit,
    onBack: ()-> Unit
) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (mode == "server") "Session Settings" else "Enter Class Info!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (mode == "server") {
                Text(
                    text = "Session Ip: $ipAddress",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = onPortChange,
                    label = { Text("Port") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }

                    Button(
                        onClick = onStartServer
                    ) {
                        Text("Start")
                    }
                }



            } else { // Client mode
                OutlinedTextField(
                    value = serverIp,
                    onValueChange = onServerIpChange,
                    label = { Text("Class IP") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = onPortChange,
                    label = { Text("Port") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onBack
                    ) {
                        Text("Back")
                    }

                    Button(
                        onClick = onConnectToServer
                    ) {
                        Text("Start")
                    }
                }

            }
        }

}

@Composable
fun InteractionScreen(
    viewModel: PluggedViewModel,
    ipAddress: String,
    logMessages: SnapshotStateList<PluggedViewModel.LogMessage>,
    onDisconnect: () -> Unit
){
    Column{
    // Align InteractTopBar to the top-center
        InteractTopBar(classCode = viewModel.connectedIp.value, viewModel.serverSize.intValue)
        // Center content in the Box
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,

        ) {
            when(viewModel.connectionMode.value){
                "client" -> StudentContent(viewModel)
                "server" -> TeacherContent(viewModel)
            }
            Spacer(modifier = Modifier.weight(1f))
            DisconnectButton(
                mode = viewModel.connectionMode.value,
                onDisconnect = {
                    if (viewModel.connectionMode.value == "server") {
                        viewModel.stopServer()
                    } else {
                        viewModel.disconnectFromServer()
                    }
                    onDisconnect()

                }
            )
        }
    }
}

@Composable
fun TeacherContent(viewModel: PluggedViewModel){

        Column(
        ) {

            Button(onClick = {

            }) {
                Text("Comprehension Check")
            }

            HorizontalDivider(
                thickness = 2.dp, // Thicker line
                color = Color.Gray, // Custom color,
                modifier = Modifier
                    .width(370.dp)
                    .padding(vertical = 16.dp)
            )


            Card(

            ) {
                LazyColumn(
//            state = listState,
                ) {
                    itemsIndexed(viewModel.questionsList) { index, message ->
                        MessageItem(message, { viewModel.removeQuestion(index) })
                    }
                }
            }
        }


}
@Composable
fun StudentContent(viewModel: PluggedViewModel){
    val messageToSend by remember { viewModel.messageToSend }
        Column() {
        Row() {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text("Slow Down Please")
            }
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text("I Am Confused")
            }
        }
        HorizontalDivider(
            thickness = 2.dp, // Thicker line
            color = Color.Gray, // Custom color,
            modifier = Modifier
                .width(370.dp)
                .padding(vertical = 16.dp)
        )
        Column() {
            MessageInputRow(
                messageToSend = messageToSend,
                onMessageChange = { viewModel.messageToSend.value = it },
                onSendMessage = { viewModel.sendMessage(messageToSend) }
            )
        }
            }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputRow(
    messageToSend: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageToSend,
            onValueChange = onMessageChange,
            label = { Text("Message") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            maxLines = 3
        )

        IconButton(
            onClick = onSendMessage,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun DisconnectButton(
    mode: String,
    onDisconnect: () -> Unit
) {
    Button(
        onClick = onDisconnect,
        modifier = Modifier
    ) {
        Text(if (mode == "server") "Stop Server" else "Disconnect")
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
//            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        )
    )
}
@Preview
@Composable
fun PluggedBottomBar(){
    BottomAppBar(
//        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
//        contentColor = MaterialTheme.colorScheme.primary,
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