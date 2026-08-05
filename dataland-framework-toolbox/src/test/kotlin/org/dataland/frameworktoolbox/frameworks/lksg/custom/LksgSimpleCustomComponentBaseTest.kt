package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.dataland.frameworktoolbox.intermediate.components.newSection
import org.dataland.frameworktoolbox.intermediate.components.onlyCell
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

private const val FORMATTER = "formatSubcontractingCompaniesForDatatable"
private const val FORMATTER_MODULE = "@/components/resources/dataTable/conversion/lksg/LksgDisplayValueGetters"

class LksgSimpleCustomComponentBaseTest {
    private fun component(label: String): LksgSimpleCustomComponentBase =
        LksgSimpleCustomComponentBase(
            identifier = "subcontractingCompanies",
            parent = DemoComponentGroupApiImpl(),
            viewFormattingFunctionName = FORMATTER,
            uploadComponentName = "SubcontractingCompaniesFormField",
            guaranteedFixtureExpression = "dataGenerator.guaranteedSubcontractingCompanies()",
            randomFixtureExpression = null,
        ).also { it.label = label }

    @Test
    fun `generateDefaultViewConfig - check that both getters call the same formatter with matching arguments`() {
        val section = newSection()
        component("My Field").generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        assertEquals(
            "$FORMATTER(dataset.subcontractingCompanies, \"My Field\")",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "$FORMATTER(parseDataPoint(dataPoint) as Parameters<typeof $FORMATTER>[0], \"My Field\")",
            cell.valueGetterByDataPoint!!.lambdaBody,
        )

        // This component hand-writes both getters instead of routing through ViewConfigCells, so a copy-paste of the
        // wrong formatter name is the realistic regression here.
        assertTrue(cell.valueGetter.imports.contains(TypeScriptImport(FORMATTER, FORMATTER_MODULE)))
        assertTrue(cell.valueGetterByDataPoint!!.imports.contains(TypeScriptImport(FORMATTER, FORMATTER_MODULE)))
        assertTrue(
            cell.valueGetterByDataPoint!!.imports.contains(
                TypeScriptImport("parseDataPoint", "@/components/resources/dataTable/conversion/DataPoints"),
            ),
        )

        assertTrue(cell.valueGetterByDataPoint!!.usesDataPoint)
        assertFalse(cell.valueGetterByDataPoint!!.usesDataset)
        assertTrue(cell.valueGetter.usesDataset)
    }

    @Test
    fun `generateDefaultViewConfig - check that both getters escape the label for EcmaScript`() {
        val section = newSection()
        component("He said \"hi\"").generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        listOf(cell.valueGetter.lambdaBody, cell.valueGetterByDataPoint!!.lambdaBody).forEach { body ->
            assertContains(body, "\\\"hi\\\"")
        }
    }
}
