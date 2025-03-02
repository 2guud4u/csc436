package com.zybooks.petadoption

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException

class WebSocketClientActivity : ComponentActivity() {
    private val TAG = "WebSocketClient"
    private var client: MyWebSocketClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WebSocketClientScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WebSocketClientScreen() {
        val logMessages = remember { mutableStateListOf<String>() }
        var serverIp by remember { mutableStateOf("") }
        var messageToSend by remember { mutableStateOf("") }
        var isConnected by remember { mutableStateOf(false) }

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
                    Text(
                        text = "WebSocket Client",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = serverIp,
                            onValueChange = { serverIp = it },
                            label = { Text("Server IP") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        Button(
                            onClick = {
                                if (!isConnected) {
                                    connectToServer(serverIp, logMessages)
                                    isConnected = true
                                } else {
                                    disconnectFromServer()
                                    isConnected = false
                                }
                            }
                        ) {
                            Text(if (!isConnected) "Connect" else "Disconnect")
                        }
                    }

                    Text(
                        text = "Messages:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageToSend,
                            onValueChange = { messageToSend = it },
                            label = { Text("Message") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        Button(
                            onClick = {
                                sendMessage(messageToSend)
                                messageToSend = ""
                            },
                            enabled = isConnected
                        ) {
                            Text("Send")
                        }
                    }
                }
            }
        }
    }

    private fun connectToServer(serverIp: String, logMessages: MutableList<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverUri = URI("ws://$serverIp:8080")
                client = MyWebSocketClient(serverUri, logMessages)
                client?.connect()
                Log.d(TAG, "Connecting to $serverIp:8080")
            } catch (e: URISyntaxException) {
                Log.e(TAG, "Error connecting to server: ${e.message}")
                e.printStackTrace()

                CoroutineScope(Dispatchers.Main).launch {
                    logMessages.add("Error connecting: ${e.message}")
                }
            }
        }
    }

    private fun disconnectFromServer() {
        client?.let {
            try {
                it.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun sendMessage(message: String) {
        client?.let {
            if (it.isOpen) {
                it.send(message)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromServer()
    }

    inner class MyWebSocketClient(serverUri: URI, private val logMessages: MutableList<String>) : WebSocketClient(serverUri) {
        override fun onOpen(handshakedata: ServerHandshake) {
            Log.d(TAG, "Connected to server")

            CoroutineScope(Dispatchers.Main).launch {
                logMessages.add("Connected to server")
            }
        }

        override fun onMessage(message: String) {
            Log.d(TAG, "Received message: $message")

            CoroutineScope(Dispatchers.Main).launch {
                logMessages.add("Received: $message")
            }
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.d(TAG, "Connection closed: $reason")

            CoroutineScope(Dispatchers.Main).launch {
                logMessages.add("Connection closed: $reason")
            }
        }

        override fun onError(ex: Exception) {
            Log.e(TAG, "Error occurred: ${ex.message}")
            ex.printStackTrace()

            CoroutineScope(Dispatchers.Main).launch {
                logMessages.add("Error: ${ex.message}")
            }
        }
    }
}