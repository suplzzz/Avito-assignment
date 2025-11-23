package com.suplz.avitoassignment.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.suplz.avitoassignment.presentation.navigation.AppNavGraph
import com.suplz.avitoassignment.presentation.ui.theme.AvitoAssignmentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvitoAssignmentTheme {
                AppNavGraph()
            }
        }
    }
}