package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SkyCastWeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

  private val weatherViewModel: WeatherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val state by weatherViewModel.uiState.collectAsStateWithLifecycle()
      MyApplicationTheme(themeMode = state.themeMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          SkyCastWeatherScreen(viewModel = weatherViewModel)
        }
      }
    }
  }
}
