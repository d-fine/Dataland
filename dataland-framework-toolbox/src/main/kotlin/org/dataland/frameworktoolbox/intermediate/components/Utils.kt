package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
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
 * Add a cell to the upload-config section with configuration shared between components
 */
fun UploadCategoryBuilder.addStandardUploadConfigCell(
    frameworkUploadOptions: FrameworkUploadOptions?,
    component: ComponentBase,
    uploadComponentName: String,
    options: MutableSet<SelectionOption>? = null,
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
        frameworkUploadOptions = frameworkUploadOptions,
    )
}
