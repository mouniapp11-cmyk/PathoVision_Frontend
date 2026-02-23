package com.simats.pathovision.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val navigateToNext by viewModel.navigateToNextScreen.collectAsState()

    LaunchedEffect(navigateToNext) {
        if (navigateToNext) {
            onNavigateToNext()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Microscope Icon
            Icon(
                imageVector = Icons.Rounded.Biotech,
                contentDescription = "PathoVision Logo",
                tint = Color.Black,
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Title
            Text(
                text = "PathoVision",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B) // Navy Blue
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = "AI-Powered Histopathology Analysis",
                fontSize = 16.sp,
                color = Color(0xFF64748B) // Slate Gray
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading Indicator
            CircularProgressIndicator(
                color = Color(0xFF3B82F6), // Blue
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
