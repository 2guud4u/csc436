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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer

class WebSocketServerActivity : ComponentActivity() {
    private val TAG = "WebSocketServer"
    private val PORT = 8080
    private var server: MyWebSocketServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.ACCESS_WIFI_STATE
                ),
                1001
            )
        }
        // Get the device's IP address
        val ipAddress = getLocalIpAddress()

        setContent {
            WebSocketServerScreen(ipAddress)
        }

        // Start the WebSocket server
        startServer(ipAddress)
    }

    @Composable
    fun WebSocketServerScreen(ipAddress: String) {
        val logMessages = remember { mutableStateListOf<String>() }

        // Observe server log messages
        LaunchedEffect(Unit) {
            server?.logMessages = logMessages
        }

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
                        text = "WebSocket Server",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "IP Address: $ipAddress",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Server Status:",
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
                }
            }
        }
    }

    private fun startServer(ipAddress: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                server = MyWebSocketServer(InetSocketAddress(PORT))
                server?.start()
                Log.d(TAG, "Server started on $ipAddress:$PORT")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()

            // Look through all the network interfaces
            for (networkInterface in networkInterfaces) {
                // Skip loopback interfaces and interfaces that are down
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }

                // Get all IP addresses assigned to the interface
                val addresses = networkInterface.inetAddresses.toList()
                for (address in addresses) {
                    // Skip loopback addresses and IPv6 addresses
                    if (address.isLoopbackAddress || address.hostAddress.contains(":")) {
                        continue
                    }

                    return address.hostAddress
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address: ${e.message}")
            e.printStackTrace()
        }

        return "Unknown"
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the server when the activity is destroyed
        server?.let {
            try {
                it.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    inner class MyWebSocketServer(address: InetSocketAddress) : WebSocketServer(address) {
        var logMessages = mutableStateListOf<String>()

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

            // Broadcast message to all connected clients
            broadcast("Client said: $message")
        }

        override fun onMessage(conn: WebSocket, message: ByteBuffer) {
            // Handle binary messages if needed
            Log.d(TAG, "Received binary message")
            addLogMessage("Received binary message")
        }

        override fun onError(conn: WebSocket?, ex: Exception) {
            Log.e(TAG, "Error occurred: ${ex.message}")
            ex.printStackTrace()

            addLogMessage("Error: ${ex.message}")
        }

        override fun onStart() {
            Log.d(TAG, "Server started on port: $PORT")
            addLogMessage("Server started on port: $PORT")
        }

        private fun addLogMessage(message: String) {
            CoroutineScope(Dispatchers.Main).launch {
                logMessages.add(message)
            }
        }
    }
}