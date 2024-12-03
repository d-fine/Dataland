package org.dataland.datalandcommunitymanager.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * Converts RequestStatus entries in Entities to database values and vice versa
 */
@Converter
class RequestStatusEnumAttributeConverter : AttributeConverter<RequestStatus, String> {
    override fun convertToDatabaseColumn(requestStatus: RequestStatus): String = requestStatus.toString()

    override fun convertToEntityAttribute(requestStatusAsString: String): RequestStatus = RequestStatus.valueOf(requestStatusAsString)
}
