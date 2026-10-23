package com.sifre.kasasi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity

@Composable
fun LockScreen(activity: FragmentActivity, onUnlocked: () -> Unit) {
    var message by remember { mutableStateOf("") }
    val available = BiometricHelper.canAuthenticate(activity)

    fun ask() {
        BiometricHelper.authenticate(
            activity,
            onSuccess = { onUnlocked() },
            onError = { message = it }
        )
    }

    LaunchedEffect(Unit) {
        if (available) ask()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Şifre Kasası", style = MaterialTheme.typography.headlineMedium)
        if (available) {
            Button(
                onClick = { ask() },
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Kilidi aç")
            }
            if (message.isNotEmpty()) {
                Text(message, modifier = Modifier.padding(top = 16.dp))
            }
        } else {
            Text(
                "Önce telefonda ekran kilidi veya parmak izi ayarla.",
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}
