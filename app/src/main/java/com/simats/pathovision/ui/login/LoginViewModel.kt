package com.simats.pathovision.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.models.LoginResponse
import com.simats.pathovision.repository.AuthRepository
import com.simats.pathovision.utils.Resource
import com.simats.pathovision.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = authRepository.login(email, password)
            if (result is Resource.Success) {
                result.data.token?.let { tokenManager.saveToken(it) }
                result.data.user?.role?.let { tokenManager.saveRole(it) }
            }
            _loginState.value = result
        }
    }

    fun resetState() {
        _loginState.value = null
    }
}
