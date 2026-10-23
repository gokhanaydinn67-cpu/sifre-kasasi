package com.sifre.kasasi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity

@Composable
fun VaultScreen(activity: FragmentActivity) {
    val entries = remember {
        mutableStateListOf<Entry>().apply { addAll(Vault.load(activity)) }
    }
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Entry?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    fun persist() {
        Vault.save(activity, entries.toList())
    }

    val shown = entries
        .filter {
            it.title.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true)
        }
        .sortedWith(
            compareByDescending<Entry> { it.favorite }
                .thenBy { it.title.lowercase() }
        )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Şifre Kasası",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Ara") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(shown, key = { it.id }) { e ->
                    EntryRow(
                        entry = e,
                        activity = activity,
                        onFavorite = {
                            val i = entries.indexOfFirst { x -> x.id == e.id }
                            if (i >= 0) {
                                entries[i] = e.copy(favorite = !e.favorite)
                                persist()
                            }
                        },
                        onEdit = {
                            editing = e
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        val current = editing
        EntryDialog(
            initial = current,
            onDismiss = { showDialog = false },
            onSave = { saved ->
                val i = entries.indexOfFirst { it.id == saved.id }
                if (i >= 0) entries[i] = saved else entries.add(saved)
                persist()
                showDialog = false
            },
            onDelete = if (current != null) {
                {
                    entries.removeAll { it.id == current.id }
                    persist()
                    showDialog = false
                }
            } else null
        )
    }
}

@Composable
fun EntryRow(
    entry: Entry,
    activity: FragmentActivity,
    onFavorite: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                Text(entry.username, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onFavorite, modifier = Modifier.size(40.dp)) {
                Icon(
                    if (entry.favorite) Icons.Default.Star
                    else Icons.Default.StarBorder,
                    contentDescription = "Favori"
                )
            }
            IconButton(
                onClick = {
                    ClipboardHelper.copy(activity, "Kullanıcı adı", entry.username)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Kullanıcı adını kopyala")
            }
            IconButton(
                onClick = {
                    ClipboardHelper.copy(activity, "Şifre", entry.password)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = "Şifreyi kopyala")
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Düzenle")
            }
        }
    }
}
