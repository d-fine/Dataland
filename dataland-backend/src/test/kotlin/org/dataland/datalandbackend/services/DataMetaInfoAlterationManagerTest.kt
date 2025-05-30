package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataMetaInfoAlterationManagerTest {
    private val mockDataManager: DataManager = mock<DataManager>()
    private val mockDataMetaInformationManager = mock<DataMetaInformationManager>()
    private val mockDataPointUtils = mock<DataPointUtils>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val mockMessageQueuePublications = mock<MessageQueuePublications>()
    private lateinit var dataMetaInfoAlterationManager: DataMetaInfoAlterationManager

    private val dataId = "dummyDataId"
    private val initialUploaderUserId = "initialUploaderUserId"
    private val newValidUploaderUserId = "newValidUploaderUserId"
    private val newUploaderUserIdUnknownToKeycloak = "newUploaderUserIdUnknownToKeycloak"
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
        initializePartialMocks()
        reset(mockDataManager, mockDataMetaInformationManager, mockDataManager, mockKeycloakUserService, mockMessageQueuePublications)

        doNothing().whenever(mockDataManager).storeDatasetInTemporaryStoreAndSendPatchMessage(any(), any(), any())
        doReturn(partiallyMockedDataMetaInformationEntity)
            .whenever(mockDataMetaInformationManager)
            .getDataMetaInformationByDataId(any())
        doReturn(storableDataset).whenever(mockDataManager).getPublicDataset(any(), any(), any())
        doReturn(null).whenever(mockDataPointUtils).getFrameworkSpecificationOrNull(any())
        doNothing().whenever(mockMessageQueuePublications).publishDatasetMetaInfoPatchMessage(any(), any(), any())

        doReturn(false).whenever(mockKeycloakUserService).isKeycloakUserId(any())
        doReturn(true).whenever(mockKeycloakUserService).isKeycloakUserId(newValidUploaderUserId)

        dataMetaInfoAlterationManager =
            DataMetaInfoAlterationManager(
                this.mockDataManager,
                this.mockDataMetaInformationManager,
                this.mockDataPointUtils,
                this.mockKeycloakUserService,
                this.mockMessageQueuePublications,
            )
    }

    private fun initializePartialMocks() {
        partiallyMockedDataMetaInformationEntity.uploaderUserId = initialUploaderUserId
    }

    @Test
    fun `test that patch functionality runs as expected on happy path patching metaInfo and storableDataset`() {
        val dataMetaInformationPatch = DataMetaInformationPatch(newValidUploaderUserId)
        val argumentCaptor = argumentCaptor<StorableDataset>()

        assertDoesNotThrow {
            dataMetaInfoAlterationManager.patchDataMetaInformation(
                dataId,
                dataMetaInformationPatch,
                correlationId,
            )
        }
        assertEquals(newValidUploaderUserId, partiallyMockedDataMetaInformationEntity.uploaderUserId)
        verify(mockDataManager).storeDatasetInTemporaryStoreAndSendPatchMessage(any(), argumentCaptor.capture(), any())
        assertEquals(newValidUploaderUserId, argumentCaptor.firstValue.uploaderUserId)
    }

    @Test
    fun `test that patch functionality runs as expected on happy path for assembled datasets`() {
        doReturn(mock<FrameworkSpecification>()).whenever(mockDataPointUtils).getFrameworkSpecificationOrNull(any())
        val dataMetaInformationPatch = DataMetaInformationPatch(newValidUploaderUserId)

        assertDoesNotThrow {
            dataMetaInfoAlterationManager.patchDataMetaInformation(
                dataId,
                dataMetaInformationPatch,
                correlationId,
            )
        }
        assertEquals(newValidUploaderUserId, partiallyMockedDataMetaInformationEntity.uploaderUserId)
        verify(mockDataManager, times(0)).storeDatasetInTemporaryStoreAndSendPatchMessage(any(), any(), any())
    }

    @Test
    fun `ensure that an error is thrown if uploaderUserId is null and metaInfo and storableDataset are not patched`() {
        assertThrows<InvalidInputApiException> {
            dataMetaInfoAlterationManager.patchDataMetaInformation(
                dataId,
                DataMetaInformationPatch(uploaderUserId = ""),
                correlationId,
            )
        }
        assertEquals(initialUploaderUserId, partiallyMockedDataMetaInformationEntity.uploaderUserId)
        assertEquals(initialUploaderUserId, storableDataset.uploaderUserId)
    }

    @Test
    fun `ensure that an error is thrown if uploaderUserId is unknown and metaInfo and storableDataset are not patched`() {
        assertThrows<InvalidInputApiException> {
            dataMetaInfoAlterationManager.patchDataMetaInformation(
                dataId,
                DataMetaInformationPatch(newUploaderUserIdUnknownToKeycloak),
                correlationId,
            )
        }
        assertEquals(initialUploaderUserId, partiallyMockedDataMetaInformationEntity.uploaderUserId)
        assertEquals(initialUploaderUserId, storableDataset.uploaderUserId)
    }
}
