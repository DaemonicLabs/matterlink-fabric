package matterlink.jenkins

import kotlinx.serialization.Serializable

@Serializable
data class BuildWithDetails(
    val number: Int,
    val url: String,
    val artifacts: List<Artifact>,
    val timestamp: Long
)