package org.dataland.frameworktoolbox.specific.specification.elements

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandspecification.specifications.DataPointType
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.specification.FrameworkSpecificationBuilder
import org.dataland.frameworktoolbox.specific.specification.SpecificationNamingConvention

/**
 * A CategoryBuilder is a part of a DataModel hierarchy
 */
class CategoryBuilder(
    override val identifier: String,
    override val parentCategory: CategoryBuilder?,
    private val builder: FrameworkSpecificationBuilder,
    val childElements: MutableList<SpecificationElement> = mutableListOf(),
) : SpecificationElement {
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
        dataPointTypeId: String,
    ): Pair<DataPointType, DatapointBuilder> {
        val specificationId =
            SpecificationNamingConvention.generateDataPointSpecificationName(
                component.documentSupport,
                component.identifier,
                typeNameSuffix,
            )
        val specification =
            addDatapointSpecification(
                id = specificationId,
                name = component.label ?: throw IllegalArgumentException("Component must have a label"),
                businessDefinition =
                    component.uploadPageExplanation ?: throw IllegalArgumentException("Component must have an uploadPageExplanation"),
                dataPointSchemaId = dataPointTypeId,
            )
        val datapoint =
            addDatapointToFrameworkHierarchy(
                identifier = component.identifier,
                dataPointId = specificationId,
            )
        return Pair(specification, datapoint)
    }

    /**
     * Add a new data point specification to the framework
     */
    fun addDatapointSpecification(
        id: String,
        name: String,
        businessDefinition: String,
        dataPointSchemaId: String,
    ): DataPointType {
        require(builder.database.dataPointBaseTypes.containsKey(dataPointSchemaId)) {
            "Data point schema id $dataPointSchemaId does not exist in the database."
        }
        val newDatapointType =
            DataPointType(
                id = id,
                name = name,
                businessDefinition = businessDefinition,
                dataPointBaseTypeId = dataPointSchemaId,
                frameworkOwnership = builder.framework.identifier,
            )
        require(!builder.database.dataPointTypes.containsKey(id)) {
            "Data point specification with id $id already exists in the database."
        }
        builder.database.dataPointTypes[id] = newDatapointType
        return newDatapointType
    }

    /**
     * Add a new data point to the framework hierarchy
     */
    fun addDatapointToFrameworkHierarchy(
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
}
