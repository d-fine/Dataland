package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * Add a cell to the section with configuration shared between components
 * and a component-specific value-getter
 */
fun SectionConfigBuilder.addStandardCellWithValueGetterFactory(
    component: ComponentBase,
    valueGetter: FrameworkDisplayValueLambda,
) {
    addCell(
        label = component.label ?: throw IllegalStateException(
            "You must specify a label for ${component.identifier} to generate a view configuration",
        ),
        explanation = component.explanation,
        shouldDisplay = FrameworkBooleanLambda.TRUE,
        valueGetter = valueGetter,
    )
}
