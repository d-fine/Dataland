package org.dataland.frameworktoolbox.specific.specification.elements

import com.fasterxml.jackson.databind.JsonNode

/**
 * A SpecificationElement is part of a DataModel hierarchy
 * They can be built and compiled into a Dataland repository
 */
sealed interface SpecificationElement {
    val identifier: String
    val parentCategory: CategoryBuilder?

    fun toJsonNode(): JsonNode
}
