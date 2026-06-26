package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.ProfileScreen
import com.example.ui.ProfileViewModel
import com.example.ui.HybridAuthScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: ProfileViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(
        darkTheme = true,      // Force the deep dark threads theme requested by the user
        dynamicColor = false    // Disable dynamic coloring to prevent overrides of the color scheme
      ) {
        val profileState by viewModel.profile.collectAsState()

        if (profileState == null) {
          // Beautiful dark landing skeleton / loading indicator
          androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
          ) {
            CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
          }
        } else if (profileState?.isLoggedIn == true) {
          ProfileScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          HybridAuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
              // Transition handles automatically via Room stream updating profileState to isLoggedIn = true!
            },
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}

