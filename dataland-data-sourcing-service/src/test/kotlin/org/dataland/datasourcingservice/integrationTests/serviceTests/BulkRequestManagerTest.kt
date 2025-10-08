package org.dataland.datasourcingservice.integrationTests.serviceTests

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.services.BulkRequestManager
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.services.RequestCreationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class BulkRequestManagerTest {
    private lateinit var bulkRequestManager: BulkRequestManager
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockRequestCreationService = mock<RequestCreationService>()
    private val mockMetaDataControllerApi = mock<MetaDataControllerApi>()
    private val mockEntityManager = mock<EntityManager>()
    private val mockQuery = mock<Query>()

    companion object {
        private val userId = UUID.randomUUID()

        private val companyIdentifier1 = UUID.randomUUID().toString()
        private val companyIdentifier2 = UUID.randomUUID().toString()
        private const val INVALID_COMPANY_ID = "invalid-company-id"
        private val companyIdentifiers = setOf(companyIdentifier1, companyIdentifier2, INVALID_COMPANY_ID)

        private const val DATA_TYPE_1 = "sfdr"
        private const val DATA_TYPE_2 = "eutaxonomy-financials"
        private const val INVALID_DATA_TYPE = "invalid-data-type"
        private val dataTypes = setOf(DATA_TYPE_1, DATA_TYPE_2, INVALID_DATA_TYPE)

        private const val REPORTING_PERIOD_1 = "2024"
        private const val REPORTING_PERIOD_2 = "2025"
        private const val INVALID_REPORTING_PERIOD = "2040" // currently, only reporting periods up to 2039 are valid
        private val reportingPeriods = setOf(REPORTING_PERIOD_1, REPORTING_PERIOD_2, INVALID_REPORTING_PERIOD)

        private lateinit var allRequestedDataDimensions: List<BasicDataDimensions>
        private lateinit var validDataDimensions: List<BasicDataDimensions>
        private lateinit var dataDimensionsWithExistingRequests: List<BasicDataDimensions>
        private lateinit var dataDimensionsWithExistingDatasets: List<BasicDataDimensions>
        private lateinit var dataMetaInformationList: List<DataMetaInformation>
        private lateinit var acceptedDataDimensions: List<BasicDataDimensions>

        /**
         * Set the values of the six lateinit vars of types List<BasicDataDimensions> and List<DataMetaInformation> above.
         * For computing allDataDimensions, the base-3 digits of the running index i are used.
         * Starting with i / 9 % 3, the most significant digit, ensures lexicographic ordering,
         * as in the productive code.
         */
        @BeforeAll
        @JvmStatic
        fun createDataDimensionsLists() {
            val allDataDimensionsMutable = mutableListOf<BasicDataDimensions>()

            for (i in 0..26) {
                allDataDimensionsMutable.add(
                    BasicDataDimensions(
                        companyId = companyIdentifiers.toList()[i / 9 % 3],
                        dataType = dataTypes.toList()[i / 3 % 3],
                        reportingPeriod = reportingPeriods.toList()[i % 3],
                    ),
                )
            }

            allRequestedDataDimensions = allDataDimensionsMutable

            validDataDimensions =
                allRequestedDataDimensions.filter {
                    it.companyId != INVALID_COMPANY_ID &&
                        it.dataType != INVALID_DATA_TYPE &&
                        it.reportingPeriod != INVALID_REPORTING_PERIOD
                }

            dataDimensionsWithExistingRequests =
                listOf(
                    BasicDataDimensions(companyIdentifier1, DATA_TYPE_2, REPORTING_PERIOD_2),
                    BasicDataDimensions(companyIdentifier2, DATA_TYPE_1, REPORTING_PERIOD_1),
                    BasicDataDimensions(companyIdentifier2, DATA_TYPE_1, REPORTING_PERIOD_2),
                )

            dataDimensionsWithExistingDatasets =
                listOf(
                    BasicDataDimensions(companyIdentifier2, DATA_TYPE_2, REPORTING_PERIOD_1),
                    BasicDataDimensions(companyIdentifier2, DATA_TYPE_2, REPORTING_PERIOD_2),
                )

            dataMetaInformationList =
                dataDimensionsWithExistingDatasets.map {
                    DataMetaInformation(
                        dataId = UUID.randomUUID().toString(),
                        companyId = it.companyId,
                        dataType = DataTypeEnum.decode(it.dataType)!!,
                        uploadTime = 0L,
                        reportingPeriod = it.reportingPeriod,
                        currentlyActive = true,
                        qaStatus = QaStatus.Accepted,
                        uploaderUserId = UUID.randomUUID().toString(),
                        ref = null,
                    )
                }

            acceptedDataDimensions = validDataDimensions - dataDimensionsWithExistingRequests - dataDimensionsWithExistingDatasets
        }
    }

    /**
     * Native SQL query string that is expected to be generated as part of getting the existing
     * data requests among all valid data dimensions (the ones not containing one of the invalid strings).
     */
    private val expectedQueryString =
        buildString {
            append("SELECT * FROM requests request\n                ")
            append("WHERE (request.company_id, request.data_type, request.reporting_period) IN ")
            append("(")
            validDataDimensions.forEachIndexed { index, dataDimension ->
                if (index < validDataDimensions.size - 1) {
                    append("('${dataDimension.companyId}', '${dataDimension.dataType}', '${dataDimension.reportingPeriod}'), ")
                } else {
                    append("('${dataDimension.companyId}', '${dataDimension.dataType}', '${dataDimension.reportingPeriod}')")
                }
            }
            append(")\n                ")
            append("AND request.user_id = '$userId'")
        }

    private fun createBulkDataRequest(
        companyIdentifiers: Set<String>,
        dataTypes: Set<String>,
        reportingPeriods: Set<String>,
    ): BulkDataRequest =
        BulkDataRequest(
            companyIdentifiers = companyIdentifiers,
            dataTypes = dataTypes,
            reportingPeriods = reportingPeriods,
        )

    private fun createRequestEntity(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ): RequestEntity =
        RequestEntity(
            id = UUID.randomUUID(),
            companyId = UUID.fromString(companyId),
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            userId = userId,
            creationTimestamp = 0L,
            memberComment = null,
            adminComment = null,
            lastModifiedDate = 0L,
            requestPriority = RequestPriority.High,
            state = RequestState.Open,
            dataSourcingEntity = null,
        )

    @BeforeEach
    fun setup() {
        reset(
            mockDataSourcingValidator,
            mockRequestCreationService,
            mockMetaDataControllerApi,
            mockEntityManager,
        )

        doReturn(
            Pair(validDataDimensions, allRequestedDataDimensions - validDataDimensions),
        ).whenever(mockDataSourcingValidator).validateBulkDataRequest(allRequestedDataDimensions)
        doReturn(mockQuery).whenever(mockEntityManager).createNativeQuery(
            expectedQueryString, RequestEntity::class.java,
        )
        doReturn(
            dataDimensionsWithExistingRequests.map {
                createRequestEntity(it.companyId, it.dataType, it.reportingPeriod)
            },
        ).whenever(mockQuery).resultList
        doReturn(dataMetaInformationList)
            .whenever(mockMetaDataControllerApi)
            .retrieveMetaDataOfActiveDatasets(
                validDataDimensions - dataDimensionsWithExistingRequests,
            )

        bulkRequestManager =
            BulkRequestManager(
                dataSourcingValidator = mockDataSourcingValidator,
                requestCreationService = mockRequestCreationService,
                metaDataController = mockMetaDataControllerApi,
                entityManager = mockEntityManager,
            )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "null, dataType, reportingPeriod",
            "companyId, null, reportingPeriod",
            "companyId, dataType, null",
        ],
        nullValues = ["null"],
    )
    fun `check that a bulk data request with at least one empty set results in an InvalidInputApiException`(
        companyIdentifiersIndicator: String?,
        dataTypesIndicator: String?,
        reportingPeriodsIndicator: String?,
    ) {
        val companyIdentifiers = if (companyIdentifiersIndicator == null) emptySet() else companyIdentifiers
        val dataTypes = if (dataTypesIndicator == null) emptySet() else dataTypes
        val reportingPeriods = if (reportingPeriodsIndicator == null) emptySet() else reportingPeriods

        val invalidBulkDataRequest = createBulkDataRequest(companyIdentifiers, dataTypes, reportingPeriods)

        assertThrows<InvalidInputApiException> {
            bulkRequestManager.processBulkDataRequest(
                invalidBulkDataRequest,
                userId,
            )
        }
    }

    @Test
    fun `check that data dimensions are split correctly into four categories and that exactly the accepted requests are stored`() {
        val bulkDataRequest = createBulkDataRequest(companyIdentifiers, dataTypes, reportingPeriods)

        val bulkDataRequestResponse =
            bulkRequestManager.processBulkDataRequest(
                bulkDataRequest,
                userId,
            )

        assertEquals(
            acceptedDataDimensions,
            bulkDataRequestResponse.acceptedDataRequests,
        )

        assertEquals(
            allRequestedDataDimensions - validDataDimensions,
            bulkDataRequestResponse.invalidDataRequests,
        )

        assertEquals(
            dataDimensionsWithExistingRequests,
            bulkDataRequestResponse.existingDataRequests,
        )

        assertEquals(
            dataDimensionsWithExistingDatasets,
            bulkDataRequestResponse.existingDataSets,
        )

        acceptedDataDimensions.forEach {
            verify(mockRequestCreationService, times(1)).storeRequest(userId, it)
        }

        (allRequestedDataDimensions - acceptedDataDimensions).forEach {
            verify(mockRequestCreationService, times(0)).storeRequest(userId, it)
        }
    }
}
