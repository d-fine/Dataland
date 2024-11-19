package org.dataland.frameworktoolbox.specific.specification.elements

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.TextNode

/**
 * A DatapointBuilder is a part of a DataModel hierarchy
 */
class DatapointBuilder(
    override val identifier: String,
    val dataPointId: String,
    override val parentCategory: CategoryBuilder?,
) : SpecificationElement {
    override fun toJsonNode(): TextNode = JsonNodeFactory.instance.textNode(dataPointId)
}
