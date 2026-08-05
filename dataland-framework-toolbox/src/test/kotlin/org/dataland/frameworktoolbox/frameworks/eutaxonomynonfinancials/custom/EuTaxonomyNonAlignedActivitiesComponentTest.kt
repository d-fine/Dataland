package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.dataland.frameworktoolbox.intermediate.components.newSection
import org.dataland.frameworktoolbox.intermediate.components.onlyCell
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

private const val FORMATTER = "formatNonAlignedActivitiesForDataTable"
private const val FIELD = "nonAlignedActivities"

class EuTaxonomyNonAlignedActivitiesComponentTest {
    @Test
    fun `generateDefaultViewConfig - check that it derives the kpi type from the enclosing component group`() {
        val group = ComponentGroup("revenue", DemoComponentGroupApiImpl())
        val component = EuTaxonomyNonAlignedActivitiesComponent(FIELD, group).also { it.label = "My Field" }

        val section = newSection()
        component.generateDefaultViewConfig(section)

        val cell = onlyCell(section)
        assertEquals(
            "$FORMATTER(dataset.revenue?.$FIELD,\"My Field\", \"revenue\")",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "$FORMATTER(parseDataPoint(dataPoint) as Parameters<typeof $FORMATTER>[0], \"My Field\", \"revenue\")",
            cell.valueGetterByDataPoint!!.lambdaBody,
        )

        assertTrue(cell.valueGetterByDataPoint!!.usesDataPoint)
        assertFalse(cell.valueGetterByDataPoint!!.usesDataset)
        assertTrue(cell.valueGetter.usesDataset)
    }

    @Test
    fun `generateDefaultViewConfig - check that it truncates the kpi type when not nested in a component group`() {
        val component =
            EuTaxonomyNonAlignedActivitiesComponent(FIELD, DemoComponentGroupApiImpl()).also { it.label = "My Field" }

        val section = newSection()
        component.generateDefaultViewConfig(section)

        // Characterisation test, not intended behaviour: the kpi type is derived via
        // `getTypescriptFieldAccessor().split(".")[1].dropLast(1)`, which is only correct while the component is
        // nested in a ComponentGroup (where `dropLast(1)` strips the trailing "?" of the null-safe accessor).
        // Without a group parent the accessor is `dataset.<field>` and the last character of the field name is eaten.
        val cell = onlyCell(section)
        listOf(cell.valueGetter.lambdaBody, cell.valueGetterByDataPoint!!.lambdaBody).forEach { body ->
            assertContains(body, "\"nonAlignedActivitie\")")
        }
    }
}
