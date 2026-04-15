package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointCount
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportSecurityPolicy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataPointQaReportManagerTest {
    private val mockDataPointControllerApi: DataPointControllerApi = mock()
    private val mockRepo: DataPointQaReportRepository = mock()
    private val mockSecurityPolicy: QaReportSecurityPolicy = mock()

    private val dataPointQaReportManager =
        DataPointQaReportManager(
            mockDataPointControllerApi,
            mockRepo,
            mockSecurityPolicy,
        )

    @Test
    fun `returns empty map for empty input`() {
        val result = dataPointQaReportManager.countQaReportsForDataPointIdsBulk(emptySet())

        Assertions.assertEquals(emptyMap<String, Long>(), result)
        verify(mockRepo, never()).countByDataPointIdInGrouped(any())
    }

    @Test
    fun `splits into batches and aggregates results`() {
        // read the (private) MAX_DATA_POINT_IDS_PER_BATCH from the manager via reflection
        // and create a set that is one larger so the implementation must split it into batches
        val max = DataPointQaReportManager.MAX_DATA_POINT_IDS_PER_BATCH
        val manyIds: Set<String> = (0..max).map { "id-$it" }.toSet()
        // sanity-check for readers: manyIds size is max + 1 (e.g. 50001 when max == 50000)
        Assertions.assertEquals(max + 1, manyIds.size)

        whenever(mockRepo.countByDataPointIdInGrouped(any()))
            .thenAnswer {
                listOf(
                    object : DataPointCount {
                        override fun getDataPointId(): String = "shared-id"

                        override fun getQaReportCount(): Long = 1L
                    },
                )
            }.thenAnswer {
                listOf(
                    object : DataPointCount {
                        override fun getDataPointId(): String = "shared-id"

                        override fun getQaReportCount(): Long = 1L
                    },
                )
            }

        val result = dataPointQaReportManager.countQaReportsForDataPointIdsBulk(manyIds)

        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals(2L, result.getValue("shared-id"))

        verify(mockRepo, times(2)).countByDataPointIdInGrouped(any())
    }
}
