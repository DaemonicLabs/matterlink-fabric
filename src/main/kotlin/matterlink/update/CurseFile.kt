package matterlink.update

import kotlinx.serialization.Serializable

@Serializable
data class CurseFile(
    val downloadURL: String,
    val fileName: String,
    val gameVersion: List<String>,
    val releaseType: String,
    val fileStatus: String
)