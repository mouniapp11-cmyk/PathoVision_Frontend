package com.simats.pathovision.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.repository.AuthRepository
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow<Resource<String>?>(null)
    val changePasswordState: StateFlow<Resource<String>?> = _changePasswordState

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = Resource.Loading()
            _changePasswordState.value = authRepository.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        }
    }

    fun clearState() {
        _changePasswordState.value = null
    }
}
