package com.zybooks.sockety.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RefreshButton(onRefresh: () -> Unit) {
    IconButton(onClick = onRefresh) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh"
        )
    }
}

@Composable
fun TextWithCaption(caption: String, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall, // Caption style
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp)) // Space between caption and text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ItemWithCaption(caption: String, item: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall, // Caption style
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp)) // Space between caption and text
        item()
    }
}

@Composable
fun TextFieldWithCaption(
    labelText: String,
    textInput: String,
    onValueChange: (String) -> Unit,
){
    Text("Class Code")
    TextField(
        value = textInput,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
    )
}