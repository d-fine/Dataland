package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.specific.viewconfig.elements.CellConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private val EXTRACT_DATAPOINT_VALUE_IMPORT =
    TypeScriptImport("extractDatapointValue", "@/components/resources/dataTable/conversion/DataPoints")

private val PARSE_DATAPOINT_IMPORT =
    TypeScriptImport("parseDataPoint", "@/components/resources/dataTable/conversion/DataPoints")

private val FORMATTER_IMPORT = TypeScriptImport("formatX", "@/components/resources/dataTable/conversion/FormatX")

class ViewConfigCellsTest {
    private fun stringComponent(support: DocumentSupport): StringComponent =
        DemoComponentGroupApiImpl().create<StringComponent>("myField") {
            label = "My Field"
            documentSupport = support
        }

    private fun newSection(): SectionConfigBuilder =
        SectionConfigBuilder(
            parentSection = null,
            label = "root",
            expandOnPageLoad = false,
            shouldDisplay = FrameworkBooleanLambda.TRUE,
        )

    private fun onlyCell(section: SectionConfigBuilder): CellConfigBuilder = section.children.single() as CellConfigBuilder

    @Test
    fun `addSingleArgumentFormatterCell should build parallel dataset and datapoint getters`() {
        val component = stringComponent(NoDocumentSupport)
        val section = newSection()

        component.addSingleArgumentFormatterCell(
            section,
            formatterFunction = "formatStringForDatatable",
            formatterModule = "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory",
            dataPointCastType = "string",
        )

        val cell = onlyCell(section)
        assertEquals("formatStringForDatatable(dataset.myField)", cell.valueGetter.lambdaBody)
        assertEquals(
            "formatStringForDatatable(extractDatapointValue(dataPoint) as string)",
            cell.valueGetterByDataPoint!!.lambdaBody,
        )
        assertEquals(cell.valueGetter.imports + EXTRACT_DATAPOINT_VALUE_IMPORT, cell.valueGetterByDataPoint!!.imports)
        assertEquals(cell.valueGetter.imports + cell.valueGetterByDataPoint!!.imports, cell.imports)
    }

    @Test
    fun `addDocumentSupportedValueCell should wrap both getters and only the datapoint getter should reference dataPoint`() {
        val component = stringComponent(SimpleDocumentSupport)
        val section = newSection()

        component.addDocumentSupportedValueCell(
            section,
            formatterImports = setOf(FORMATTER_IMPORT),
            dataPointValueExpression = "extractDatapointValue(dataPoint) as string",
        ) { valueExpression -> "formatX($valueExpression)" }

        val cell = onlyCell(section)

        assertEquals(
            "wrapDisplayValueWithDatapointInformation(formatX(dataset.myField?.value), \"My Field\", dataset.myField)",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "wrapDisplayValueWithDatapointInformationByDataPoint(" +
                "formatX(extractDatapointValue(dataPoint) as string), \"My Field\", dataPoint)",
            cell.valueGetterByDataPoint!!.lambdaBody,
        )

        // This is the invariant the FreeMarker template relies on to pick the lambda's parameter signature
        // (see FrameworkLambda.usesDataset / usesDataPoint and ViewConfig.ts.ftl).
        assertTrue(cell.valueGetterByDataPoint!!.usesDataPoint)
        assertFalse(cell.valueGetterByDataPoint!!.usesDataset)
        assertTrue(cell.valueGetter.usesDataset)
    }

    @Test
    fun `additionalDataPointImports should only reach the datapoint getter`() {
        val component = stringComponent(NoDocumentSupport)
        val section = newSection()
        val extraImport = TypeScriptImport("type YesNoNa", "@clients/backend")

        component.addDocumentSupportedValueCell(
            section,
            formatterImports = setOf(FORMATTER_IMPORT),
            dataPointValueExpression = "extractDatapointValue(dataPoint) as YesNoNa",
            additionalDataPointImports = setOf(extraImport),
        ) { valueExpression -> "formatX($valueExpression)" }

        val cell = onlyCell(section)
        assertTrue(cell.valueGetterByDataPoint!!.imports.contains(extraImport))
        assertFalse(cell.valueGetter.imports.contains(extraImport))
    }

    @Test
    fun `addParsedDataPointValueCell should skip document support wrapping and use parseDataPoint`() {
        val component = stringComponent(SimpleDocumentSupport)
        val section = newSection()

        component.addParsedDataPointValueCell(
            section,
            formatterImports = setOf(FORMATTER_IMPORT),
            dataPointValueExpression = "parseDataPoint(dataPoint) as string",
        ) { valueExpression -> "formatX($valueExpression)" }

        val cell = onlyCell(section)

        // Unlike addDocumentSupportedValueCell, neither getter is wrapped with document-support information.
        assertFalse(cell.valueGetter.lambdaBody.contains("wrapDisplayValueWithDatapointInformation"))
        assertFalse(cell.valueGetterByDataPoint!!.lambdaBody.contains("wrapDisplayValueWithDatapointInformation"))

        // The default dataset expression is the raw field accessor, not the document-support value accessor.
        assertEquals("formatX(dataset.myField)", cell.valueGetter.lambdaBody)
        assertEquals("formatX(parseDataPoint(dataPoint) as string)", cell.valueGetterByDataPoint!!.lambdaBody)

        assertTrue(cell.valueGetterByDataPoint!!.imports.contains(PARSE_DATAPOINT_IMPORT))
        assertFalse(cell.valueGetterByDataPoint!!.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
    }

    @Test
    fun `explicit datasetValueExpression should be honoured and buildBody should be applied identically to both getters`() {
        val component = stringComponent(NoDocumentSupport)
        val section = newSection()

        component.addDocumentSupportedValueCell(
            section,
            formatterImports = emptySet(),
            dataPointValueExpression = "extractDatapointValue(dataPoint) as string",
            datasetValueExpression = "dataset.custom",
        ) { valueExpression -> "PRE[$valueExpression]POST" }

        val cell = onlyCell(section)
        assertEquals("PRE[dataset.custom]POST", cell.valueGetter.lambdaBody)
        assertEquals("PRE[extractDatapointValue(dataPoint) as string]POST", cell.valueGetterByDataPoint!!.lambdaBody)
    }
}
