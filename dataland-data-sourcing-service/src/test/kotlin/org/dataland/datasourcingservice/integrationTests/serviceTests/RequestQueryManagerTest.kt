package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.ExtendedStoredRequest
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.dataland.datasourcingservice.utils.ADMIN_COMMENT
import org.dataland.datasourcingservice.utils.ADMIN_COMMENT_SEARCH_STRING
import org.dataland.datasourcingservice.utils.COMPANY_ID_1
import org.dataland.datasourcingservice.utils.COMPANY_ID_2
import org.dataland.datasourcingservice.utils.DATA_TYPE_1
import org.dataland.datasourcingservice.utils.DATA_TYPE_2
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2
import org.dataland.datasourcingservice.utils.REQUEST_STATE_1
import org.dataland.datasourcingservice.utils.REQUEST_STATE_2
import org.dataland.datasourcingservice.utils.TEST_COMPANY_NAME
import org.dataland.datasourcingservice.utils.TEST_COMPANY_SEARCH_STRING
import org.dataland.datasourcingservice.utils.USER_EMAIL
import org.dataland.datasourcingservice.utils.USER_EMAIL_SEARCH_STRING
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
class RequestQueryManagerTest
    @Autowired
    constructor(
        private val requestQueryManager: RequestQueryManager,
        private val requestRepository: RequestRepository,
    ) : BaseIntegrationTest() {
        private val dataBaseCreationUtils = DataBaseCreationUtils(requestRepository = requestRepository)
        private lateinit var requestEntities: List<RequestEntity>

        @MockitoBean
        private lateinit var mockKeycloakUserService: KeycloakUserService

        @MockitoBean
        private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
        private val firstUser = KeycloakUserInfo(USER_EMAIL, "19223180-a213-4294-86aa-de3341139bcd", "John", "Doe")
        private val secondUser = mock<KeycloakUserInfo>()
        private val mockBasicCompanyInfo1 = mock<BasicCompanyInformation>()
        private val mockBasicCompanyInfo2 = mock<BasicCompanyInformation>()
        private val storedCompany1 = mock<StoredCompany>()
        private val storedCompany2 = mock<StoredCompany>()
        private val companyInfo1 = mock<CompanyInformation>()
        private val companyInfo2 = mock<CompanyInformation>()

        @BeforeEach
        fun setupMocks() {
            doReturn(firstUser).whenever(mockKeycloakUserService).getUser(firstUser.userId)
            doReturn(secondUser).whenever(mockKeycloakUserService).getUser(not(eq(firstUser.userId)))
            doReturn(null).whenever(secondUser).email
            doReturn(listOf(firstUser)).whenever(mockKeycloakUserService).searchUsers(USER_EMAIL_SEARCH_STRING)
            doReturn(COMPANY_ID_1).whenever(mockBasicCompanyInfo1).companyId
            doReturn(COMPANY_ID_2).whenever(mockBasicCompanyInfo2).companyId
            doReturn(listOf(mockBasicCompanyInfo1))
                .whenever(mockCompanyDataControllerApi)
                .getCompanies(eq(TEST_COMPANY_SEARCH_STRING), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            doReturn(COMPANY_ID_1).whenever(storedCompany1).companyId
            doReturn(COMPANY_ID_2).whenever(storedCompany2).companyId
            doReturn(companyInfo1).whenever(storedCompany1).companyInformation
            doReturn(companyInfo2).whenever(storedCompany2).companyInformation
            doReturn(TEST_COMPANY_NAME).whenever(companyInfo1).companyName
            doReturn(null).whenever(companyInfo2).companyName
            doReturn(storedCompany1).whenever(mockCompanyDataControllerApi).getCompanyById(COMPANY_ID_1)
            doReturn(storedCompany2).whenever(mockCompanyDataControllerApi).getCompanyById(COMPANY_ID_2)
        }

        /**
         * Store 16 requests covering all combinations of the four filter parameters defined above.
         * Note: i / 2^k % 2 is the position k binary digit of i, with k=0 for the least significant bit.
         */
        fun setupParameterizedTest() {
            requestEntities =
                (0..15).map {
                    dataBaseCreationUtils.storeRequest(
                        companyId = UUID.fromString(if (it / 8 % 2 == 0) COMPANY_ID_1 else COMPANY_ID_2),
                        dataType = if (it / 4 % 2 == 0) DATA_TYPE_1 else DATA_TYPE_2,
                        reportingPeriod = if (it / 2 % 2 == 0) REPORTING_PERIOD_1 else REPORTING_PERIOD_2,
                        state = RequestState.valueOf(if (it % 2 == 0) REQUEST_STATE_1 else REQUEST_STATE_2),
                        userId = if (it % 2 == 0) UUID.fromString(firstUser.userId) else UUID.randomUUID(),
                        adminComment = if (it / 8 % 2 == 0) ADMIN_COMMENT else null,
                    )
                }
        }

        data class RequestSearchTestCase(
            val companyId: String?,
            val dataType: String?,
            val reportingPeriod: String?,
            val requestState: String?,
            val emailAddressSearchString: String?,
            val companySearchString: String?,
            val adminCommentSearchString: String?,
            val indexString: String,
        )

        companion object {
            val ALL_INDICES = (0..15).joinToString(";")

            @JvmStatic
            fun requestSearchTestCases() =
                listOf(
                    RequestSearchTestCase(COMPANY_ID_1, DATA_TYPE_1, REPORTING_PERIOD_1, REQUEST_STATE_1, null, null, null, "0"),
                    RequestSearchTestCase(COMPANY_ID_1, DATA_TYPE_1, REPORTING_PERIOD_1, null, null, null, null, "0;1"),
                    RequestSearchTestCase(null, null, null, REQUEST_STATE_1, null, null, null, "0;2;4;6;8;10;12;14"),
                    RequestSearchTestCase(null, null, null, null, null, null, null, ALL_INDICES),
                    RequestSearchTestCase(null, null, "${REPORTING_PERIOD_1};${REPORTING_PERIOD_2}", null, null, null, null, ALL_INDICES),
                    RequestSearchTestCase(null, null, null, "${REQUEST_STATE_1};${REQUEST_STATE_2}", null, null, null, ALL_INDICES),
                    RequestSearchTestCase(null, null, null, null, USER_EMAIL_SEARCH_STRING, null, null, "0;2;4;6;8;10;12;14"),
                    RequestSearchTestCase(null, null, null, null, null, TEST_COMPANY_SEARCH_STRING, null, "0;1;2;3;4;5;6;7"),
                    RequestSearchTestCase(null, null, null, null, null, null, ADMIN_COMMENT_SEARCH_STRING, "0;1;2;3;4;5;6;7"),
                )
        }

        @ParameterizedTest(name = "{index} => {0}")
        @DisplayName("Request search filter combinations")
        @MethodSource("requestSearchTestCases")
        fun `ensure that searching for requests works for all filter combinations`(testCase: RequestSearchTestCase) {
            setupParameterizedTest()
            val indicesOfExpectedResults = testCase.indexString.split(';').mapNotNull { it.toIntOrNull() }
            val expectedResults =
                indicesOfExpectedResults.map {
                    val entity = requestEntities[it]
                    entity.toExtendedStoredRequest().copy(
                        companyName = if (entity.companyId.toString() == COMPANY_ID_1) TEST_COMPANY_NAME else null,
                        userEmailAddress = if (entity.userId.toString() == firstUser.userId) USER_EMAIL else null,
                    )
                }
            val reportingPeriods = testCase.reportingPeriod?.split(';')?.toSet()
            val requestStates =
                testCase.requestState
                    ?.split(';')
                    ?.map { RequestState.valueOf(it) }
                    ?.toSet()
            val requestSearchFilter =
                RequestSearchFilter<UUID>(
                    companyId = testCase.companyId?.let { UUID.fromString(it) },
                    dataTypes = testCase.dataType?.let { setOf(it) },
                    reportingPeriods = reportingPeriods,
                    userId = null,
                    requestStates = requestStates,
                    requestPriorities = null,
                    emailAddress = testCase.emailAddressSearchString,
                    companySearchString = testCase.companySearchString,
                    adminComment = testCase.adminCommentSearchString,
                )
            val actualResults = requestQueryManager.searchRequests(requestSearchFilter)
            val actualNumberOfResultsAccordingToEndpoint = requestQueryManager.getNumberOfRequests(requestSearchFilter)
            Assertions.assertEquals(expectedResults.size, actualResults.size)
            Assertions.assertEquals(expectedResults.size, actualNumberOfResultsAccordingToEndpoint)
            expectedResults.forEach { expected ->
                val actual = actualResults.find { it.id == expected.id }
                assert(actual != null) { "Expected result $expected not found in actual results." }
                Assertions.assertEquals(expected.userEmailAddress, actual?.userEmailAddress)
                Assertions.assertEquals(expected.adminComment, actual?.adminComment)
                Assertions.assertEquals(expected.companyName, actual?.companyName)
            }
        }

        @Test
        fun `test sorting of requests works as expected`() {
            val timestamp = 1760428203000
            dataBaseCreationUtils.storeRequest(
                companyId = UUID.fromString(COMPANY_ID_2),
                dataType = DATA_TYPE_2,
                reportingPeriod = REPORTING_PERIOD_2,
                state = RequestState.valueOf(REQUEST_STATE_2),
                userId = UUID.randomUUID(),
                adminComment = null,
                creationTimestamp = timestamp,
            )
            dataBaseCreationUtils.storeRequest(
                companyId = UUID.fromString(COMPANY_ID_1),
                dataType = DATA_TYPE_1,
                reportingPeriod = REPORTING_PERIOD_1,
                state = RequestState.valueOf(REQUEST_STATE_1),
                userId = UUID.randomUUID(),
                adminComment = null,
                creationTimestamp = timestamp,
            )
            dataBaseCreationUtils.storeRequest(
                companyId = UUID.fromString(COMPANY_ID_1),
                dataType = DATA_TYPE_2,
                reportingPeriod = REPORTING_PERIOD_2,
                state = RequestState.valueOf(REQUEST_STATE_2),
                userId = UUID.randomUUID(),
                adminComment = null,
                creationTimestamp = timestamp + 600000,
            )
            dataBaseCreationUtils.storeRequest(
                companyId = UUID.fromString(COMPANY_ID_2),
                dataType = DATA_TYPE_1,
                reportingPeriod = REPORTING_PERIOD_1,
                state = RequestState.valueOf(REQUEST_STATE_1),
                userId = UUID.randomUUID(),
                adminComment = null,
                creationTimestamp = timestamp + 600000,
            )

            val filter = RequestSearchFilter<UUID>()
            val results = requestQueryManager.searchRequests(filter)
            val sorted =
                results.sortedWith(
                    compareByDescending<ExtendedStoredRequest> { it.creationTimeStamp }
                        .thenBy { it.companyId }
                        .thenByDescending { it.reportingPeriod }
                        .thenBy { it.state },
                )
            Assertions.assertEquals(sorted, results, "Results are not sorted as expected")
        }
    }
