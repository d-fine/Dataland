package org.dataland.frameworktoolbox.specific.specification.elements

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.specifications.DataPointType
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.specification.FrameworkBuilder
import org.dataland.frameworktoolbox.specific.specification.SpecificationNamingConvention

/**
 * A CategoryBuilder is a part of a DataModel hierarchy
 */
class CategoryBuilder(
    override val identifier: String,
    override val parentCategory: CategoryBuilder?,
    private val builder: FrameworkBuilder,
    val childElements: MutableList<SpecificationElement> = mutableListOf(),
) : SpecificationElement {
    companion object {
        const val MAXIMUM_DATA_POINT_TYPE_ID_LENGTH = 255
    }

    /**
     * Add a new category to the hierarchy
     */
    fun addCategory(identifier: String): CategoryBuilder {
        val newCategory =
            CategoryBuilder(
                identifier = identifier,
                parentCategory = this,
                builder = builder,
            )
        childElements.add(newCategory)
        return newCategory
    }

    override fun toJsonNode(): ObjectNode {
        val node = JsonNodeFactory.instance.objectNode()
        for (child in childElements) {
            node.set<JsonNode>(child.identifier, child.toJsonNode())
        }
        return node
    }

    /**
     * Add a new data point specification to the framework and include it in the hierarchy
     */
    fun addDefaultDatapointAndSpecification(
        component: ComponentBase,
        typeNameSuffix: String,
        dataPointBaseTypeId: String? = null,
        dataPointTypeIdOverwrite: String? = null,
    ): Pair<DataPointType, DatapointBuilder> {
        val dataPointTypeId =
            dataPointTypeIdOverwrite
                ?: SpecificationNamingConvention.generateDataPointTypeId(
                    component.documentSupport,
                    component.dataPointTypeName ?: component.identifier,
                    typeNameSuffix,
                )
        val specification =
            addDatapointSpecification(
                id = dataPointTypeId,
                name = component.label ?: throw IllegalArgumentException("Component must have a label"),
                businessDefinition =
                    component.uploadPageExplanation ?: throw IllegalArgumentException("Component must have an uploadPageExplanation"),
                dataPointBaseTypeId = dataPointBaseTypeId ?: "${component.documentSupport.getNamingPrefix()}$typeNameSuffix",
                constraints = component.getConstraints(),
            )
        val datapoint =
            addDatapointToFrameworkHierarchy(
                identifier = component.identifier,
                dataPointId = dataPointTypeId,
            )
        return Pair(specification, datapoint)
    }

    /**
     *  Include a new translation in the framework translation hierarchy
     */
    fun addDefaultTranslation(component: ComponentBase): TranslationBuilder {
        val aliasExport = component.aliasExport
        val translation =
            addTranslationToFrameworkHierarchy(
                identifier = component.identifier,
                aliasExport = aliasExport,
            )
        return translation
    }

    /**
     * Add a new data point specification to the framework
     */
    private fun addDatapointSpecification(
        id: String,
        name: String,
        businessDefinition: String,
        dataPointBaseTypeId: String,
        constraints: List<String>?,
    ): DataPointType {
        require(builder.database.dataPointBaseTypes.containsKey(dataPointBaseTypeId)) {
            "Data point base type id $dataPointBaseTypeId does not exist in the database."
        }
        require(id.length < MAXIMUM_DATA_POINT_TYPE_ID_LENGTH) {
            "The length of the data-point-id field in the databases (e.g., the QA database) is limited to 255 chars. " +
                "Therefore, the id of the data point type must be shorter than 255 chars."
        }
        val newDatapointType =
            DataPointType(
                id = id,
                name = name,
                businessDefinition = businessDefinition,
                dataPointBaseTypeId = dataPointBaseTypeId,
                frameworkOwnership = setOf(builder.framework.identifier),
                constraints = constraints,
            )
        val existingDataPointType = builder.database.dataPointTypes[id]
        val combinedDataPointType =
            if (existingDataPointType != null) {
                assertDataPointTypeConsistency(existingDataPointType = existingDataPointType, newDataPointType = newDatapointType)
                require(!existingDataPointType.frameworkOwnership.contains(builder.framework.identifier)) {
                    "Trying to add two datapoints with the same id $id to the same framework ${builder.framework.identifier}"
                }
                newDatapointType.copy(frameworkOwnership = existingDataPointType.frameworkOwnership + newDatapointType.frameworkOwnership)
            } else {
                newDatapointType
            }
        builder.database.dataPointTypes[id] = combinedDataPointType
        return combinedDataPointType
    }

    private fun assertDataPointTypeConsistency(
        existingDataPointType: DataPointType?,
        newDataPointType: DataPointType,
    ) {
        if (existingDataPointType == null) {
            return
        }
        require(existingDataPointType.copy(frameworkOwnership = emptySet()) == newDataPointType.copy(frameworkOwnership = emptySet())) {
            "Inconsistency detected for Data point type with id ${existingDataPointType.id}. " +
                "Existing: $existingDataPointType, new: $newDataPointType"
        }
    }

    /**
     * Add a new data point to the framework specification hierarchy
     */
    private fun addDatapointToFrameworkHierarchy(
        identifier: String,
        dataPointId: String,
    ): DatapointBuilder {
        val newDatapoint =
            DatapointBuilder(
                identifier = identifier,
                dataPointId = dataPointId,
                parentCategory = this,
            )
        childElements.add(newDatapoint)
        return newDatapoint
    }

    /**
     * Add a new translation to the framework translation hierarchy
     */
    private fun addTranslationToFrameworkHierarchy(
        identifier: String,
        aliasExport: String?,
    ): TranslationBuilder {
        val newTranslation =
            TranslationBuilder(
                identifier = identifier,
                aliasExport = aliasExport,
                parentCategory = this,
            )
        childElements.add(newTranslation)
        return newTranslation
    }
}
