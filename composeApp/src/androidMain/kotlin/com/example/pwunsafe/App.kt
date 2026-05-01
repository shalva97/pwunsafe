package com.example.pwunsafe

import androidx.compose.runtime.Composable
import com.example.pwunsafe.ui.screen.CredentialListScreen
import com.example.pwunsafe.ui.theme.PwUnsafeTheme
import com.example.pwunsafe.ui.viewmodel.CredentialViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun App() {
    PwUnsafeTheme {
        val viewModel: CredentialViewModel = koinViewModel()
        CredentialListScreen(viewModel = viewModel)
    }
}
