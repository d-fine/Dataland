package org.dataland.datalandspringbase.configurations

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.http.HttpStatus

/**
 * This serializer class ensures that an HttpStatus gets serialized as its respective HTTP Status code
 */
class HttpStatusIntegerSerializer : JsonSerializer<HttpStatus>() {
    override fun serialize(value: HttpStatus?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) gen.writeNull()
        else gen.writeNumber(value.value())
    }
}
