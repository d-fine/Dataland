package org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults

/**
 * Persists [PreApprovalCheckResults] as a JSON string in the database and restores it when loading the entity.
 */
@Converter
class PreApprovalCheckResultsConverter : AttributeConverter<PreApprovalCheckResults, String> {
    /**
     * Serializes the structured pre-approval check results to JSON for database storage.
     */
    override fun convertToDatabaseColumn(attribute: PreApprovalCheckResults?): String? =
        attribute?.let { JsonUtils.defaultObjectMapper.writeValueAsString(it) }

    /**
     * Deserializes the stored JSON string back into [PreApprovalCheckResults].
     */
    override fun convertToEntityAttribute(dbData: String?): PreApprovalCheckResults? =
        dbData?.let { JsonUtils.defaultObjectMapper.readValue(it, PreApprovalCheckResults::class.java) }
}
