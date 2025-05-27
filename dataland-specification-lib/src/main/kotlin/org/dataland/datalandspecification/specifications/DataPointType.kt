package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.dataland.datalandspecification.database.SetOrderedJsonSerializer
import org.dataland.datalandspecification.database.SpecificationDatabase

/**
 * A specification for a data point type.
 * @param id The unique identifier of the data point type.
 * @param name The name of the data point type.
 * @param businessDefinition The business definition of the data point type.
 * @param dataPointBaseTypeId The unique identifier of the data point base type.
 * @param frameworkOwnership The "owning" framework of the data point type.
 *  Used by the framework toolbox to clean-up unused data point types.
 */
data class DataPointType(
    val id: String,
    val name: String,
    val aliasExport: String,
    val businessDefinition: String,
    val dataPointBaseTypeId: String,
    @JsonSerialize(converter = SetOrderedJsonSerializer::class)
    val frameworkOwnership: Set<String>,
    val constraints: List<String>? = null,
) {
    /**
     * Validates the integrity of the data point specification.
     */
    fun validateIntegrity(database: SpecificationDatabase) {
        VerificationUtils.assertValidId(id)
        require(database.dataPointBaseTypes.containsKey(dataPointBaseTypeId)) {
            "Data point base type id $dataPointBaseTypeId does not exist in the database."
        }
    }
}
