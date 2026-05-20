package com.example.pwunsafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.autofill.AutofillManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pwunsafe.ui.screen.AddEditCredentialScreen
import com.example.pwunsafe.ui.screen.CredentialListScreen
import com.example.pwunsafe.ui.theme.PwUnsafeTheme
import com.example.pwunsafe.ui.viewmodel.CredentialViewModel
import org.koin.androidx.compose.koinViewModel

private const val ROUTE_LIST = "list"
private const val ROUTE_ADD_EDIT = "add_edit"
private const val ARG_CREDENTIAL_ID = "credentialId"

private fun hasStorageAccess(context: android.content.Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

@Composable
fun App() {
    PwUnsafeTheme {
        val context = LocalContext.current

        // Storage permission — must be granted before the app can read/write Downloads
        var showStorageDialog by remember { mutableStateOf(!hasStorageAccess(context)) }

        val storageSettingsLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { showStorageDialog = !hasStorageAccess(context) }

        val storagePermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { showStorageDialog = !hasStorageAccess(context) }

        if (showStorageDialog) {
            AlertDialog(
                onDismissRequest = { showStorageDialog = false },
                title = { Text("Storage Access Required") },
                text = { Text("pwunsafe stores credentials in the public Downloads folder (pwunsafe_credentials.json). Grant storage access to continue.") },
                confirmButton = {
                    TextButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            storageSettingsLauncher.launch(
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        } else {
                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }) {
                        Text("Grant Access")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStorageDialog = false }) {
                        Text("Not Now")
                    }
                },
            )
        }

        // Autofill service prompt — shown only when storage is already sorted
        val autofillManager = remember { context.getSystemService(AutofillManager::class.java) }
        var showAutofillDialog by remember {
            mutableStateOf(autofillManager?.hasEnabledAutofillServices() == false)
        }

        val autofillSettingsLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            showAutofillDialog = autofillManager?.hasEnabledAutofillServices() == false
        }

        if (!showStorageDialog && showAutofillDialog) {
            AlertDialog(
                onDismissRequest = { showAutofillDialog = false },
                title = { Text("Set as Autofill Service") },
                text = { Text("Enable pwunsafe as the default autofill service to automatically fill test credentials.") },
                confirmButton = {
                    TextButton(onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        autofillSettingsLauncher.launch(intent)
                    }) {
                        Text("Enable")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAutofillDialog = false }) {
                        Text("Not Now")
                    }
                },
            )
        }

        val navController = rememberNavController()
        val viewModel: CredentialViewModel = koinViewModel()

        NavHost(navController = navController, startDestination = ROUTE_LIST) {
            composable(ROUTE_LIST) {
                CredentialListScreen(
                    viewModel = viewModel,
                    onAddNew = { navController.navigate(ROUTE_ADD_EDIT) },
                    onEdit = { id -> navController.navigate("$ROUTE_ADD_EDIT?$ARG_CREDENTIAL_ID=$id") },
                )
            }
            composable(
                route = "$ROUTE_ADD_EDIT?$ARG_CREDENTIAL_ID={$ARG_CREDENTIAL_ID}",
                arguments = listOf(
                    navArgument(ARG_CREDENTIAL_ID) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                val credentialId = backStackEntry.arguments?.getString(ARG_CREDENTIAL_ID)
                val credentials by viewModel.credentials.collectAsState()
                val initial = credentialId?.let { id -> credentials.find { it.id == id } }
                AddEditCredentialScreen(
                    initial = initial,
                    onSave = { cred ->
                        if (credentialId == null) viewModel.add(cred) else viewModel.update(cred)
                        navController.popBackStack()
                    },
                    onDismiss = { navController.popBackStack() },
                )
            }
        }
    }
}
