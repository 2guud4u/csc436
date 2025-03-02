package com.zybooks.petadoption

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zybooks.petadoption.ui.PluggedApp
import com.zybooks.petadoption.ui.theme.PetAdoptionTheme

import android.Manifest
import android.content.pm.PackageManager

import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
class MainActivity : ComponentActivity() {
   private val logMessages = mutableStateListOf<String>()
   private val requestPermissionsLauncher = registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions()
   ) { permissions ->
      if (permissions.all { it.value }) {

      } else {

      }
   }
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         PetAdoptionTheme {
            Surface(
               modifier = Modifier.fillMaxSize(),
               color = MaterialTheme.colorScheme.background
            ) {
               PluggedApp()
            }
         }
      }
   }
}
