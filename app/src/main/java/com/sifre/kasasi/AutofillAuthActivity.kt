package com.sifre.kasasi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.WindowManager
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity

@Suppress("DEPRECATION")
class AutofillAuthActivity : FragmentActivity() {

    companion object {
        const val EXTRA_USER = "user_id"
        const val EXTRA_PASS = "pass_id"
        const val EXTRA_HINT = "hint"
    }

    private var userId: AutofillId? = null
    private var passId: AutofillId? = null
    private var hint = ""
    private var unlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        userId = intent.getParcelableExtra(EXTRA_USER)
        passId = intent.getParcelableExtra(EXTRA_PASS)
        hint = intent.getStringExtra(EXTRA_HINT) ?: ""

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (unlocked) {
                        Picker()
                    } else {
                        Text(
                            "Doğrulanıyor...",
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }

        if (!BiometricHelper.canAuthenticate(this)) {
            cancel()
            return
        }
        BiometricHelper.authenticate(
            this,
            onSuccess = { unlocked = true },
            onError = { cancel() }
        )
    }

    private fun cancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun sortedEntries(): List<Entry> {
        val all = Vault.load(this)
        val skip = setOf(
            "www", "com", "org", "net", "android",
            "mobile", "app", "apps", "https", "http"
        )
        val tokens = hint.lowercase()
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 3 && it !in skip }
        return all.sortedWith(
            compareByDescending<Entry> { e ->
                tokens.any { e.title.lowercase().contains(it) }
            }.thenBy { it.title.lowercase() }
        )
    }

    private fun pick(e: Entry) {
        val b = Dataset.Builder()
        userId?.let {
            b.setValue(it, AutofillValue.forText(e.username))
        }
        passId?.let {
            b.setValue(it, AutofillValue.forText(e.password))
        }
        val reply = Intent().putExtra(
            AutofillManager.EXTRA_AUTHENTICATION_RESULT,
            b.build()
        )
        setResult(Activity.RESULT_OK, reply)
        finish()
    }

    @androidx.compose.runtime.Composable
    private fun Picker() {
        val list = sortedEntries()
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Doldurulacak kaydı seç",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            if (list.isEmpty()) {
                Text("Kasada kayıt yok.")
            }
            LazyColumn {
                items(list, key = { it.id }) { e ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { pick(e) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                e.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                e.username,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
