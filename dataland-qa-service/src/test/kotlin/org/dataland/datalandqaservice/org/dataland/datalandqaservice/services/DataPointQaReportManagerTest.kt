package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointCount
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataPointQaReportManagerTest {
    private val mockDataPointControllerApi: DataPointControllerApi = mock()
    private val mockRepo: DataPointQaReportRepository = mock()
    private val mockSecurityPolicy: QaReportSecurityPolicy = mock()

    private val manager =
        DataPointQaReportManager(
            mockDataPointControllerApi,
            mockRepo,
            mockSecurityPolicy,
        )

    @Test
    fun `returns empty map for empty input`() {
        val result = manager.countQaReportsForDataPointIdsBulk(emptySet())

        assertEquals(emptyMap<String, Long>(), result)
        verify(mockRepo, never()).countByDataPointIdInGrouped(any())
    }

    @Test
    fun `splits into batches and aggregates results`() {
        val manyIds: Set<String> = (0..50000).map { "id-$it" }.toSet()

        whenever(mockRepo.countByDataPointIdInGrouped(any())).doAnswer { invocation ->
            val batch = invocation.arguments[0] as Set<*>
            val first = batch.first() as String
            listOf(
                object : DataPointCount {
                    override fun getDataPointId(): String = first

                    override fun getQaReportCount(): Long = 1L
                })
        }

        val result = manager.countQaReportsForDataPointIdsBulk(manyIds)

        assertEquals(2, result.size)

        result.values.forEach { count -> assertEquals(1L, count) }
    }
}
