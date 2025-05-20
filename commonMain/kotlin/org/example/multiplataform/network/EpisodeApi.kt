package org.example.multiplataform.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.multiplataform.model.EpisodeResponse

object EpisodeApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchEpisodes(): EpisodeResponse {
        return client.get("https://rickandmortyapi.com/api/episode").body()
    }
}
