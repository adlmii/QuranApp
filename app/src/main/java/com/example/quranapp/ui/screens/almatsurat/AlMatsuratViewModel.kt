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

class AlMatsuratViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlMatsuratRepository(application)

    private val _matsuratList = MutableStateFlow<List<AlMatsurat>>(emptyList())
    val matsuratList: StateFlow<List<AlMatsurat>> = _matsuratList.asStateFlow()

    private val _matsuratType = MutableStateFlow(MatsuratType.MORNING)
    val matsuratType: StateFlow<MatsuratType> = _matsuratType.asStateFlow()

    fun loadMatsurat(type: MatsuratType) {
        viewModelScope.launch {
            _matsuratType.value = type
            _matsuratList.value = repository.getMatsurat(type)
        }
    }
}
