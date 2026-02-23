package com.simats.pathovision.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.models.CaseItem
import com.simats.pathovision.models.UserData
import com.simats.pathovision.repository.CaseRepository
import com.simats.pathovision.repository.UserRepository
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatWithDoctorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _doctorsState = MutableStateFlow<Resource<List<UserData>>?>(null)
    val doctorsState = _doctorsState.asStateFlow()

    private val _casesState = MutableStateFlow<Resource<List<CaseItem>>?>(null)
    val casesState = _casesState.asStateFlow()

    init {
        loadDoctors()
        loadCases()
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _doctorsState.value = Resource.Loading()
            _doctorsState.value = userRepository.getDoctors()
        }
    }

    fun loadCases() {
        viewModelScope.launch {
            _casesState.value = Resource.Loading()
            _casesState.value = caseRepository.getCases()
        }
    }
}
