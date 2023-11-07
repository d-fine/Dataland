package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * A PercentageComponent represents a decimal percentage between 0 % and 100 %.
 */
class PercentageComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
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
                "formatPercentageForDatatable(${getTypescriptFieldAccessor()})",
                setOf("import { formatPercentageForDatatable } from \"@/components/resources/dataTable/conversion/PercentageValueGetterFactory\";"),
            ),
        )
    }
}
