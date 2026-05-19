package com.example.pwunsafe.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pwunsafe.data.model.Credential
import com.example.pwunsafe.data.model.PasskeyData
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCredentialScreen(
    initial: Credential?,
    onSave: (Credential) -> Unit,
    onDismiss: () -> Unit,
) {
    val isEdit = initial != null
    val initialIsPasskey = initial?.passkey != null
    var selectedTab by remember { mutableIntStateOf(if (initialIsPasskey) 1 else 0) }

    var service by remember { mutableStateOf(initial?.service ?: "") }
    var packageName by remember { mutableStateOf(initial?.packageName ?: "") }
    var domain by remember { mutableStateOf(initial?.domain ?: "") }
    var username by remember { mutableStateOf(initial?.username ?: "") }
    var password by remember { mutableStateOf(initial?.password ?: "") }
    var rpId by remember { mutableStateOf(initial?.passkey?.rpId ?: "") }
    var privateKeyPkcs8 by remember { mutableStateOf(initial?.passkey?.privateKeyPkcs8 ?: "") }
    var publicKeyCose by remember { mutableStateOf(initial?.passkey?.publicKeyCose ?: "") }
    var userHandle by remember { mutableStateOf(initial?.passkey?.userHandle ?: "") }

    val title = if (isEdit) "Edit Credential" else "Add Credential"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!isEdit) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Password", "Passkey").forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                            label = { Text(label) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = service,
                onValueChange = { service = it },
                label = { Text("Service name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("App package name (optional)") },
                placeholder = { Text("e.g. com.github.android") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (selectedTab == 0) {
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domain (optional)") },
                    placeholder = { Text("e.g. github.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
            } else {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username / Display name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = rpId,
                    onValueChange = { rpId = it },
                    label = { Text("RP ID (relying party domain)") },
                    placeholder = { Text("e.g. example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = userHandle,
                    onValueChange = { userHandle = it },
                    label = { Text("User handle (base64url)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = privateKeyPkcs8,
                    onValueChange = { privateKeyPkcs8 = it },
                    label = { Text("Private key PKCS#8 (base64)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                )
                OutlinedTextField(
                    value = publicKeyCose,
                    onValueChange = { publicKeyCose = it },
                    label = { Text("Public key COSE (base64, optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val cred = if (selectedTab == 0) {
                            Credential(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                service = service.trim(),
                                packageName = packageName.trim().ifEmpty { null },
                                domain = domain.trim().ifEmpty { null },
                                username = username.trim(),
                                password = password.ifEmpty { null },
                            )
                        } else {
                            Credential(
                                id = initial?.id ?: UUID.randomUUID().toString(),
                                service = service.trim(),
                                packageName = packageName.trim().ifEmpty { null },
                                username = username.trim(),
                                passkey = PasskeyData(
                                    rpId = rpId.trim(),
                                    credentialId = initial?.passkey?.credentialId ?: UUID.randomUUID().toString(),
                                    privateKeyPkcs8 = privateKeyPkcs8.trim(),
                                    publicKeyCose = publicKeyCose.trim(),
                                    userHandle = userHandle.trim(),
                                    signCount = initial?.passkey?.signCount ?: 0,
                                ),
                            )
                        }
                        onSave(cred)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = service.isNotBlank() && username.isNotBlank() &&
                        (selectedTab == 0 || (rpId.isNotBlank() && userHandle.isNotBlank())),
                ) {
                    Text("Save")
                }
            }
        }
    }
}
