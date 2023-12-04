package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.specific.uploadconfig.elements.SectionUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkDisplayValueLambda
as FrameworkDisplayValueLambdaUpload

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
fun SectionUploadConfigBuilder.addStandardCellWithValueGetterFactory( // todo to addField und anpassen
    uploadComponentName: String?,
    component: ComponentBase,
    valueGetter: FrameworkDisplayValueLambdaUpload,
) {
    addCell(
        label = component.label ?: throw IllegalStateException(
            "You must specify a label for ${component.identifier} to generate a view configuration",
        ),
        explanation = component.explanation,
        unit = component.unit,
        required = component.required,
        isNullable = component.isNullable,
        shouldDisplay = component.availableIfUpload.toFrameworkBooleanLambdaUpload(),
        valueGetter = valueGetter,
        uploadComponentName = uploadComponentName,
    )
}
