package com.dam2v.vetseek.model.network

import com.dam2v.vetseek.model.data.VeterinarioData

// Clase para manejar la conversión de PlaceResult a VeterinarioData
object MapsDataMapper {
    fun mapToVeterinarioData(placeResult: PlaceResult, apiKey: String): VeterinarioData {
        // Procesar las fotos para asegurarnos de obtener todas las disponibles
        val fotosUrl = placeResult.photos?.map {
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photoreference=${it.photo_reference}&key=$apiKey"
        } ?: emptyList()

        return VeterinarioData(
            id = placeResult.place_id,
            nombre = placeResult.name,
            direccion = placeResult.vicinity ?: placeResult.formatted_address ?: "",
            rating = placeResult.rating ?: 0.0,
            abierto = placeResult.opening_hours?.open_now ?: false,
            telefono = "",  // Mantenemos esto vacío ya que no tenemos el teléfono en esta respuesta
            latitud = placeResult.geometry.location.lat,
            longitud = placeResult.geometry.location.lng,
            fotosUrl = fotosUrl
        )
    }
}