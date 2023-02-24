package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.enums.data.QAStatus
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.utils.AuthenticationMock
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.notNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
internal class DataControllerTest(
    @Autowired @Spy
    var objectMapper: ObjectMapper,
) {
    val testDataProvider = TestDataProvider(objectMapper)

    val testDataType = DataType.valueOf("sme")
    val storedCompanyEntity = testDataProvider.getEmptyStoredCompanyEntity()
    val someSmeData = testDataProvider.getEmptySmeDataset()
    val someSmeDataAsString = objectMapper.writeValueAsString(someSmeData)!!

    val testUserId = "testuser"
    val otherUserId = "otheruser"
    val testUserPendingDataId = "testuser_pending"
    val otherUserPendingDataId = "otheruser_pending"
    val otherUserAcceptedDataId = "otheruser_accepted"
    val testUserPendingDataMetaInformationEntity =
        buildDataMetaInformationEntity(testUserPendingDataId, testUserId, QAStatus.Pending)
    val otherUserPendingDataMetaInformationEntity =
        buildDataMetaInformationEntity(otherUserPendingDataId, otherUserId, QAStatus.Pending)
    val otherUserAcceptedDataMetaInformationEntity =
        buildDataMetaInformationEntity(otherUserAcceptedDataId, otherUserId, QAStatus.Accepted)

    lateinit var mockSecurityContext: SecurityContext
    lateinit var mockDataManager: DataManager
    lateinit var mockDataMetaInformationManager: DataMetaInformationManager
    lateinit var dataController: SmeDataController

    @BeforeEach
    fun resetMocks() {
        mockSecurityContext = mock(SecurityContext::class.java)
        mockDataManager = mock(DataManager::class.java)
        mockDataMetaInformationManager = mock(DataMetaInformationManager::class.java)
        dataController = SmeDataController(mockDataManager, mockDataMetaInformationManager, objectMapper)
    }

    @Test
    fun `test that the correct datasets are filtered`() {
        mockDataMetaInformationManager()
        mockDataManager()
        testGetCompanyAssociatedDataEndpoint()
        testGetAllCompanyDataEndpoint()
    }

    private fun mockDataMetaInformationManager() {
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(testUserPendingDataId)).thenReturn(
            testUserPendingDataMetaInformationEntity,
        )
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(otherUserPendingDataId)).thenReturn(
            otherUserPendingDataMetaInformationEntity,
        )
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(otherUserAcceptedDataId)).thenReturn(
            otherUserAcceptedDataMetaInformationEntity,
        )
        `when`(mockDataMetaInformationManager.searchDataMetaInfo(anyString(), any())).thenReturn(
            listOf(
                testUserPendingDataMetaInformationEntity,
                otherUserPendingDataMetaInformationEntity,
                otherUserAcceptedDataMetaInformationEntity,
            ),
        )
    }

    private fun mockDataManager() {
        `when`(mockDataManager.getDataSet(anyString(), notNull() ?: testDataType, anyString())).thenReturn(
            StorableDataSet(
                "",
                testDataType,
                "",
                0,
                someSmeDataAsString,
            ),
        )
    }

    private fun buildDataMetaInformationEntity(
        dataId: String,
        uploaderId: String,
        qaStatus: QAStatus,
    ): DataMetaInformationEntity {
        return DataMetaInformationEntity(
            dataId,
            testDataType.toString(),
            uploaderId,
            0,
            storedCompanyEntity,
            qaStatus,
        )
    }

    private fun testGetCompanyAssociatedDataEndpoint() {
        mockJwtAuthentication(DatalandRealmRole.ROLE_UPLOADER)
        assertExpectedDatasetForDataId(testUserPendingDataId)
        assertThrows<InvalidInputApiException> {
            dataController.getCompanyAssociatedData(otherUserPendingDataId)
        }
        assertExpectedDatasetForDataId(otherUserAcceptedDataId)

        mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertExpectedDatasetForDataId(testUserPendingDataId)
        assertExpectedDatasetForDataId(otherUserPendingDataId)
        assertExpectedDatasetForDataId(otherUserAcceptedDataId)
    }

    private fun testGetAllCompanyDataEndpoint() {
        mockJwtAuthentication(DatalandRealmRole.ROLE_UPLOADER)
        assertDatasetsContainExactly(listOf(testUserPendingDataId, otherUserAcceptedDataId))

        mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertDatasetsContainExactly(listOf(testUserPendingDataId, otherUserPendingDataId, otherUserAcceptedDataId))
    }

    private fun mockJwtAuthentication(role: DatalandRealmRole) {
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication("", testUserId, setOf(role))
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun assertExpectedDatasetForDataId(dataId: String) {
        Assertions.assertEquals(
            dataController.getCompanyAssociatedData(dataId).body!!.data,
            someSmeData,
        )
    }

    private fun assertDatasetsContainExactly(dataIds: List<String>) {
        val datasetsWithMetaInfo = dataController.getAllCompanyData("").body!!
        Assertions.assertEquals(datasetsWithMetaInfo.size, dataIds.size)
        dataIds.forEach { dataId ->
            assert(datasetsWithMetaInfo.any { it.metaInfo.dataId == dataId })
        }
    }
}
