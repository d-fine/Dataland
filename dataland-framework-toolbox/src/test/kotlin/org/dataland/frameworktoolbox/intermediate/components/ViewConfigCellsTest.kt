package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull

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

    @Test
    fun `addSingleArgumentFormatterCell check if dataset and datapoint getters are build`() {
        val component = stringComponent(NoDocumentSupport)
        val section = ViewConfigTestUtils.newSection()

        component.addSingleArgumentFormatterCell(
            section,
            formatterFunction = "formatStringForDatatable",
            formatterModule = "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory",
            dataPointCastType = "string",
        )

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        assertEquals("formatStringForDatatable(dataset.myField)", cell.valueGetter.lambdaBody)
        assertEquals(
            "formatStringForDatatable((extractDatapointValue(dataPoint) as string))",
            byDataPoint.lambdaBody,
        )
        assertEquals(cell.valueGetter.imports + EXTRACT_DATAPOINT_VALUE_IMPORT, byDataPoint.imports)
        assertEquals(cell.valueGetter.imports + byDataPoint.imports, cell.imports)
    }

    @Test
    fun `addDocumentSupportedValueCell - check if both getters are build and only the datapoint getter references dataPoint`() {
        val component = stringComponent(SimpleDocumentSupport)
        val section = ViewConfigTestUtils.newSection()

        component.addDocumentSupportedValueCell(
            section,
            ValueGetterSpec(
                formatterImports = setOf(FORMATTER_IMPORT),
                dataPointCastType = "string",
            ) { valueExpression -> "formatX($valueExpression)" },
        )

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)

        assertEquals(
            "wrapDisplayValueWithDatapointInformation(formatX(dataset.myField?.value), \"My Field\", dataset.myField)",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "wrapDisplayValueWithDatapointInformationByDataPoint(" +
                "formatX((extractDatapointValue(dataPoint) as string)), \"My Field\", dataPoint)",
            byDataPoint.lambdaBody,
        )

        assertTrue(byDataPoint.usesDataPoint)
        assertFalse(byDataPoint.usesDataset)
        assertTrue(cell.valueGetter.usesDataset)
    }

    @Test
    fun `additionalDataPointImports - check that it only creates imports for the datapoint getter`() {
        val component = stringComponent(NoDocumentSupport)
        val section = ViewConfigTestUtils.newSection()
        val extraImport = TypeScriptImport("type YesNoNa", "@clients/backend")

        component.addDocumentSupportedValueCell(
            section,
            ValueGetterSpec(
                formatterImports = setOf(FORMATTER_IMPORT),
                dataPointCastType = "YesNoNa",
                additionalDataPointImports = setOf(extraImport),
            ) { valueExpression -> "formatX($valueExpression)" },
        )

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        assertTrue(byDataPoint.imports.contains(extraImport))
        assertFalse(cell.valueGetter.imports.contains(extraImport))
    }

    @Test
    fun `addNonDocumentSupportedValueCell - check that it skips document support wrapping and uses parseDataPoint`() {
        val component = stringComponent(SimpleDocumentSupport)
        val section = ViewConfigTestUtils.newSection()

        component.addNonDocumentSupportedValueCell(
            section,
            ValueGetterSpec(
                formatterImports = setOf(FORMATTER_IMPORT),
                dataPointCastType = "string",
            ) { valueExpression -> "formatX($valueExpression)" },
        )

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)

        assertFalse(cell.valueGetter.lambdaBody.contains("wrapDisplayValueWithDatapointInformation"))
        assertFalse(byDataPoint.lambdaBody.contains("wrapDisplayValueWithDatapointInformation"))

        assertEquals("formatX(dataset.myField)", cell.valueGetter.lambdaBody)
        assertEquals("formatX((parseDataPoint(dataPoint) as string))", byDataPoint.lambdaBody)

        assertTrue(byDataPoint.imports.contains(PARSE_DATAPOINT_IMPORT))
        assertFalse(byDataPoint.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
    }

    @Test
    fun `dataset getter - check that only the document supported variant unwraps the value accessor`() {
        val documentSupportedSection = ViewConfigTestUtils.newSection()
        stringComponent(SimpleDocumentSupport).addDocumentSupportedValueCell(
            documentSupportedSection,
            ValueGetterSpec(formatterImports = emptySet(), dataPointCastType = "string") { "PRE[$it]POST" },
        )
        assertContains(ViewConfigTestUtils.onlyCell(documentSupportedSection).valueGetter.lambdaBody, "PRE[dataset.myField?.value]POST")

        val nonDocumentSupportedSection = ViewConfigTestUtils.newSection()
        stringComponent(SimpleDocumentSupport).addNonDocumentSupportedValueCell(
            nonDocumentSupportedSection,
            ValueGetterSpec(formatterImports = emptySet(), dataPointCastType = "string") { "PRE[$it]POST" },
        )
        assertEquals(
            "PRE[dataset.myField]POST",
            ViewConfigTestUtils.onlyCell(nonDocumentSupportedSection).valueGetter.lambdaBody,
        )
    }
}
