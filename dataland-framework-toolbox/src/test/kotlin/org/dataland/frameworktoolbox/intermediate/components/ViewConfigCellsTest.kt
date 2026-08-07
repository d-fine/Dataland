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
    fun `addDocumentSupportedValueCell - check if both getters are build only the datapoint getter references dataPoint`() {
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

        // This is the invariant the FreeMarker template relies on to pick the lambda's parameter signature
        // (see FrameworkLambda.usesDataset / usesDataPoint and ViewConfig.ts.ftl).
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

        // Unlike addDocumentSupportedValueCell, neither getter is wrapped with document-support information.
        assertFalse(cell.valueGetter.lambdaBody.contains("wrapDisplayValueWithDatapointInformation"))
        assertFalse(byDataPoint.lambdaBody.contains("wrapDisplayValueWithDatapointInformation"))

        // The default dataset expression is the raw field accessor, not the document-support value accessor.
        assertEquals("formatX(dataset.myField)", cell.valueGetter.lambdaBody)
        assertEquals("formatX((parseDataPoint(dataPoint) as string))", byDataPoint.lambdaBody)

        assertTrue(byDataPoint.imports.contains(PARSE_DATAPOINT_IMPORT))
        assertFalse(byDataPoint.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
    }

    @Test
    fun `dataset getter - check that only the document supported variant unwraps the value accessor`() {
        // addDocumentSupportedValueCell reads the document-support-aware accessor, so under a document support
        // that wraps the value it appends `?.value`; addNonDocumentSupportedValueCell always reads the raw
        // accessor, because its formatter consumes the whole data point.
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

    @Test
    fun `dataPointCastType - check that each function pairs its own reader function with the matching import`() {
        val documentSupportedSection = ViewConfigTestUtils.newSection()
        stringComponent(NoDocumentSupport).addDocumentSupportedValueCell(
            documentSupportedSection,
            ValueGetterSpec(formatterImports = emptySet(), dataPointCastType = "MyType") { it },
        )
        val documentSupportedGetter = assertNotNull(ViewConfigTestUtils.onlyCell(documentSupportedSection).valueGetterByDataPoint)

        val nonDocumentSupportedSection = ViewConfigTestUtils.newSection()
        stringComponent(NoDocumentSupport).addNonDocumentSupportedValueCell(
            nonDocumentSupportedSection,
            ValueGetterSpec(formatterImports = emptySet(), dataPointCastType = "MyType") { it },
        )
        val nonDocumentSupportedGetter = assertNotNull(ViewConfigTestUtils.onlyCell(nonDocumentSupportedSection).valueGetterByDataPoint)

        // The reader function is derived from the called function, so it can no longer be mismatched with the
        // import that same function adds.
        assertEquals("(extractDatapointValue(dataPoint) as MyType)", documentSupportedGetter.lambdaBody)
        assertTrue(documentSupportedGetter.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
        assertFalse(documentSupportedGetter.imports.contains(PARSE_DATAPOINT_IMPORT))

        assertEquals("(parseDataPoint(dataPoint) as MyType)", nonDocumentSupportedGetter.lambdaBody)
        assertTrue(nonDocumentSupportedGetter.imports.contains(PARSE_DATAPOINT_IMPORT))
        assertFalse(nonDocumentSupportedGetter.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
    }
}
