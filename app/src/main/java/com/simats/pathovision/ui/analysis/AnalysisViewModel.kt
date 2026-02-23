package com.simats.pathovision.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.pathovision.models.CaseItem
import com.simats.pathovision.repository.CaseRepository
import com.simats.pathovision.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _casesState = MutableStateFlow<Resource<List<CaseItem>>?>(null)
    val casesState = _casesState.asStateFlow()

    init {
        loadAnalyses()
    }

    fun loadAnalyses() {
        viewModelScope.launch {
            _casesState.value = Resource.Loading()
            _casesState.value = caseRepository.getCases()
        }
    }
}
