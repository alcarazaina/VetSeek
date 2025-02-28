package com.dam2v.vetseek.viewmodel

import androidx.lifecycle.ViewModel
import com.dam2v.vetseek.model.InicioUiState

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InicioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InicioUiState())
    val uiState: StateFlow<InicioUiState> = _uiState.asStateFlow()

    fun onComenzarClick() {
        _uiState.value = _uiState.value.copy(shouldNavigateToMenu = true)
    }

    fun onNavigatedToMenu() {
        _uiState.value = _uiState.value.copy(shouldNavigateToMenu = false)
    }
}
