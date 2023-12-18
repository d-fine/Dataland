package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.SectionUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
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
        shouldDisplay = component.availableIf.toFrameworkBooleanLambda(),
        valueGetter = valueGetter,
    )
}

/**
 * Add a cell to the section with configuration shared between components
 * and a component-specific value-getter
 */
fun SectionUploadConfigBuilder.addStandardCellWithValueGetterFactory(
    uploadComponentName: String?,
    options: MutableSet<SelectionOption>?,
    component: ComponentBase,
    unit: String? = null,
) {
    addCell(
        identifier = component.identifier,
        label = component.label ?: throw IllegalStateException(
            "You must specify a label for ${component.identifier} to generate a view configuration",
        ),
        explanation = component.explanation,
        unit = unit,
        required = component.isRequired,
        shouldDisplay = component.availableIf.toFrameworkBooleanLambda(),
        uploadComponentName = uploadComponentName,
        options = options,
    )
}
