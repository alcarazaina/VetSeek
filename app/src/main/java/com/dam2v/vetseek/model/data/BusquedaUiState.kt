package com.dam2v.vetseek.model.data


data class BusquedaUiState(
    val isLoading: Boolean = false,
    val veterinarios: List<VeterinarioData> = emptyList(),
    val error: String? = null,
    val ubicacionBusqueda: String = "",
    val usarUbicacionActual: Boolean = true,
    val busquedasRecientes: List<BusquedaReciente> = emptyList(),
    val mostrarBusquedasRecientes: Boolean = false
)
