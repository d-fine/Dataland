package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.components.ViewConfigTestUtils.assertGettersDifferOnlyInValueExpression
import org.dataland.frameworktoolbox.intermediate.components.ViewConfigTestUtils.newSection
import org.dataland.frameworktoolbox.intermediate.components.ViewConfigTestUtils.onlyCell
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

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
        val section = newSection()
        component<FreeTextComponent>().generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        assertEquals("formatFreeTextForDatatable($DATASET_ACCESSOR)", cell.valueGetter.lambdaBody)
        assertEquals(
            "formatFreeTextForDatatable((extractDatapointValue(dataPoint) as string))",
            cell.valueGetterByDataPoint!!.lambdaBody,
        )
        assertFalse(cell.valueGetter.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
        assertTrue(cell.valueGetterByDataPoint!!.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
    }

    @Test
    fun `AmountWithCurrencyComponent - check that it reads the whole data point and imports the backend type`() {
        val section = newSection()
        component<AmountWithCurrencyComponent>().generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        val dataPointExpression = "(parseDataPoint(dataPoint) as AmountWithCurrency)"
        assertEquals(
            "formatStringForDatatable(\nformatAmountWithCurrency($DATASET_ACCESSOR)\n)",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "formatStringForDatatable(\nformatAmountWithCurrency($dataPointExpression)\n)",
            cell.valueGetterByDataPoint!!.lambdaBody,
        )

        val backendTypeImport = TypeScriptImport("type AmountWithCurrency", "@clients/backend")
        assertTrue(cell.valueGetterByDataPoint!!.imports.contains(backendTypeImport))
        assertFalse(cell.valueGetter.imports.contains(backendTypeImport))
    }

    @Test
    fun `ListOfStringBaseDataPointComponent - check that it propagates the column headers into both getters`() {
        val section = newSection()
        component<ListOfStringBaseDataPointComponent> {
            descriptionColumnHeader = "Custom Description"
            documentColumnHeader = "Custom Document"
        }.generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        val dataPointExpression = "(parseDataPoint(dataPoint) as Parameters<typeof formatListOfBaseDataPoint>[1])"

        listOf(cell.valueGetter.lambdaBody, cell.valueGetterByDataPoint!!.lambdaBody).forEach { body ->
            assertContains(body, "'$LABEL'")
            assertContains(body, "\"Custom Description\"")
            assertContains(body, "\"Custom Document\"")
        }
        assertGettersDifferOnlyInValueExpression(cell, DATASET_ACCESSOR, dataPointExpression)
        assertTrue(cell.valueGetterByDataPoint!!.imports.contains(PARSE_DATAPOINT_IMPORT))
    }

    @Test
    fun `NaceCodesComponent - check that it generates a matching pair of value getters`() {
        val section = newSection()
        component<NaceCodesComponent>().generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        assertEquals(
            "formatNaceCodesForDatatable(\n$DATASET_ACCESSOR,\n'$LABEL',\n)",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "formatNaceCodesForDatatable(\n(extractDatapointValue(dataPoint) as string[] | null | undefined),\n" +
                "'$LABEL',\n)",
            cell.valueGetterByDataPoint!!.lambdaBody,
        )
    }

    @Test
    fun `MultiSelectComponent - check that it embeds the same option mapping in both getters`() {
        val section = newSection()
        component<MultiSelectComponent> {
            options = setOf(SelectionOption("yes", "Yes"), SelectionOption("no", "No"))
        }.generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        val dataPointExpression = "(extractDatapointValue(dataPoint) as string[] | null | undefined)"
        val expectedMapping = "const mappings = {\n    yes: \"Yes\",\n    no: \"No\",\n}\n"

        listOf(cell.valueGetter.lambdaBody, cell.valueGetterByDataPoint!!.lambdaBody).forEach { body ->
            assertContains(body, expectedMapping)
            assertContains(body, "getOriginalNameFromTechnicalName(it, mappings)")
        }
        assertGettersDifferOnlyInValueExpression(cell, DATASET_ACCESSOR, dataPointExpression)
    }

    @Test
    fun `Iso2CountryCodesMultiSelectComponent - check that its datapoint getter omits the substring dataset`() {
        val section = newSection()
        component<Iso2CountryCodesMultiSelectComponent>().generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        val dataPointExpression = "(extractDatapointValue(dataPoint) as string[] | null | undefined)"
        val mappingPrelude = "const mappings = getDatasetAsMap(DropdownDatasetIdentifier.CountryCodesIso2);"

        listOf(cell.valueGetter.lambdaBody, cell.valueGetterByDataPoint!!.lambdaBody).forEach { body ->
            assertContains(body, mappingPrelude)
        }
        assertGettersDifferOnlyInValueExpression(cell, DATASET_ACCESSOR, dataPointExpression)

        // ViewConfig.ts.ftl picks the lambda parameter via FrameworkLambda.usesDataset / usesDataPoint, which are
        // case-sensitive `lambdaBody.contains(...)` checks. This body embeds `getDatasetAsMap` and
        // `DropdownDatasetIdentifier`, which only escape `usesDataset` because of their capital "D". Introducing a
        // lower-case `dataset` here (e.g. by renaming the helper or adding a local) would make the template emit
        // `(dataset: XData)` for a getter that is actually called with a data-point string.
        assertTrue(cell.valueGetterByDataPoint!!.usesDataPoint)
        assertFalse(cell.valueGetterByDataPoint!!.usesDataset)
        assertTrue(cell.valueGetter.usesDataset)

        assertTrue(cell.valueGetterByDataPoint!!.imports.contains(EXTRACT_DATAPOINT_VALUE_IMPORT))
        assertTrue(cell.valueGetter.imports.all { cell.valueGetterByDataPoint!!.imports.contains(it) })
    }
}
