package com.example.pwunsafe.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pwunsafe.data.model.Credential
import com.example.pwunsafe.ui.viewmodel.CredentialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialListScreen(
    viewModel: CredentialViewModel,
    onAddNew: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val credentials by viewModel.credentials.collectAsState()
    val passwords = credentials.filter { it.passkey == null }
    val passkeys = credentials.filter { it.passkey != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("pwunsafe", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNew) {
                Icon(Icons.Default.Add, contentDescription = "Add credential")
            }
        },
    ) { padding ->
        if (credentials.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No credentials yet. Tap + to add one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                if (passwords.isNotEmpty()) {
                    item {
                        SectionHeader("Passwords")
                    }
                    items(passwords, key = { it.id }) { cred ->
                        PasswordItem(
                            credential = cred,
                            onEdit = { onEdit(it.id) },
                            onDelete = { viewModel.delete(it) },
                        )
                    }
                }
                if (passkeys.isNotEmpty()) {
                    item {
                        SectionHeader("Passkeys")
                    }
                    items(passkeys, key = { it.id }) { cred ->
                        PasskeyItem(
                            credential = cred,
                            onEdit = { onEdit(it.id) },
                            onDelete = { viewModel.delete(it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun PasswordItem(
    credential: Credential,
    onEdit: (Credential) -> Unit,
    onDelete: (String) -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(credential.service, style = MaterialTheme.typography.titleMedium)
                Text(credential.username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (credential.domain != null) {
                    Text(credential.domain, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Row {
                if (credential.password != null) {
                    IconButton(onClick = { clipboard.setText(AnnotatedString(credential.password)) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy password")
                    }
                }
                IconButton(onClick = { onEdit(credential) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(credential.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PasskeyItem(
    credential: Credential,
    onEdit: (Credential) -> Unit,
    onDelete: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(credential.service, style = MaterialTheme.typography.titleMedium)
                Text(credential.username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                credential.passkey?.let {
                    Text(it.rpId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Row {
                IconButton(onClick = { onEdit(credential) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDelete(credential.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
