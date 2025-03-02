package com.zybooks.petadoption.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class WebSocketViewModel : ViewModel() {
    private val TAG = "WebSocketApp"
    private val DEFAULT_PORT = 45678

    // State
    val logMessages = mutableStateListOf<String>()
    val connectionMode = mutableStateOf("")
    val serverIp = mutableStateOf("")
    val port = mutableStateOf(DEFAULT_PORT.toString())
    val messageToSend = mutableStateOf("")
    val isConnected = mutableStateOf(false)

    // WebSocket components
    private var server: MyWebSocketServer? = null
    private var client: MyWebSocketClient? = null

    fun setMode(mode: String) {
        connectionMode.value = mode
        addLogMessage("Selected $mode mode")
    }

    fun sendMessage(message: String) {
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
        messageToSend.value = ""
    }

    fun startServer(portNumber: String) {
        try {
            val portNum = portNumber.toInt()
            startServerInternal(portNum)
            isConnected.value = true
        } catch (e: NumberFormatException) {
            addLogMessage("Invalid port number")
        }
    }

    private fun startServerInternal(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun stopServer() {
        server?.let {
            try {
                it.stop()
                server = null
                addLogMessage("Server stopped")
                isConnected.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server: ${e.message}")
                e.printStackTrace()
                addLogMessage("Error stopping server: ${e.message}")
            }
        }
    }

    fun connectToServer(serverIpAddress: String, portNumber: String) {
        try {
            val portNum = portNumber.toInt()
            connectToServerInternal(serverIpAddress, portNum)
            isConnected.value = true
        } catch (e: NumberFormatException) {
            addLogMessage("Invalid port number")
        }
    }

    private fun connectToServerInternal(serverIp: String, port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun disconnectFromServer() {
        client?.let {
            try {
                it.close()
                client = null
                addLogMessage("Disconnected from server")
                isConnected.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting: ${e.message}")
                e.printStackTrace()
                addLogMessage("Error disconnecting: ${e.message}")
            }
        }
    }

    fun getLocalIpAddress(): String {
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

    fun addLogMessage(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
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

    fun cleanup() {
        stopServer()
        disconnectFromServer()
    }
}