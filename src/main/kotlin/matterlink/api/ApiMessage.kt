package matterlink.api

import kotlinx.serialization.Encoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import matterlink.jsonNonstrict

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
@Serializable
data class ApiMessage(
    var username: String = "",
    var text: String = "",
    var gateway: String = "",
    var timestamp: String = "",
    var channel: String = "",
    var userid: String = "",
    var avatar: String = "",
    var account: String = "",
    var protocol: String = "",
    var event: String = "",
    var id: String = "",
    var Extra: Map<String, String>? = null
) {
    override fun toString(): String = jsonNonstrict.stringify(ApiMessage.serializer(), this)

    companion object {
        const val USER_ACTION = "user_action"
        const val JOIN_LEAVE = "join_leave"
    }
}
