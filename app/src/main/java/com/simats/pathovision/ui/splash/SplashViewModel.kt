package com.simats.pathovision.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _navigateToNextScreen = MutableStateFlow(false)
    val navigateToNextScreen = _navigateToNextScreen.asStateFlow()

    init {
        startSplash()
    }

    private fun startSplash() {
        viewModelScope.launch {
            // Simulate loading process (e.g., checking auth state in DataStore)
            delay(3000)
            _navigateToNextScreen.value = true
        }
    }
}
