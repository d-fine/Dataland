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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
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

    private val userId = UUID.randomUUID()

    private val companyIdentifier1 = UUID.randomUUID().toString()
    private val companyIdentifier2 = UUID.randomUUID().toString()
    private val invalidCompanyId = "invalid-company-id"
    private val companyIdentifiers = setOf(companyIdentifier1, companyIdentifier2, invalidCompanyId)

    private val dataType1 = "sfdr"
    private val dataType2 = "eutaxonomy-financials"
    private val invalidDataType = "invalid-data-type"
    private val dataTypes = setOf(dataType1, dataType2, invalidDataType)

    private val reportingPeriod1 = "2024"
    private val reportingPeriod2 = "2025"
    private val invalidReportingPeriod = "2040" // currently, only reporting periods up to 2039 are valid
    private val reportingPeriods = setOf(reportingPeriod1, reportingPeriod2, invalidReportingPeriod)

    private val allRequestedDataDimensions =
        (0..26).map {
            BasicDataDimensions(
                companyId = companyIdentifiers.elementAt(it / 9 % 3),
                dataType = dataTypes.elementAt(it / 3 % 3),
                reportingPeriod = reportingPeriods.elementAt(it % 3),
            )
        }

    private val validDataDimensions =
        allRequestedDataDimensions.filter {
            it.companyId != invalidCompanyId &&
                it.dataType != invalidDataType &&
                it.reportingPeriod != invalidReportingPeriod
        }

    private val dataDimensionsWithExistingRequests =
        listOf(
            BasicDataDimensions(companyIdentifier1, dataType2, reportingPeriod2),
            BasicDataDimensions(companyIdentifier2, dataType1, reportingPeriod1),
            BasicDataDimensions(companyIdentifier2, dataType1, reportingPeriod2),
        )

    private val dataDimensionsWithExistingDatasets =
        listOf(
            BasicDataDimensions(companyIdentifier2, dataType2, reportingPeriod1),
            BasicDataDimensions(companyIdentifier2, dataType2, reportingPeriod2),
        )

    private val dataMetaInformationList =
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

    private val acceptedDataDimensions =
        validDataDimensions - dataDimensionsWithExistingRequests - dataDimensionsWithExistingDatasets

    /**
     * Native SQL query string that is expected to be generated as part of getting the existing
     * data requests among all valid data dimensions (the ones not containing one of the invalid strings).
     */
    private val expectedQueryString =
        buildString {
            append("SELECT * FROM requests request\n                ")
            append("WHERE (request.company_id, request.data_type, request.reporting_period) IN ")
            append("(")
            append(
                validDataDimensions.joinToString(separator = ", ") {
                    "('${it.companyId}', '${it.dataType}', '${it.reportingPeriod}')"
                },
            )
            append(")\n                ")
            append("AND request.user_id = '$userId'")
        }

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

        // Build mocks for DataRequestValidationResult with companyIdValidation, dataTypeValidation, reportingPeriodValidation
        val companyIdValidSet = companyIdentifiers - invalidCompanyId
        val dataTypeValidSet = dataTypes - invalidDataType
        val reportingPeriodValidSet = reportingPeriods - invalidReportingPeriod

        val companyIdValidation =
            companyIdentifiers.associateWith { id -> if (id in companyIdValidSet) UUID.randomUUID() else null }
        val dataTypeValidation = dataTypes.associateWith { dt -> dt in dataTypeValidSet }
        val reportingPeriodValidation = reportingPeriods.associateWith { rp -> rp in reportingPeriodValidSet }

        doReturn(
            DataSourcingValidator.DataRequestValidationResult(
                companyIdValidation = companyIdValidation,
                dataTypeValidation = dataTypeValidation,
                reportingPeriodValidation = reportingPeriodValidation,
            ),
        ).whenever(mockDataSourcingValidator).validateBulkDataRequest(any())

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

        val invalidBulkDataRequest = BulkDataRequest(companyIdentifiers, dataTypes, reportingPeriods)

        assertThrows<InvalidInputApiException> {
            bulkRequestManager.processBulkDataRequest(
                invalidBulkDataRequest,
                userId,
            )
        }
    }

    @Test
    fun `check that data dimensions are split correctly into four categories and that exactly the accepted requests are stored`() {
        val bulkDataRequest = BulkDataRequest(companyIdentifiers, dataTypes, reportingPeriods)

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
