package com.dam2v.vetseek.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dam2v.vetseek.model.data.BusquedaReciente

import com.dam2v.vetseek.model.database.AppDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.dam2v.vetseek.R
import com.dam2v.vetseek.model.data.BusquedaUiState
import com.dam2v.vetseek.model.network.MapsApiService
import com.dam2v.vetseek.model.network.MapsDataMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

class BusquedaViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BusquedaUiState())
    val uiState: StateFlow<BusquedaUiState> = _uiState.asStateFlow()

    private val mapsApiService = MapsApiService.create()
    private val apiKey = application.getString(R.string.maps_api_key)

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val busquedaRecienteDao = AppDatabase.getDatabase(application).busquedaRecienteDao()

    init {
        // Cargar búsquedas recientes al iniciar
        viewModelScope.launch {
            busquedaRecienteDao.obtenerBusquedasRecientes().collectLatest { busquedas ->
                _uiState.update { it.copy(busquedasRecientes = busquedas) }
            }
        }
    }

    fun inicializarLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun actualizarUbicacionBusqueda(ubicacion: String) {
        _uiState.update { it.copy(ubicacionBusqueda = ubicacion, usarUbicacionActual = false) }
    }

    fun toggleUsarUbicacionActual(usar: Boolean) {
        _uiState.update { it.copy(usarUbicacionActual = usar) }
    }

    fun mostrarBusquedasRecientes(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarBusquedasRecientes = mostrar) }
    }

    fun seleccionarBusquedaReciente(busqueda: String) {
        _uiState.update {
            it.copy(
                ubicacionBusqueda = busqueda,
                usarUbicacionActual = false,
                mostrarBusquedasRecientes = false
            )
        }
    }

    fun eliminarBusquedaReciente(texto: String) {
        viewModelScope.launch {
            busquedaRecienteDao.eliminarBusqueda(texto)
        }
    }

    @SuppressLint("MissingPermission")
    fun buscarVeterinarios(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, veterinarios = emptyList()) }

            try {
                if (uiState.value.usarUbicacionActual) {
                    // Verificar permisos
                    if (tienePermisosUbicacion(context)) {
                        val ubicacion = obtenerUbicacionActual()
                        if (ubicacion != null) {
                            val ubicacionStr = "${ubicacion.latitude},${ubicacion.longitude}"
                            realizarBusqueda(ubicacionStr)
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
                    // Guardar la búsqueda en la base de datos si no está vacía
                    val textoBusqueda = uiState.value.ubicacionBusqueda.trim()
                    if (textoBusqueda.isNotEmpty()) {
                        busquedaRecienteDao.insertarBusqueda(BusquedaReciente(textoBusqueda))
                    }

                    // Realizar la búsqueda
                    realizarBusqueda(textoBusqueda)
                }
            } catch (e: Exception) {
                android.util.Log.e("MapsAPI", "Error general", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )}
            }
        }
    }

    private suspend fun realizarBusqueda(query: String) {
        try {
            val respuesta = if (uiState.value.usarUbicacionActual) {
                mapsApiService.buscarVeterinariosCercanos(
                    ubicacion = query,
                    apiKey = apiKey
                )
            } else {
                mapsApiService.buscarVeterinariosPorTexto(query, "veterinary_care", apiKey)
            }

            android.util.Log.d("MapsAPI", "Response status: ${respuesta.status}")
            android.util.Log.d("MapsAPI", "Response body: $respuesta")

            if (respuesta.status == "OK") {
                val veterinarios = respuesta.results.mapNotNull { placeResult ->
                    try {
                        MapsDataMapper.mapToVeterinarioData(placeResult, apiKey)
                    } catch (e: Exception) {
                        android.util.Log.e("MapsAPI", "Error mapping place result: $placeResult", e)
                        null
                    }
                }
                _uiState.update { it.copy(isLoading = false, veterinarios = veterinarios) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error en la búsqueda: ${respuesta.status}") }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapsAPI", "Error in API call", e)
            _uiState.update { it.copy(isLoading = false, error = "Error en la llamada a la API: ${e.message}") }
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

    fun obtenerDetallesVeterinario(veterinarioId: String) {
        viewModelScope.launch {
            try {
                // Aquí podrías hacer una llamada a la API de Google Places para obtener más detalles
                // Por ejemplo, usando el endpoint Place Details
                // Por ahora, simplemente actualizamos el estado para indicar que estamos cargando
                _uiState.update { it.copy(isLoading = true) }

                // Simulamos una carga
                delay(500)

                // Actualizamos el estado para indicar que hemos terminado de cargar
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al obtener detalles: ${e.message}"
                    )
                }
            }
        }
    }
}

