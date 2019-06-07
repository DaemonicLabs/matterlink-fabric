package matterlink.api

import kotlinx.serialization.Encoder
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json

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

    fun encode(): String {
        return Json.nonstrict.stringify(ApiMessage.serializer(), this)
    }


    override fun toString(): String = encode()

    @Serializer(forClass = ApiMessage::class)
    companion object {
        const val USER_ACTION = "user_action"
        const val JOIN_LEAVE = "join_leave"

        override fun serialize(output: Encoder, obj: ApiMessage) {
            val elemOutput = output.beginStructure(descriptor)
            obj.username.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 0, it)
            }
            obj.text.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 1, it)
            }
            obj.gateway.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 2, it)
            }
            obj.timestamp.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 3, it)
            }
            obj.channel.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 4, it)
            }
            obj.userid.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 5, it)
            }
            obj.avatar.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 6, it)
            }
            obj.account.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 7, it)
            }
            obj.protocol.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 8, it)
            }
            obj.event.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 9, it)
            }
            obj.id.takeIf { it.isNotEmpty() }?.let {
                elemOutput.encodeStringElement(descriptor, 10, it)
            }
//            obj.Extra.takeIf { ! it.isNullOrEmpty() }?.let {
//                elemOutput.encodeStringElement(descriptor, 11, it)
//            }
            elemOutput.endStructure(descriptor)
        }

        fun decode(input: String): ApiMessage {
            return Json.nonstrict.parse(ApiMessage.serializer(), input)
        }
    }
}
