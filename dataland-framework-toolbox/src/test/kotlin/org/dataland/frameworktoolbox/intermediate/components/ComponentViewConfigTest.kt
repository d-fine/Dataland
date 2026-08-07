package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.components.ViewConfigTestUtils
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull

private const val FIELD = "myField"
private const val LABEL = "My Field"
private const val DATASET_ACCESSOR = "dataset.$FIELD"

private val EXTRACT_DATAPOINT_VALUE_IMPORT =
    TypeScriptImport("extractDatapointValue", "@/components/resources/dataTable/conversion/DataPoints")

private val PARSE_DATAPOINT_IMPORT =
    TypeScriptImport("parseDataPoint", "@/components/resources/dataTable/conversion/DataPoints")

class ComponentViewConfigTest {
    private inline fun <reified T : ComponentBase> component(noinline init: (T.() -> Unit)? = null): T =
        DemoComponentGroupApiImpl().create<T>(FIELD) {
            label = LABEL
            init?.invoke(this)
        }

    @Test
    fun `FreeTextComponent - check that it generates a matching pair of value getters`() {
        val section = ViewConfigTestUtils.newSection()
        component<FreeTextComponent>().generateDefaultViewConfig(section)

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        assertEquals("formatFreeTextForDatatable($DATASET_ACCESSOR)", cell.valueGetter.lambdaBody)
        assertEquals(
            "formatFreeTextForDatatable((extractDatapointValue(dataPoint) as string))",
            byDataPoint.lambdaBody,
        )
        assertFalse(cell.valueGetter.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
        assertTrue(byDataPoint.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
    }

    @Test
    fun `AmountWithCurrencyComponent - check that it reads the whole data point and imports the backend type`() {
        val section = ViewConfigTestUtils.newSection()
        component<AmountWithCurrencyComponent>().generateDefaultViewConfig(section)

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        val dataPointExpression = "(parseDataPoint(dataPoint) as AmountWithCurrency)"
        assertEquals(
            "formatStringForDatatable(\nformatAmountWithCurrency($DATASET_ACCESSOR)\n)",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "formatStringForDatatable(\nformatAmountWithCurrency($dataPointExpression)\n)",
            byDataPoint.lambdaBody,
        )

        val backendTypeImport = TypeScriptImport("type AmountWithCurrency", "@clients/backend")
        assertTrue(byDataPoint.imports.contains(backendTypeImport))
        assertFalse(cell.valueGetter.imports.contains(backendTypeImport))
    }

    @Test
    fun `ListOfStringBaseDataPointComponent - check that it propagates the column headers into both getters`() {
        val section = ViewConfigTestUtils.newSection()
        component<ListOfStringBaseDataPointComponent> {
            descriptionColumnHeader = "Custom Description"
            documentColumnHeader = "Custom Document"
        }.generateDefaultViewConfig(section)

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        val dataPointExpression = "(parseDataPoint(dataPoint) as Parameters<typeof formatListOfBaseDataPoint>[1])"

        listOf(cell.valueGetter.lambdaBody, byDataPoint.lambdaBody).forEach { body ->
            assertContains(body, "'$LABEL'")
            assertContains(body, "\"Custom Description\"")
            assertContains(body, "\"Custom Document\"")
        }
        ViewConfigTestUtils.assertGettersDifferOnlyInValueExpression(cell, DATASET_ACCESSOR, dataPointExpression)
        assertTrue(byDataPoint.imports.contains(PARSE_DATAPOINT_IMPORT))
    }

    @Test
    fun `NaceCodesComponent - check that it generates a matching pair of value getters`() {
        val section = ViewConfigTestUtils.newSection()
        component<NaceCodesComponent>().generateDefaultViewConfig(section)

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        assertEquals(
            "formatNaceCodesForDatatable(\n$DATASET_ACCESSOR,\n'$LABEL',\n)",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "formatNaceCodesForDatatable(\n(extractDatapointValue(dataPoint) as string[] | null | undefined),\n" +
                "'$LABEL',\n)",
            byDataPoint.lambdaBody,
        )
    }

    @Test
    fun `MultiSelectComponent - check that it embeds the same option mapping in both getters`() {
        val section = ViewConfigTestUtils.newSection()
        component<MultiSelectComponent> {
            options = setOf(SelectionOption("yes", "Yes"), SelectionOption("no", "No"))
        }.generateDefaultViewConfig(section)

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        val dataPointExpression = "(extractDatapointValue(dataPoint) as string[] | null | undefined)"
        val expectedMapping = "const mappings = {\n    yes: \"Yes\",\n    no: \"No\",\n}\n"

        listOf(cell.valueGetter.lambdaBody, byDataPoint.lambdaBody).forEach { body ->
            assertContains(body, expectedMapping)
            assertContains(body, "getOriginalNameFromTechnicalName(it, mappings)")
        }
        ViewConfigTestUtils.assertGettersDifferOnlyInValueExpression(cell, DATASET_ACCESSOR, dataPointExpression)
    }

    @Test
    fun `Iso2CountryCodesMultiSelectComponent - check that its datapoint getter omits the substring dataset`() {
        val section = ViewConfigTestUtils.newSection()
        component<Iso2CountryCodesMultiSelectComponent>().generateDefaultViewConfig(section)

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        val dataPointExpression = "(extractDatapointValue(dataPoint) as string[] | null | undefined)"
        val mappingPrelude = "const mappings = getDatasetAsMap(DropdownDatasetIdentifier.CountryCodesIso2);"

        listOf(cell.valueGetter.lambdaBody, byDataPoint.lambdaBody).forEach { body ->
            assertContains(body, mappingPrelude)
        }
        ViewConfigTestUtils.assertGettersDifferOnlyInValueExpression(cell, DATASET_ACCESSOR, dataPointExpression)

        assertTrue(byDataPoint.usesDataPoint)
        assertFalse(byDataPoint.usesDataset)
        assertTrue(cell.valueGetter.usesDataset)

        assertTrue(byDataPoint.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
        assertTrue(cell.valueGetter.imports.all { byDataPoint.imports.contains(it) })
    }
}
