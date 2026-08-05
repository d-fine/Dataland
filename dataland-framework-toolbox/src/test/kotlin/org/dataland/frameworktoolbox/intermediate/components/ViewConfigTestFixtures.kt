package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.specific.viewconfig.elements.CellConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Creates an empty root section to generate view-config cells into.
 */
fun newSection(): SectionConfigBuilder =
    SectionConfigBuilder(
        parentSection = null,
        label = "root",
        expandOnPageLoad = false,
        shouldDisplay = FrameworkBooleanLambda.TRUE,
    )

/**
 * Returns the single cell that was generated into the given section.
 */
fun onlyCell(section: SectionConfigBuilder): CellConfigBuilder = section.children.single() as CellConfigBuilder

/**
 * Asserts that the dataset-based and the data-point-based value-getter of a cell are structurally identical,
 * i.e. that they only differ in the value expression that is substituted into the shared body.
 */
fun assertGettersDifferOnlyInValueExpression(
    cell: CellConfigBuilder,
    datasetValueExpression: String,
    dataPointValueExpression: String,
) {
    val placeholder = "<VALUE>"
    assertEquals(
        cell.valueGetter.lambdaBody.replace(datasetValueExpression, placeholder),
        cell.valueGetterByDataPoint!!.lambdaBody.replace(dataPointValueExpression, placeholder),
    )
}
