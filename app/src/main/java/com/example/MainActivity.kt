package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.RewardsRepository
import com.example.ui.RewardsDashboardScreen
import com.example.ui.RewardsViewModel
import com.example.ui.RewardsViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database & Repository directly in Activity
    val database = AppDatabase.getDatabase(this)
    val repository = RewardsRepository(database.rewardsDao())
    
    // Create ViewModel with custom Factory
    val viewModelFactory = RewardsViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, viewModelFactory)[RewardsViewModel::class.java]

    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          RewardsDashboardScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
