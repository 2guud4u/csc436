package com.zybooks.sockety.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class ConnectionViewModel : ViewModel(){
    var role by mutableStateOf("")
    var serverId by mutableStateOf("")
    var inputId by mutableStateOf("")

    fun generate6DigitUUID():String {
        return Random.nextInt(1000000, 9999999).toString()
    }
}