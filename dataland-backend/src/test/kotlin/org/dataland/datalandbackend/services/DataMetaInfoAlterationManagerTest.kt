package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackendutils.model.QaStatus
import org.junit.jupiter.api.Assertions.assertEquals
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
    private val initialUploaderUserId = "initialUploaderUserId"
    private val correlationId = "correlationId"

    private val storableDataset =
        StorableDataset(
            companyId = "dummyCompanyId",
            dataType = DataType.of(SfdrData::class.java),
            uploaderUserId = initialUploaderUserId,
            uploadTime = 0L,
            reportingPeriod = "reportingPeriod",
            data = "dummyData",
        )

    private val partiallyMockedDataMetaInformationEntity =
        DataMetaInformationEntity(
            dataId,
            company = mock<StoredCompanyEntity>(),
            dataType = DataType.of(SfdrData::class.java).toString(),
            uploaderUserId = initialUploaderUserId,
            uploadTime = 0,
            reportingPeriod = "reportingPeriod",
            currentlyActive = true,
            qaStatus = mock<QaStatus>(),
        )

    @BeforeEach
    fun setup() {
        reset(mockDataMetaInformationManager, mockDataManager)

        doReturn(partiallyMockedDataMetaInformationEntity)
            .whenever(mockDataMetaInformationManager)
            .getDataMetaInformationByDataId(any())
        doNothing().whenever(mockDataManager).storeDatasetInTemporaryStoreAndSendPatchMessage(any(), any(), any())
        doReturn(storableDataset).whenever(mockDataManager).getPublicDataset(any(), any(), any())

        dataMetaInfoAlterationManager =
            DataMetaInfoAlterationManager(
                mockDataMetaInformationManager,
                mockDataManager,
            )
    }

    @Test
    fun `test that patch functionality runs as expected on happy path patching metaInfo and storableDataset`() {
        val newUploaderUserId = "newUploaderUserId"
        val dataMetaInformationPatch = DataMetaInformationPatch(newUploaderUserId)

        assertDoesNotThrow {
            dataMetaInfoAlterationManager.patchDataMetaInformation(dataId, dataMetaInformationPatch, correlationId)
        }
        assertEquals(newUploaderUserId, partiallyMockedDataMetaInformationEntity.uploaderUserId)
        assertEquals(newUploaderUserId, storableDataset.uploaderUserId)
    }

    @Test
    fun `ensure that metaInfo and storableDataset are not patched if uploaderUserId is null`() {
        val dataMetaInformationPatch = DataMetaInformationPatch()

        assertDoesNotThrow {
            dataMetaInfoAlterationManager.patchDataMetaInformation(dataId, dataMetaInformationPatch, correlationId)
        }
        assertEquals(initialUploaderUserId, partiallyMockedDataMetaInformationEntity.uploaderUserId)
        assertEquals(initialUploaderUserId, storableDataset.uploaderUserId)
    }
}
