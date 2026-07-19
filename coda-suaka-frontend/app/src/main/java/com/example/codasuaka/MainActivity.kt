package com.example.codasuaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.codasuaka.navigation.AppNavigation
import com.example.codasuaka.ui.theme.CodaSuakaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodaSuakaTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
