package org.dataland.datalandcommunitymanager

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MappingCompletenessTest {
    @Test
    fun `validate that the data type to readable name mapping is complete`() {
        assertEquals(DataTypeEnum.entries.size, readableFrameworkNameMapping.size)
    }
}
