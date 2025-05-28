package org.dataland.datalandspecification.specifications

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * A specification for a framework
 */
data class FrameworkTranslation(
    val id: String,
    val schema: ObjectNode = JsonNodeFactory.instance.objectNode(),
)
