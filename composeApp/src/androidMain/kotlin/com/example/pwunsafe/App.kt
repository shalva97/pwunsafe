package com.example.pwunsafe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun App() {
    PwUnsafeTheme {
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
