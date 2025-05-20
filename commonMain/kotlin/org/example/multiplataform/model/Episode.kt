package org.example.multiplataform.model

import kotlinx.serialization.Serializable

@Serializable
data class EpisodeResponse(
    val results: List<Episode>
)

@Serializable
data class Episode(
    val id: Int,
    val name: String,
    val episode: String,
    val air_date: String
)
