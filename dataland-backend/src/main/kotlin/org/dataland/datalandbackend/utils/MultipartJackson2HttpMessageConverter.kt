package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.controller.SmeDataController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.stereotype.Service
import java.lang.reflect.Type

/**
 * A HttpMessage converter that accepts application/octet-stream Multipart chunks
 * and converts them using Jackson. The implementation is restricted to only work for the
 * SmeData parameter in the SmeDataController. This workaround is required to make the Swagger-UI work
 * as it does not send the correct Content-Type for the Json. Once the Swagger-UI has been fixed, this workaround
 * can be safely deleted: https://github.com/swagger-api/swagger-ui/issues/6462. But as the issue was open
 * for 4 years at the time of writing, this may never happen.
 */
@Service
class MultipartJackson2HttpMessageConverter(
    @Autowired objectMapper: ObjectMapper,
) : AbstractJackson2HttpMessageConverter(objectMapper, MediaType("application", "octet-stream")) {

    companion object {
        const val SME_DATA_TYPENAME = "org.dataland.datalandbackend.model.companies.CompanyAssociatedData" +
            "<org.dataland.datalandbackend.frameworks.sme.model.SmeData>"
    }

    override fun canWrite(mediaType: MediaType?): Boolean {
        return false
    }

    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return false
    }

    override fun canRead(type: Type, contextClass: Class<*>?, mediaType: MediaType?): Boolean {
        if (contextClass != SmeDataController::class.java || type.typeName != SME_DATA_TYPENAME) {
            return false
        }
        return super.canRead(type, contextClass, mediaType)
    }
}
