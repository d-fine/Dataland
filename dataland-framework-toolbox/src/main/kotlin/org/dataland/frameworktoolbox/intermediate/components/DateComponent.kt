package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * A DataComponent represents a date (with Year, Month, and Day)
 */
class DateComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference("java.time.LocalDate", isNullable),
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
                "formatStringForDatatable(${getTypescriptFieldAccessor()})",
                setOf("import { formatStringForDatatable } from \"@/components/resources/dataTable/conversion/PlainStringValueGetterFactory\";"),
            ),
        )
    }
}
