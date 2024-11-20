package org.dataland.datalandcommunitymanager.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 *  This converter is used for database entities to convert the RequestStatus enum to strings when saving to the database
 *  and to convert the string to the RequestStatus when loading data from the database.
 *  As autoApply=true, this should work for all stored DataTypeEnums.
 */
@Converter(autoApply = true)
class RequestStatusEnumAttributeConverter : AttributeConverter<RequestStatus, String> {
    override fun convertToDatabaseColumn(dataType: RequestStatus?): String? = dataType?.name

    override fun convertToEntityAttribute(string: String?): RequestStatus? =
        string?.let {
            RequestStatus.entries.find { it.name == string } ?: throw IllegalArgumentException("Unknown dataType $string")
        }
}
