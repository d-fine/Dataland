package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueByDataPointLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

private val EXTRACT_DATAPOINT_VALUE_IMPORT =
    TypeScriptImport("extractDatapointValue", "@/components/resources/dataTable/conversion/DataPoints")

private val PARSE_DATAPOINT_IMPORT =
    TypeScriptImport("parseDataPoint", "@/components/resources/dataTable/conversion/DataPoints")

/**
 * Adds a standard cell to the section whose value-getters are wrapped with the component's DocumentSupport
 * (e.g., BaseDataPoint / ExtendedDataPoint wrapping). The data-point-based value-getter reads the raw
 * datapoint value via `extractDatapointValue`.
 *
 * @param sectionConfigBuilder the section to add the cell to
 * @param formatterImports the TypeScript imports required by [buildBody]'s formatter function
 * @param dataPointValueExpression the TypeScript expression used to read the value for the data-point-based
 * value-getter, typically `extractDatapointValue(dataPoint) as <Type>`
 * @param additionalDataPointImports extra TypeScript imports only needed for the data-point-based value-getter
 * @param datasetValueExpression the TypeScript expression used to read the value for the dataset-based
 * value-getter, defaults to [getTypescriptFieldAccessor] with `valueAccessor = true`
 * @param buildBody builds the lambda body from a value expression (either [datasetValueExpression] or
 * [dataPointValueExpression])
 */
@Suppress("LongParameterList")
fun ComponentBase.addDocumentSupportedValueCell(
    sectionConfigBuilder: SectionConfigBuilder,
    formatterImports: Set<TypeScriptImport>,
    dataPointValueExpression: String,
    additionalDataPointImports: Set<TypeScriptImport> = emptySet(),
    datasetValueExpression: String = getTypescriptFieldAccessor(true),
    buildBody: (valueExpression: String) -> String,
) {
    val fieldAccessor = getTypescriptFieldAccessor()
    sectionConfigBuilder.addStandardCellWithValueGetterFactory(
        this,
        documentSupport.getFrameworkDisplayValueLambda(
            FrameworkDisplayValueLambda(
                buildBody(datasetValueExpression),
                formatterImports,
            ),
            label, fieldAccessor,
        ),
        valueGetterByDataPoint =
            documentSupport.getFrameworkDisplayValueByDataPointLambda(
                FrameworkDisplayValueByDataPointLambda(
                    buildBody(dataPointValueExpression),
                    formatterImports + EXTRACT_DATAPOINT_VALUE_IMPORT + additionalDataPointImports,
                ),
                label, fieldAccessor,
            ),
    )
}

/**
 * Adds a standard cell to the section whose value-getters are NOT wrapped with the component's DocumentSupport.
 * The data-point-based value-getter reads the whole data point via `parseDataPoint`.
 *
 * @param sectionConfigBuilder the section to add the cell to
 * @param formatterImports the TypeScript imports required by [buildBody]'s formatter function
 * @param dataPointValueExpression the TypeScript expression used to read the value for the data-point-based
 * value-getter, typically `parseDataPoint(dataPoint) as <Type>`
 * @param additionalDataPointImports extra TypeScript imports only needed for the data-point-based value-getter
 * @param datasetValueExpression the TypeScript expression used to read the value for the dataset-based
 * value-getter, defaults to [getTypescriptFieldAccessor]
 * @param buildBody builds the lambda body from a value expression (either [datasetValueExpression] or
 * [dataPointValueExpression])
 */
@Suppress("LongParameterList")
fun ComponentBase.addParsedDataPointValueCell(
    sectionConfigBuilder: SectionConfigBuilder,
    formatterImports: Set<TypeScriptImport>,
    dataPointValueExpression: String,
    additionalDataPointImports: Set<TypeScriptImport> = emptySet(),
    datasetValueExpression: String = getTypescriptFieldAccessor(),
    buildBody: (valueExpression: String) -> String,
) {
    sectionConfigBuilder.addStandardCellWithValueGetterFactory(
        this,
        FrameworkDisplayValueLambda(
            buildBody(datasetValueExpression),
            formatterImports,
        ),
        valueGetterByDataPoint =
            FrameworkDisplayValueByDataPointLambda(
                buildBody(dataPointValueExpression),
                formatterImports + PARSE_DATAPOINT_IMPORT + additionalDataPointImports,
            ),
    )
}

/**
 * Convenience wrapper around [addDocumentSupportedValueCell] for the common case of a single-argument
 * formatter call of the form `formatterFunction(value)`.
 *
 * @param sectionConfigBuilder the section to add the cell to
 * @param formatterFunction the name of the TypeScript formatter function
 * @param formatterModule the module the formatter function is imported from
 * @param dataPointCastType the TypeScript type the extracted datapoint value is cast to
 * @param additionalDataPointImports extra TypeScript imports only needed for the data-point-based value-getter
 */
fun ComponentBase.addSingleArgumentFormatterCell(
    sectionConfigBuilder: SectionConfigBuilder,
    formatterFunction: String,
    formatterModule: String,
    dataPointCastType: String,
    additionalDataPointImports: Set<TypeScriptImport> = emptySet(),
) = addDocumentSupportedValueCell(
    sectionConfigBuilder,
    formatterImports = setOf(TypeScriptImport(formatterFunction, formatterModule)),
    dataPointValueExpression = "extractDatapointValue(dataPoint) as $dataPointCastType",
    additionalDataPointImports = additionalDataPointImports,
) { valueExpression -> "$formatterFunction($valueExpression)" }
