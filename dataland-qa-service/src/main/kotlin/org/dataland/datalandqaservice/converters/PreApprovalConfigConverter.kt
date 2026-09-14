package org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig

/**
 * Persists [PreApprovalConfig] as a JSON string in the database and restores it when loading the entity.
 */
@Converter
class PreApprovalConfigConverter : AttributeConverter<PreApprovalConfig, String> {
    /**
     * Serializes the pre-approval configuration to JSON for database storage.
     */
    override fun convertToDatabaseColumn(attribute: PreApprovalConfig?): String? =
        attribute?.let { JsonUtils.defaultObjectMapper.writeValueAsString(it) }

    /**
     * Deserializes the stored JSON string back into a [PreApprovalConfig].
     */
    override fun convertToEntityAttribute(dbData: String?): PreApprovalConfig? =
        dbData?.let { JsonUtils.defaultObjectMapper.readValue(it, PreApprovalConfig::class.java) }
}
