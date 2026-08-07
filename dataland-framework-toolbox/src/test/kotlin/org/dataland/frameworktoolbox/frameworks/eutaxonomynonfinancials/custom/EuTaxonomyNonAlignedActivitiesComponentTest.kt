package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.dataland.frameworktoolbox.intermediate.components.ViewConfigTestUtils
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

private const val FORMATTER = "formatNonAlignedActivitiesForDataTable"
private const val FIELD = "nonAlignedActivities"

class EuTaxonomyNonAlignedActivitiesComponentTest {
    @Test
    fun `generateDefaultViewConfig - check that it derives the kpi type from the enclosing component group`() {
        val group = ComponentGroup("revenue", DemoComponentGroupApiImpl())
        val component = EuTaxonomyNonAlignedActivitiesComponent(FIELD, group).also { it.label = "My Field" }

        val section = ViewConfigTestUtils.newSection()
        component.generateDefaultViewConfig(section)

        val cell = ViewConfigTestUtils.onlyCell(section)
        val byDataPoint = assertNotNull(cell.valueGetterByDataPoint)
        assertEquals(
            "$FORMATTER(dataset.revenue?.$FIELD,\"My Field\", \"revenue\")",
            cell.valueGetter.lambdaBody,
        )
        assertEquals(
            "$FORMATTER(parseDataPoint(dataPoint) as Parameters<typeof $FORMATTER>[0], \"My Field\", \"revenue\")",
            byDataPoint.lambdaBody,
        )

        assertTrue(byDataPoint.usesDataPoint)
        assertFalse(byDataPoint.usesDataset)
        assertTrue(cell.valueGetter.usesDataset)
    }
}
