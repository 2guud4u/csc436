package com.zybooks.petadoption.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSocketScreen(viewModel: WebSocketViewModel, ipAddress: String) {
    // Collect state from ViewModel
    val mode by remember { viewModel.connectionMode }
    val serverIp by remember { viewModel.serverIp }
    val port by remember { viewModel.port }
    val messageToSend by remember { viewModel.messageToSend }
    val isConnected by remember { viewModel.isConnected }
    val logMessages = viewModel.logMessages

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // App Header
                Text(
                    text = "WebSocket Communication",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Mode selection if not connected
                if (mode.isEmpty()) {
                    ModeSelectionCard(viewModel)
                }

                // Connection settings based on mode
                if (mode.isNotEmpty() && !isConnected) {
                    ConnectionSettingsCard(
                        mode = mode,
                        ipAddress = ipAddress,
                        serverIp = serverIp,
                        port = port,
                        onServerIpChange = { viewModel.serverIp.value = it },
                        onPortChange = { viewModel.port.value = it },
                        onStartServer = { viewModel.startServer(port) },
                        onConnectToServer = { viewModel.connectToServer(serverIp, port) }
                    )
                }

                // Status label
                if (mode.isNotEmpty()) {
                    Text(
                        text = "Status:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Main content area with flexible height
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (mode.isNotEmpty()) {
                        LogMessagesCard(logMessages = logMessages)
                    }
                }

                // Fixed bottom area for message input and disconnect button
                if (mode.isNotEmpty() && isConnected) {
                    Spacer(modifier = Modifier.height(8.dp))

                    MessageInputRow(
                        messageToSend = messageToSend,
                        onMessageChange = { viewModel.messageToSend.value = it },
                        onSendMessage = { viewModel.sendMessage(messageToSend) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DisconnectButton(
                        mode = mode,
                        onDisconnect = {
                            if (mode == "server") {
                                viewModel.stopServer()
                            } else {
                                viewModel.disconnectFromServer()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModeSelectionCard(viewModel: WebSocketViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Mode",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.setMode("server") }
                ) {
                    Text("Server Mode")
                }

                Button(
                    onClick = { viewModel.setMode("client") }
                ) {
                    Text("Client Mode")
                }
            }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (mode == "server") "Server Settings" else "Client Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (mode == "server") {
                Text(
                    text = "Your IP Address: $ipAddress",
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
                    Text("Start Server")
                }
            } else { // Client mode
                OutlinedTextField(
                    value = serverIp,
                    onValueChange = onServerIpChange,
                    label = { Text("Server IP") },
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
}

@Composable
fun LogMessagesCard(logMessages: List<String>) {
    Card(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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