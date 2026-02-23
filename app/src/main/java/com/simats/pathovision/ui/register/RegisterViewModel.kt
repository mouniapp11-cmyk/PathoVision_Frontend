package com.simats.pathovision.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.models.RegisterResponse
import com.simats.pathovision.repository.AuthRepository
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<RegisterResponse>?>(null)
    val registerState = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            _registerState.value = authRepository.register(name, email, password, role)
        }
    }

    fun resetState() {
        _registerState.value = null
    }
}
