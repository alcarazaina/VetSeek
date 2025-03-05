package com.dam2v.vetseek.model.network

import com.dam2v.vetseek.model.data.VeterinarioData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun buscarVeterinariosCercanos(
        @Query("location") ubicacion: String,
        @Query("radius") radio: Int = 5000,
        @Query("type") tipo: String = "veterinary_care",
        @Query("key") apiKey: String
    ): PlacesResponse

    @GET("maps/api/place/textsearch/json")
    suspend fun buscarVeterinariosPorTexto(
        @Query("query") consulta: String,
        @Query("type") tipo: String = "veterinary_care",
        @Query("key") apiKey: String
    ): PlacesResponse

    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/"

        fun create(): MapsApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MapsApiService::class.java)
        }
    }
}

data class PlacesResponse(
    val results: List<PlaceResult>,
    val status: String
)

data class PlaceResult(
    val place_id: String,
    val name: String,
    val vicinity: String?,
    val formatted_address: String?,  // Añadimos este campo
    val rating: Double?,
    val opening_hours: OpeningHours?,
    val geometry: Geometry,
    val photos: List<Photo>?
)

data class OpeningHours(
    val open_now: Boolean?
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Photo(
    val photo_reference: String
)



