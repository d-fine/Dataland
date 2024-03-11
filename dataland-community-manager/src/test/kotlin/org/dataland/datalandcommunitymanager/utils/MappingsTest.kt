package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MappingsTest {
    @Test
    fun `validates that for each framework there is a mapping to a readible name`() {
        assertTrue(readableFrameworkNameMapping.keys.containsAll(DataTypeEnum.entries))
    }
}
