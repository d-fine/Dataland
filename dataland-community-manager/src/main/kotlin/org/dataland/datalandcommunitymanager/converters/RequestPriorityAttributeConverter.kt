package org.dataland.datalandcommunitymanager.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority

/**
 *  This converter is used for database entities to convert the Request Priority enum to strings when saving to
 *  the database and to convert the string to the DataType when loading data from the database.
 */
@Converter(autoApply = true)
class RequestPriorityAttributeConverter : AttributeConverter<RequestPriority, String> {
    override fun convertToDatabaseColumn(requestPriority: RequestPriority?): String? = requestPriority?.name

    override fun convertToEntityAttribute(string: String?): RequestPriority? =
        string?.let {
            RequestPriority.entries.find { it.name == string } ?: throw IllegalArgumentException("Unknown requestPriority $string")
        }
}
