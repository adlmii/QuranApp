package com.example.quranapp.ui.screens.almatsurat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.AlMatsuratRepository
import com.example.quranapp.model.AlMatsurat
import com.example.quranapp.model.MatsuratType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class AlMatsuratUiState(
    val matsuratList: List<AlMatsurat> = emptyList(),
    val matsuratType: MatsuratType = MatsuratType.MORNING,
    val isLoading: Boolean = false
)

class AlMatsuratViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlMatsuratRepository(application)

    private val _uiState = MutableStateFlow(AlMatsuratUiState())
    val uiState: StateFlow<AlMatsuratUiState> = _uiState.asStateFlow()

    fun loadMatsurat(type: MatsuratType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                matsuratType = type,
                isLoading = true
            )
            val list = repository.getMatsurat(type)
            _uiState.value = _uiState.value.copy(
                matsuratList = list,
                isLoading = false
            )
        }
    }
}

