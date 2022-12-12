package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Component
import java.util.*

@Component
class BearerTokenParser {
    private val objectMapper = ObjectMapper()

    fun extractBearerTokenFromRequest():String{
        return "thebearertoken" // TODO
    }

    fun decodeAndReturnBearerTokenPayload(bearerToken: String):String{
        val bearerTokenPayload = bearerToken.split(".")[1]
        val decoder = Base64.getUrlDecoder()
        return String(decoder.decode(bearerTokenPayload))
    }

    fun getSingleValueFromDecodedBearerToken(key: String, bearerToken:String): String? {
        val node: ObjectNode = objectMapper.readValue(bearerToken, ObjectNode::class.java)
        return node.get(key)?.toString()?.trim('"')
    }

    fun getKeycloakUserIdFromDecodedBearerToken(bearerToken: String):String?{
        val decodedPayload = decodeAndReturnBearerTokenPayload(bearerToken)
        return getSingleValueFromDecodedBearerToken("sub", decodedPayload)
    }
}