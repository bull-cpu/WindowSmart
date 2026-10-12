package com.example.ventanasmart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

suspend fun obtenerClima(): String {

    return withContext(Dispatchers.IO) {

        try {

            val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=-36.82" +
                    "&longitude=-72.72" +
                    "&current=temperature_2m,weather_code,precipitation,is_day" +
                    "&timezone=auto"

            val respuesta = URL(url).readText()

            val json = JSONObject(respuesta)

            val current = json.getJSONObject("current")

            val temperatura = current.getDouble("temperature_2m")
            val codigoClima = current.getInt("weather_code")
            val esDeDia = current.getInt("is_day")

            val descripcion = when (codigoClima) {

                0 -> "Despejado"
                1, 2, 3 -> "Parcialmente nublado"
                45, 48 -> "Niebla"
                51, 53, 55 -> "Llovizna"
                61, 63, 65 -> "Lluvia"
                71, 73, 75 -> "Nieve"
                80, 81, 82 -> "Chubascos"
                95, 96, 99 -> "Tormenta"
                else -> "Clima desconocido"
            }

            val momento = if (esDeDia == 1) {
                "Día"
            } else {
                "Noche"
            }

            "$temperatura °C\n$descripcion\n$momento"

        } catch (e: Exception) {

            "No se pudo obtener el clima"

        }
    }
}