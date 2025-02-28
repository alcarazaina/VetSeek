package com.dam2v.vetseek.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2v.vetseek.model.data.BusquedaUiState
import com.dam2v.vetseek.model.network.MapsApiService
import com.dam2v.vetseek.model.network.MapsDataMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BusquedaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BusquedaUiState())
    val uiState: StateFlow<BusquedaUiState> = _uiState.asStateFlow()

    private val mapsApiService = MapsApiService.create()
    private val apiKey = "AIzaSyC28tdSlyPFV1hEnP0k75Dptcnu5hLe65Y"

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun inicializarLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun actualizarUbicacionBusqueda(ubicacion: String) {
        _uiState.update { it.copy(ubicacionBusqueda = ubicacion, usarUbicacionActual = false) }
    }

    fun toggleUsarUbicacionActual(usar: Boolean) {
        _uiState.update { it.copy(usarUbicacionActual = usar) }
    }

    @SuppressLint("MissingPermission")
    fun buscarVeterinarios(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                if (_uiState.value.usarUbicacionActual) {
                    // Verificar permisos
                    if (tienePermisosUbicacion(context)) {
                        val ubicacion = obtenerUbicacionActual()
                        if (ubicacion != null) {
                            val ubicacionStr = "${ubicacion.latitude},${ubicacion.longitude}"
                            val respuesta = mapsApiService.buscarVeterinariosCercanos(
                                ubicacion = ubicacionStr,
                                apiKey = apiKey
                            )

                            if (respuesta.status == "OK") {
                                val veterinarios = respuesta.results.map {
                                    MapsDataMapper.mapToVeterinarioData(it, apiKey)
                                }
                                _uiState.update { it.copy(
                                    isLoading = false,
                                    veterinarios = veterinarios
                                )}
                            } else {
                                _uiState.update { it.copy(
                                    isLoading = false,
                                    error = "Error en la búsqueda: ${respuesta.status}"
                                )}
                            }
                        } else {
                            _uiState.update { it.copy(
                                isLoading = false,
                                error = "No se pudo obtener la ubicación actual"
                            )}
                        }
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Se requieren permisos de ubicación"
                        )}
                    }
                } else {
                    // Buscar por texto
                    if (_uiState.value.ubicacionBusqueda.isNotEmpty()) {
                        val consulta = "veterinario en ${_uiState.value.ubicacionBusqueda}"
                        val respuesta = mapsApiService.buscarVeterinariosPorTexto(
                            consulta = consulta,
                            apiKey = apiKey
                        )

                        if (respuesta.status == "OK") {
                            val veterinarios = respuesta.results.map {
                                MapsDataMapper.mapToVeterinarioData(it, apiKey)
                            }
                            _uiState.update { it.copy(
                                isLoading = false,
                                veterinarios = veterinarios
                            )}
                        } else {
                            _uiState.update { it.copy(
                                isLoading = false,
                                error = "Error en la búsqueda: ${respuesta.status}"
                            )}
                        }
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Ingresa una ubicación para buscar"
                        )}
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )}
            }
        }
    }

    private fun tienePermisosUbicacion(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private suspend fun obtenerUbicacionActual(): Location? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { e ->
                    continuation.resume(null)
                }
        }
    }
    }

