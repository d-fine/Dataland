package org.dataland.frameworktoolbox.specific.specification.elements

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ValueNode

/**
 * A TranslationBuilder is a part of a DataModel hierarchy for the framework translations
 */
class TranslationBuilder(
    override val identifier: String,
    val aliasExport: String?,
    override val parentCategory: CategoryBuilder?,
) : SpecificationElement {
    override fun toJsonNode(): ValueNode =
        if (aliasExport != null) JsonNodeFactory.instance.textNode(aliasExport) else JsonNodeFactory.instance.nullNode()
}
