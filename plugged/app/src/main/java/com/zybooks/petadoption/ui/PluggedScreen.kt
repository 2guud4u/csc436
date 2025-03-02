package com.zybooks.petadoption.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

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
    val mode by remember { viewModel.connectionMode }
    val serverIp by remember { viewModel.serverIp }
    val port by remember { viewModel.port }

    val isConnected by remember { viewModel.isConnected }
    val logMessages = viewModel.logMessages
    val navController = rememberNavController()

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
                    topBar = {
                        PluggedTopBar()},
                    bottomBar = {
                        PluggedBottomBar()

                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { }) {
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
                        if (mode.isEmpty()) {
                            ModeSelectionCard(
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

                    }

                    composable<Routes.Connect> { backstackEntry ->
                        if (mode.isNotEmpty() && !isConnected) {
                            ConnectionSettingsCard(
                                mode = mode,
                                ipAddress = ipAddress,
                                serverIp = serverIp,
                                port = port,
                                onServerIpChange = { viewModel.serverIp.value = it },
                                onPortChange = { viewModel.port.value = it },
                                onStartServer = { viewModel.startServer(port)
                                    navController.navigate(
                                        Routes.Interact
                                        )},
                                onConnectToServer = { viewModel.connectToServer(serverIp, port)
                                    navController.navigate(
                                        Routes.Interact
                                    )}
                            )
                        }

                    }
                    composable<Routes.Interact> { backstackEntry ->
                        val details: Routes.Interact = backstackEntry.toRoute()

                        InteractionScreen(viewModel,ipAddress=ipAddress, logMessages)

//                        Spacer(modifier = Modifier.height(8.dp))
//

//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        DisconnectButton(
//                            mode = mode,
//                            onDisconnect = {
//                                if (mode == "server") {
//                                    viewModel.stopServer()
//                                } else {
//                                    viewModel.disconnectFromServer()
//                                }
//                            }
//                        )

                    }
                }

                }
                }
            }
        }
    }
}

@Composable
fun ModeSelectionCard(viewModel: PluggedViewModel, onSelect: (String) -> Unit) {

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
fun ConnectionSettingsCard(
    mode: String,
    ipAddress: String,
    serverIp: String,
    port: String,
    onServerIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onStartServer: () -> Unit,
    onConnectToServer: () -> Unit
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

                Button(
                    onClick = onStartServer,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Start")
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

                Button(
                    onClick = onConnectToServer,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Connect")
                }
            }
        }

}
@Composable
fun InteractionScreen(
    viewModel: PluggedViewModel,
    ipAddress: String,
    logMessages: SnapshotStateList<String>
){
    val messageToSend by remember { viewModel.messageToSend }
    // Align InteractTopBar to the top-center
    InteractTopBar(classCode = ipAddress)
    // Center content in the Box
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize() // Ensure the column fills the available space
    ) {
        LogMessagesCard(logMessages = logMessages)
        MessageInputRow(
            messageToSend = messageToSend,
            onMessageChange = { viewModel.messageToSend.value = it },
            onSendMessage = { viewModel.sendMessage(messageToSend) }
        )
        when(viewModel.connectionMode.value){
            "client" -> StudentContent()
            "server" -> TeacherContent()
        }
    }
}
@Composable
fun LogMessagesCard(logMessages: List<String>) {
    Card(

    ) {
        LazyColumn(
            modifier = Modifier
        ) {
            items(logMessages) { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
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
                .padding(end = 8.dp)
        )

        Button(
            onClick = onSendMessage
        ) {
            Text("Send")
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
        modifier = Modifier.fillMaxWidth()
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