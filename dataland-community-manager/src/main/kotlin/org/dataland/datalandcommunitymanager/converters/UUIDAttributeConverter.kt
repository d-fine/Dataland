package org.dataland.datalandcommunitymanager.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.UUID

/**
 *  This converter is used for database entities to convert UUIDs to strings when saving to the database
 *  and to convert the string to the UUID when loading data from the database.
 *  As autoApply=true, this should work for all stored UUIDs.
 */
@Converter(autoApply = true)
class UUIDAttributeConverter : AttributeConverter<UUID, String> {
    override fun convertToDatabaseColumn(uuid: UUID?): String? = uuid?.toString()

    override fun convertToEntityAttribute(uuidAsString: String?): UUID? = uuidAsString?.let { UUID.fromString(it) }
}
