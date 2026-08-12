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
 * Describes how the dataset-based and the data-point-based value-getter lambdas of a standard cell are built.
 *
 * The data-point-based value expression itself is not part of this spec: it is derived by the consuming function,
 * which pairs the correct reader (`extractDatapointValue` or `parseDataPoint`) with the matching TypeScript import.
 * Only the cast type varies per component. The derived expression is always parenthesized so that it stays valid
 * in every context a [buildBody] may place it in (call argument, ternary condition, `?.` receiver).
 *
 * @param formatterImports the TypeScript imports required by [buildBody]'s formatter function
 * @param dataPointCastType the TypeScript type the read datapoint value is cast to
 * @param additionalDataPointImports extra TypeScript imports only needed for the data-point-based value-getter
 * @param buildBody builds the lambda body from a value expression (either the dataset field accessor or the
 * derived data-point value expression)
 */
data class ValueGetterSpec(
    val formatterImports: Set<TypeScriptImport>,
    val dataPointCastType: String,
    val additionalDataPointImports: Set<TypeScriptImport> = emptySet(),
    val buildBody: (valueExpression: String) -> String,
)

/**
 * Adds a standard cell to the section whose value-getters are wrapped with the component's DocumentSupport
 * (e.g., BaseDataPoint / ExtendedDataPoint wrapping). The data-point-based value-getter reads the raw
 * datapoint value via `extractDatapointValue`.
 *
 * @param sectionConfigBuilder the section to add the cell to
 * @param valueGetterSpec describes how both value-getters are built; see [ValueGetterSpec]. The dataset-based
 * value-getter reads [getTypescriptFieldAccessor] with `valueAccessor = true`, i.e. the document-support-aware
 * accessor that unwraps `.value` where the DocumentSupport requires it.
 */
fun ComponentBase.addDocumentSupportedValueCell(
    sectionConfigBuilder: SectionConfigBuilder,
    valueGetterSpec: ValueGetterSpec,
) {
    val datasetValueExpression = getTypescriptFieldAccessor(true)
    val dataPointValueExpression = "(extractDatapointValue(dataPoint) as ${valueGetterSpec.dataPointCastType})"
    sectionConfigBuilder.addStandardCellWithValueGetterFactory(
        this,
        documentSupport.getFrameworkDisplayValueLambda(
            FrameworkDisplayValueLambda(
                valueGetterSpec.buildBody(datasetValueExpression),
                valueGetterSpec.formatterImports,
            ),
            label, getTypescriptFieldAccessor(),
        ),
        valueGetterByDataPoint =
            documentSupport.getFrameworkDisplayValueByDataPointLambda(
                FrameworkDisplayValueByDataPointLambda(
                    valueGetterSpec.buildBody(dataPointValueExpression),
                    valueGetterSpec.formatterImports + EXTRACT_DATAPOINT_VALUE_IMPORT +
                        valueGetterSpec.additionalDataPointImports,
                ),
                label,
            ),
    )
}

/**
 * Adds a standard cell to the section whose value-getters are NOT wrapped with the component's DocumentSupport.
 * The formatter referenced by [ValueGetterSpec.buildBody] is expected to render the document affordance itself,
 * which is why the data-point-based value-getter reads the whole data point via `parseDataPoint` instead of just
 * its value.
 *
 * This is independent of whether the component has document support: components using this function may well
 * require a document support (e.g. CurrencyComponent requires ExtendedDocumentSupport). "Non document supported"
 * refers only to the absence of the DocumentSupport wrapping around the generated value-getters.
 *
 * @param sectionConfigBuilder the section to add the cell to
 * @param valueGetterSpec describes how both value-getters are built; see [ValueGetterSpec]. The dataset-based
 * value-getter reads the raw [getTypescriptFieldAccessor], i.e. the whole data point rather than its `.value`,
 * because the formatter consumes the whole data point.
 */
fun ComponentBase.addNonDocumentSupportedValueCell(
    sectionConfigBuilder: SectionConfigBuilder,
    valueGetterSpec: ValueGetterSpec,
) {
    val datasetValueExpression = getTypescriptFieldAccessor()
    val dataPointValueExpression = "(parseDataPoint(dataPoint) as ${valueGetterSpec.dataPointCastType})"
    sectionConfigBuilder.addStandardCellWithValueGetterFactory(
        this,
        FrameworkDisplayValueLambda(
            valueGetterSpec.buildBody(datasetValueExpression),
            valueGetterSpec.formatterImports,
        ),
        valueGetterByDataPoint =
            FrameworkDisplayValueByDataPointLambda(
                valueGetterSpec.buildBody(dataPointValueExpression),
                valueGetterSpec.formatterImports + PARSE_DATAPOINT_IMPORT +
                    valueGetterSpec.additionalDataPointImports,
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
    ValueGetterSpec(
        formatterImports = setOf(TypeScriptImport(formatterFunction, formatterModule)),
        dataPointCastType = dataPointCastType,
        additionalDataPointImports = additionalDataPointImports,
    ) { valueExpression -> "$formatterFunction($valueExpression)" },
)
