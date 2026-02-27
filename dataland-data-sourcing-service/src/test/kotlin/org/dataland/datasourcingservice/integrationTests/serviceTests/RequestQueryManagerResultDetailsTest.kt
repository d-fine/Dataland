package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enhanced.RequestSearchFilter
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.dataland.datasourcingservice.utils.COMPANY_ID_1
import org.dataland.datasourcingservice.utils.COMPANY_ID_2
import org.dataland.datasourcingservice.utils.DATA_TYPE_1
import org.dataland.datasourcingservice.utils.DATA_TYPE_2
import org.dataland.datasourcingservice.utils.DEFAULT_VALIDATION_RESULT_1
import org.dataland.datasourcingservice.utils.DEFAULT_VALIDATION_RESULT_2
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2
import org.dataland.datasourcingservice.utils.REQUEST_STATE_1
import org.dataland.datasourcingservice.utils.REQUEST_STATE_2
import org.dataland.datasourcingservice.utils.TEST_COMPANY_NAME_1
import org.dataland.datasourcingservice.utils.TEST_COMPANY_NAME_2
import org.dataland.datasourcingservice.utils.USER_EMAIL
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.not
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest(
    classes = [DatalandDataSourcingService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
@Transactional
class RequestQueryManagerResultDetailsTest
    @Autowired
    constructor(
        private val requestQueryManager: RequestQueryManager,
        private val requestRepository: RequestRepository,
        private val dataSourcingRepository: DataSourcingRepository,
    ) : BaseIntegrationTest() {
        private val dataBaseCreationUtils =
            DataBaseCreationUtils(requestRepository = requestRepository, dataSourcingRepository = dataSourcingRepository)

        @MockitoBean
        private lateinit var mockKeycloakUserService: KeycloakUserService

        @MockitoBean
        private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
        private val firstUser = KeycloakUserInfo(USER_EMAIL, "19223180-a213-4294-86aa-de3341139bcd", "John", "Doe")
        private val secondUser = mock<KeycloakUserInfo>()

        @BeforeEach
        fun setupMocks() {
            doReturn(firstUser).whenever(mockKeycloakUserService).getUser(firstUser.userId)
            doReturn(secondUser).whenever(mockKeycloakUserService).getUser(not(eq(firstUser.userId)))
            doReturn(null).whenever(secondUser).email
            doReturn(listOf(DEFAULT_VALIDATION_RESULT_1, DEFAULT_VALIDATION_RESULT_2))
                .whenever(mockCompanyDataControllerApi)
                .postCompanyValidation(anyOrNull())
        }

        private fun setupFourTestRequests(): List<RequestEntity> {
            val timestamp = 1760428203000L
            return listOf(
                dataBaseCreationUtils.storeRequest(
                    requestId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    companyId = UUID.fromString(COMPANY_ID_2),
                    userId = UUID.fromString(firstUser.userId),
                    dataType = DATA_TYPE_2,
                    reportingPeriod = REPORTING_PERIOD_2,
                    state = RequestState.valueOf(REQUEST_STATE_2),
                    creationTimestamp = timestamp,
                ),
                dataBaseCreationUtils.storeRequest(
                    requestId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    companyId = UUID.fromString(COMPANY_ID_1),
                    userId = UUID.fromString(firstUser.userId),
                    dataType = DATA_TYPE_1,
                    reportingPeriod = REPORTING_PERIOD_1,
                    state = RequestState.valueOf(REQUEST_STATE_1),
                    creationTimestamp = timestamp,
                ),
                dataBaseCreationUtils.storeRequest(
                    requestId = UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    companyId = UUID.fromString(COMPANY_ID_1),
                    dataType = DATA_TYPE_2,
                    reportingPeriod = REPORTING_PERIOD_2,
                    state = RequestState.valueOf(REQUEST_STATE_2),
                    creationTimestamp = timestamp + 600000,
                ),
                dataBaseCreationUtils.storeRequest(
                    requestId = UUID.fromString("00000000-0000-0000-0000-000000000004"),
                    companyId = UUID.fromString(COMPANY_ID_2),
                    dataType = DATA_TYPE_1,
                    reportingPeriod = REPORTING_PERIOD_1,
                    state = RequestState.valueOf(REQUEST_STATE_1),
                    creationTimestamp = timestamp + 600000,
                ),
            )
        }

        @Test
        fun `test sorting of requests works as expected`() {
            setupFourTestRequests()
            val filter = RequestSearchFilter<UUID>()
            val results = requestQueryManager.searchRequests(filter)
            val expectedOrder =
                listOf(
                    "00000000-0000-0000-0000-000000000003",
                    "00000000-0000-0000-0000-000000000004",
                    "00000000-0000-0000-0000-000000000002",
                    "00000000-0000-0000-0000-000000000001",
                )
            val actualOrder = results.map { it.id }
            Assertions.assertEquals(expectedOrder, actualOrder, "Results are not in the expected order")
        }

        @Test
        fun `getRequestsByUser returns correct company names and user emails`() {
            val requests = setupFourTestRequests()
            val userId = UUID.fromString(firstUser.userId)

            doReturn(listOf(DEFAULT_VALIDATION_RESULT_2, DEFAULT_VALIDATION_RESULT_1))
                .whenever(mockCompanyDataControllerApi)
                .postCompanyValidation(listOf(COMPANY_ID_2, COMPANY_ID_1))
            doReturn(firstUser).whenever(mockKeycloakUserService).getUser(userId.toString())

            val results = requestQueryManager.getRequestsByUser(userId)
            Assertions.assertEquals(2, results.size)
            results.forEach { result ->
                val request = requests.find { it.id.toString() == result.id }
                val expectedCompanyName =
                    if (request?.companyId.toString() == COMPANY_ID_1) TEST_COMPANY_NAME_1 else TEST_COMPANY_NAME_2
                Assertions.assertEquals(expectedCompanyName, result.companyName)
                Assertions.assertEquals(USER_EMAIL, result.userEmailAddress)
            }
        }

        @Test
        fun `searchRequests returns correct document collector and data extractor names`() {
            val dataSourcingWithBoth =
                dataBaseCreationUtils.storeDataSourcing(
                    dataSourcingId = UUID.fromString("00000000-0000-0000-0000-000000000010"),
                    companyId = UUID.fromString(COMPANY_ID_1),
                    reportingPeriod = REPORTING_PERIOD_1,
                    dataType = DATA_TYPE_1,
                    documentCollector = UUID.fromString(COMPANY_ID_1),
                    dataExtractor = UUID.fromString(COMPANY_ID_2),
                )

            val dataSourcingWithNone =
                dataBaseCreationUtils.storeDataSourcing(
                    dataSourcingId = UUID.fromString("00000000-0000-0000-0000-000000000011"),
                    companyId = UUID.fromString(COMPANY_ID_2),
                    reportingPeriod = REPORTING_PERIOD_2,
                    dataType = DATA_TYPE_2,
                    documentCollector = null,
                    dataExtractor = null,
                )

            dataBaseCreationUtils.storeRequest(
                requestId = UUID.fromString("00000000-0000-0000-0000-000000000020"),
                companyId = UUID.fromString(COMPANY_ID_1),
                userId = UUID.fromString(firstUser.userId),
                dataType = DATA_TYPE_1,
                reportingPeriod = REPORTING_PERIOD_1,
                state = RequestState.valueOf(REQUEST_STATE_2),
                dataSourcingEntity = dataSourcingWithBoth,
            )

            dataBaseCreationUtils.storeRequest(
                requestId = UUID.fromString("00000000-0000-0000-0000-000000000021"),
                companyId = UUID.fromString(COMPANY_ID_2),
                userId = UUID.fromString(firstUser.userId),
                dataType = DATA_TYPE_2,
                reportingPeriod = REPORTING_PERIOD_2,
                state = RequestState.valueOf(REQUEST_STATE_2),
                dataSourcingEntity = dataSourcingWithNone,
            )

            val results = requestQueryManager.searchRequests(RequestSearchFilter())

            val requestWithBoth = results.find { it.id == "00000000-0000-0000-0000-000000000020" }
            assert(requestWithBoth != null) { "Expected request with id 00000000-0000-0000-0000-000000000020 not found." }
            Assertions.assertEquals(TEST_COMPANY_NAME_1, requestWithBoth?.dataSourcingDetails?.documentCollectorName)
            Assertions.assertEquals(TEST_COMPANY_NAME_2, requestWithBoth?.dataSourcingDetails?.dataExtractorName)

            val requestWithNone = results.find { it.id == "00000000-0000-0000-0000-000000000021" }
            assert(requestWithNone != null) { "Expected request with id 00000000-0000-0000-0000-000000000021 not found." }
            Assertions.assertEquals(null, requestWithNone?.dataSourcingDetails?.documentCollectorName)
            Assertions.assertEquals(null, requestWithNone?.dataSourcingDetails?.dataExtractorName)
        }
    }
