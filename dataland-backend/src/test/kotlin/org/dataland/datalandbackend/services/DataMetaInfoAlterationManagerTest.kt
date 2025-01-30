package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataMetaInfoAlterationManagerTest {
    private val mockDataMetaInformationManager = mock<DataMetaInformationManager>()
    private val mockDataManager: DataManager = mock<DataManager>()
    private lateinit var dataMetaInfoAlterationManager: DataMetaInfoAlterationManager

    private val dataId = "dummyDataId"
    private val dataMetaInformationPatch = DataMetaInformationPatch(uploaderUserId = "newUploaderUserId")
    private val correlationId = "correlationId"
    private val storableDataset =
        StorableDataset(
            companyId = "dummyCompanyId",
            dataType = DataType("sfdr"),
            uploaderUserId = "initialUploaderUserId",
            uploadTime = 0L,
            reportingPeriod = "somewhere in the second age",
            data = "dummyData",
        )

    @BeforeEach
    fun setup() {
        reset(mockDataMetaInformationManager, mockDataManager)

        doNothing().whenever(mockDataManager).storeDatasetInTemporaryStoreAndSendPatchMessage(any(), any(), any())
        doReturn(storableDataset).whenever(mockDataManager).getPublicDataset(any(), any(), any())

        dataMetaInfoAlterationManager =
            DataMetaInfoAlterationManager(
                mockDataMetaInformationManager,
                mockDataManager,
            )
    }

    @Test
    fun `test that patch functionality runs as expected on happy path`() {
        assertDoesNotThrow {
            dataMetaInfoAlterationManager.patchDataMetaInformation(dataId, dataMetaInformationPatch, correlationId)
        }
    }
}
