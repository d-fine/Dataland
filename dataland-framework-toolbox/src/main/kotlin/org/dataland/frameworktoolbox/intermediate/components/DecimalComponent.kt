package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import java.math.BigDecimal

/**
 * A DecimalComponent represents a numeric decimal value
 */
open class DecimalComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    open var minimumValue: BigDecimal? = null
    open var maximumValue: BigDecimal? = null
    open var constantUnitSuffix: String? = null

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference("java.math.BigDecimal", true),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addCell(
            label = label ?: throw IllegalStateException(
                "You must specify a label for $identifier to generate a view configuration",
            ),
            explanation = explanation,
            shouldDisplay = FrameworkBooleanLambda.TRUE,
            valueGetter = FrameworkDisplayValueLambda(
                "formatNumberForDatatable(${getTypescriptFieldAccessor()}, \"${StringEscapeUtils.escapeEcmaScript(constantUnitSuffix ?: "")}\")",
                setOf("import { formatNumberForDatatable } from \"@/components/resources/dataTable/conversion/NumberValueGetterFactory\";"),
            ),
        )
    }
}
