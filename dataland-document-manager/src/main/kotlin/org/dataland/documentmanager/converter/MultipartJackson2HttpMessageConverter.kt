package org.dataland.documentmanager.converter

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.documentmanager.controller.DocumentController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import java.lang.reflect.Type

/**
 * An HttpMessageConverter used for converting application/octet-stream multipart chunks using Jackson.
 * This workaround is required to make the Swagger-UI work as it does not send the correct Content-Type for the Json.
 * The issue in the Swagger-Ui GitHub repo is still open. There is a Dataland-backlog item to follow up on this.
 */
@Component
class MultipartJackson2HttpMessageConverter(
    @Autowired private val objectMapper: ObjectMapper,
) : AbstractJackson2HttpMessageConverter(objectMapper, MediaType("application", "octet-stream")) {
    override fun canWrite(mediaType: MediaType?): Boolean = false

    override fun canRead(
        clazz: Class<*>,
        mediaType: MediaType?,
    ): Boolean = false

    override fun canRead(
        type: Type,
        contextClass: Class<*>?,
        mediaType: MediaType?,
    ): Boolean {
        if (contextClass != DocumentController::class.java) {
            return false
        }
        return super.canRead(type, contextClass, mediaType)
    }
}
