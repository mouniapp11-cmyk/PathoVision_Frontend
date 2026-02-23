package com.simats.pathovision.ui.cases

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
class NewCaseViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _patientsState = MutableStateFlow<Resource<List<UserData>>?>(null)
    val patientsState = _patientsState.asStateFlow()

    private val _createCaseState = MutableStateFlow<Resource<CaseItem>?>(null)
    val createCaseState = _createCaseState.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _patientsState.value = Resource.Loading()
            _patientsState.value = userRepository.getPatients()
        }
    }

    fun createCase(patientId: String, tissueType: String, clinicalNotes: String?) {
        viewModelScope.launch {
            _createCaseState.value = Resource.Loading()
            _createCaseState.value = caseRepository.createCase(
                patientId = patientId,
                tissueType = tissueType,
                clinicalNotes = clinicalNotes
            )
        }
    }

    fun resetCreateCaseState() {
        _createCaseState.value = null
    }
}
