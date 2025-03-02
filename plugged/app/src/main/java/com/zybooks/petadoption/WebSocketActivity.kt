package com.zybooks.petadoption

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer

class WebSocketActivity : ComponentActivity() {
    private val TAG = "WebSocketApp"
    private val DEFAULT_PORT = 45678
    private var server: MyWebSocketServer? = null
    private var client: MyWebSocketClient? = null
    private val logMessages = mutableStateListOf<String>()

    // For permission request
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            addLogMessage("All permissions granted")
        } else {
            addLogMessage("Some permissions were denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        requestNeededPermissions()

        // Get the device's IP address
        val ipAddress = getLocalIpAddress()

        setContent {
            WebSocketScreen(ipAddress)
        }
    }

    private fun requestNeededPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestPermissionsLauncher.launch(permissions)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WebSocketScreen(ipAddress: String) {
        var mode by remember { mutableStateOf("") } // "client" or "server"
        var serverIp by remember { mutableStateOf("") }
        var port by remember { mutableStateOf(DEFAULT_PORT.toString()) }
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
                        text = "WebSocket Communication",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Mode selection if not connected
                    if (mode.isEmpty()) {
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
                                        onClick = {
                                            mode = "server"
                                            addLogMessage("Selected server mode")
                                        }
                                    ) {
                                        Text("Server Mode")
                                    }

                                    Button(
                                        onClick = {
                                            mode = "client"
                                            addLogMessage("Selected client mode")
                                        }
                                    ) {
                                        Text("Client Mode")
                                    }
                                }
                            }
                        }
                    }

                    // Connection settings based on mode
                    if (mode.isNotEmpty() && !isConnected) {
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
                                        onValueChange = { port = it },
                                        label = { Text("Port") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )

                                    Button(
                                        onClick = {
                                            try {
                                                val portNum = port.toInt()
                                                startServer(portNum)
                                                isConnected = true
                                            } catch (e: NumberFormatException) {
                                                addLogMessage("Invalid port number")
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Start Server")
                                    }
                                } else { // Client mode
                                    OutlinedTextField(
                                        value = serverIp,
                                        onValueChange = { serverIp = it },
                                        label = { Text("Server IP") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )

                                    OutlinedTextField(
                                        value = port,
                                        onValueChange = { port = it },
                                        label = { Text("Port") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )

                                    Button(
                                        onClick = {
                                            try {
                                                val portNum = port.toInt()
                                                connectToServer(serverIp, portNum)
                                                isConnected = true
                                            } catch (e: NumberFormatException) {
                                                addLogMessage("Invalid port number")
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Connect")
                                    }
                                }
                            }
                        }
                    }

                    // Connection status and logs
                    if (mode.isNotEmpty()) {
                        Text(
                            text = "Status:",
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

                        // Message input for connected state
                        if (isConnected) {
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
                                    }
                                ) {
                                    Text("Send")
                                }
                            }

                            Button(
                                onClick = {
                                    if (mode == "server") {
                                        stopServer()
                                    } else {
                                        disconnectFromServer()
                                    }
                                    isConnected = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(if (mode == "server") "Stop Server" else "Disconnect")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        if (message.isBlank()) return

        when {
            server != null -> {
                server?.broadcast(message)
                addLogMessage("Broadcast: $message")
            }
            client != null -> {
                client?.send(message)
                addLogMessage("Sent: $message")
            }
        }
    }

    private fun startServer(port: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Attempt to check if the port is available
                if (!checkPortAvailability(port)) {
                    addLogMessage("Port $port is already in use. Try another port.")
                    return@launch
                }

                // Try binding to all interfaces
                server = MyWebSocketServer(InetSocketAddress("0.0.0.0", port))
                server?.start()
                Log.d(TAG, "Server started on port: $port")
                addLogMessage("Server started on port: $port")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server: ${e.message}")
                e.printStackTrace()
                addLogMessage("Error starting server: ${e.message}")

                // Try with higher permissions if on a real device
                try {
                    addLogMessage("Attempting with elevated privileges...")
                    val process = Runtime.getRuntime().exec("su")
                    server = MyWebSocketServer(InetSocketAddress("0.0.0.0", port))
                    server?.start()
                    addLogMessage("Server started with elevated privileges")
                } catch (e2: Exception) {
                    addLogMessage("Could not start server: ${e2.message}")
                }
            }
        }
    }

    private fun checkPortAvailability(port: Int): Boolean {
        return try {
            val serverSocket = java.net.ServerSocket(port)
            serverSocket.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Port $port is not available: ${e.message}")
            false
        }
    }

    private fun stopServer() {
        server?.let {
            try {
                it.stop()
                server = null
                addLogMessage("Server stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server: ${e.message}")
                e.printStackTrace()
                addLogMessage("Error stopping server: ${e.message}")
            }
        }
    }

    private fun connectToServer(serverIp: String, port: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val serverUri = URI("ws://$serverIp:$port")
                client = MyWebSocketClient(serverUri)
                client?.connect()
                Log.d(TAG, "Connecting to $serverIp:$port")
                addLogMessage("Connecting to $serverIp:$port")
            } catch (e: URISyntaxException) {
                Log.e(TAG, "Error connecting to server: ${e.message}")
                e.printStackTrace()
                addLogMessage("Error connecting: ${e.message}")
            }
        }
    }

    private fun disconnectFromServer() {
        client?.let {
            try {
                it.close()
                client = null
                addLogMessage("Disconnected from server")
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting: ${e.message}")
                e.printStackTrace()
                addLogMessage("Error disconnecting: ${e.message}")
            }
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()

            for (networkInterface in networkInterfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }

                val addresses = networkInterface.inetAddresses.toList()
                for (address in addresses) {
                    if (address.isLoopbackAddress || address.hostAddress.contains(":")) {
                        continue
                    }

                    return address.hostAddress
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address: ${e.message}")
            e.printStackTrace()
            addLogMessage("Error getting IP address: ${e.message}")
        }

        return "Unknown"
    }

    private fun addLogMessage(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            logMessages.add(message)
        }
    }

    inner class MyWebSocketServer(address: InetSocketAddress) : WebSocketServer(address) {
        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            val address = conn.remoteSocketAddress.address.hostAddress
            Log.d(TAG, "New connection from: $address")
            addLogMessage("New connection from: $address")

            // Send a welcome message to the client
            conn.send("Welcome to the server!")
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            Log.d(TAG, "Connection closed: $reason")
            addLogMessage("Connection closed: $reason")
        }

        override fun onMessage(conn: WebSocket, message: String) {
            Log.d(TAG, "Received message: $message")
            addLogMessage("Received: $message")

            // Echo the message back to the client
            conn.send("Server received: $message")
        }

        override fun onMessage(conn: WebSocket, message: ByteBuffer) {
            Log.d(TAG, "Received binary message")
            addLogMessage("Received binary message")
        }

        override fun onError(conn: WebSocket?, ex: Exception) {
            Log.e(TAG, "Error occurred: ${ex.message}")
            ex.printStackTrace()
            addLogMessage("Error: ${ex.message}")
        }

        override fun onStart() {
            Log.d(TAG, "Server started")
            addLogMessage("Server started successfully")
        }
    }

    inner class MyWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {
        override fun onOpen(handshakedata: ServerHandshake) {
            Log.d(TAG, "Connected to server")
            addLogMessage("Connected to server")
        }

        override fun onMessage(message: String) {
            Log.d(TAG, "Received message: $message")
            addLogMessage("Received: $message")
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.d(TAG, "Connection closed: $reason")
            addLogMessage("Connection closed: $reason")
        }

        override fun onError(ex: Exception) {
            Log.e(TAG, "Error occurred: ${ex.message}")
            ex.printStackTrace()
            addLogMessage("Error: ${ex.message}")
        }
    }

    override fun onDestroy() {
        stopServer()
        disconnectFromServer()
        super.onDestroy()
    }
}