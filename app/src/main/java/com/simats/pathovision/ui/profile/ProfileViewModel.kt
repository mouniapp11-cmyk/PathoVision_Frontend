package com.simats.pathovision.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.models.ProfileResponse
import com.simats.pathovision.repository.ProfileRepository
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<ProfileResponse>?>(null)
    val profileState = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<Resource<Map<String, Any>>?>(null)
    val updateState = _updateState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            val shouldShowLoading = _profileState.value == null
            if (shouldShowLoading) {
                _profileState.value = Resource.Loading()
            }
            _profileState.value = profileRepository.getProfile()
        }
    }

    fun updateProfile(
        name: String? = null,
        phoneNumber: String? = null,
        hospitalAffiliation: String? = null,
        licenseId: String? = null,
        profileImage: okhttp3.MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            _updateState.value = profileRepository.updateProfile(
                name = name,
                phoneNumber = phoneNumber,
                hospitalAffiliation = hospitalAffiliation,
                licenseId = licenseId,
                profileImage = profileImage
            )
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }
}
