package com.zybooks.petadoption

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.zybooks.petadoption.ui.PluggedApp
import com.zybooks.petadoption.ui.theme.PetAdoptionTheme
import java.net.NetworkInterface

class MainActivity : ComponentActivity() {
   private val logMessages = mutableStateListOf<String>()
   private val requestPermissionsLauncher = registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions()
   ) { permissions ->
      if (permissions.all { it.value }) {

      } else {

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
         e.printStackTrace()
      }

      return "Unknown"
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      requestNeededPermissions()
      val ipAddress = getLocalIpAddress()
      setContent {
         PetAdoptionTheme {
            Surface(
               modifier = Modifier.fillMaxSize(),
               color = MaterialTheme.colorScheme.background
            ) {
               PluggedApp(ipAddress)
            }
         }
      }
   }
}
