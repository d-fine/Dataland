package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enhanced.RequestSearchFilter
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.dataland.datasourcingservice.utils.ADMIN_COMMENT
import org.dataland.datasourcingservice.utils.ADMIN_COMMENT_SEARCH_STRING
import org.dataland.datasourcingservice.utils.COMPANY_ID_1
import org.dataland.datasourcingservice.utils.COMPANY_ID_2
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_1
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_2
import org.dataland.datasourcingservice.utils.DATA_TYPE_1
import org.dataland.datasourcingservice.utils.DATA_TYPE_2
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2
import org.dataland.datasourcingservice.utils.REQUEST_STATE_1
import org.dataland.datasourcingservice.utils.REQUEST_STATE_2
import org.dataland.datasourcingservice.utils.TEST_COMPANY_NAME_1
import org.dataland.datasourcingservice.utils.TEST_COMPANY_NAME_2
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
        private val dataSourcingRepository: DataSourcingRepository,
    ) : BaseIntegrationTest() {
        private val dataBaseCreationUtils =
            DataBaseCreationUtils(requestRepository = requestRepository, dataSourcingRepository = dataSourcingRepository)
        private lateinit var requestEntities: List<RequestEntity>

        @MockitoBean
        private lateinit var mockKeycloakUserService: KeycloakUserService

        @MockitoBean
        private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
        private val firstUser = KeycloakUserInfo(USER_EMAIL, "19223180-a213-4294-86aa-de3341139bcd", "John", "Doe")
        private val secondUser = mock<KeycloakUserInfo>()
        private val mockBasicCompanyInfo1 = mock<BasicCompanyInformation>()

        private val defaultValidationResult1 =
            CompanyIdentifierValidationResult(
                identifier = COMPANY_ID_1,
                companyInformation =
                    BasicCompanyInformation(
                        companyId = COMPANY_ID_1,
                        companyName = TEST_COMPANY_NAME_1,
                        headquarters = "HQ1",
                        countryCode = "DE",
                    ),
            )
        private val defaultValidationResult2 =
            CompanyIdentifierValidationResult(
                identifier = COMPANY_ID_2,
                companyInformation =
                    BasicCompanyInformation(
                        companyId = COMPANY_ID_2,
                        companyName = TEST_COMPANY_NAME_2,
                        headquarters = "HQ2",
                        countryCode = "FR",
                    ),
            )

        @BeforeEach
        fun setupMocks() {
            doReturn(firstUser).whenever(mockKeycloakUserService).getUser(firstUser.userId)
            doReturn(secondUser).whenever(mockKeycloakUserService).getUser(not(eq(firstUser.userId)))
            doReturn(null).whenever(secondUser).email
            doReturn(listOf(firstUser)).whenever(mockKeycloakUserService).searchUsers(USER_EMAIL_SEARCH_STRING)
            doReturn(COMPANY_ID_1).whenever(mockBasicCompanyInfo1).companyId
            doReturn(listOf(mockBasicCompanyInfo1))
                .whenever(mockCompanyDataControllerApi)
                .getCompanies(eq(TEST_COMPANY_SEARCH_STRING), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
            doReturn(listOf(defaultValidationResult1, defaultValidationResult2))
                .whenever(mockCompanyDataControllerApi)
                .postCompanyValidation(anyOrNull())
        }

        /**
         * Store 16 requests covering all combinations of the four filter parameters defined above.
         * Note: i / 2^k % 2 is the position k binary digit of i, with k=0 for the least significant bit.
         */
        fun setupParameterizedTest() {
            val dataSourcingEntitiesByDataDimension =
                mutableMapOf<Triple<UUID, String, String>, DataSourcingEntity>()
            requestEntities =
                (0..15).map {
                    val parameterizedRequestData = buildParameterizedRequestData(it)
                    val dataSourcingEntity =
                        if (parameterizedRequestData.requestState == RequestState.Processing) {
                            getOrCreateDataSourcingEntity(parameterizedRequestData, dataSourcingEntitiesByDataDimension)
                        } else {
                            null
                        }
                    dataBaseCreationUtils.storeRequest(
                        companyId = parameterizedRequestData.companyId,
                        dataType = parameterizedRequestData.dataType,
                        reportingPeriod = parameterizedRequestData.reportingPeriod,
                        state = parameterizedRequestData.requestState,
                        userId = parameterizedRequestData.userId,
                        adminComment = parameterizedRequestData.adminComment,
                        dataSourcingEntity = dataSourcingEntity,
                    )
                }
        }

        private data class ParameterizedRequestData(
            val companyId: UUID,
            val reportingPeriod: String,
            val dataType: String,
            val requestState: RequestState,
            val userId: UUID,
            val adminComment: String?,
            val dataSourcingState: DataSourcingState,
        )

        private fun buildParameterizedRequestData(index: Int): ParameterizedRequestData {
            val companyId = UUID.fromString(if (index / 8 % 2 == 0) COMPANY_ID_1 else COMPANY_ID_2)
            val reportingPeriod = if (index / 2 % 2 == 0) REPORTING_PERIOD_1 else REPORTING_PERIOD_2
            val dataType = if (index / 4 % 2 == 0) DATA_TYPE_1 else DATA_TYPE_2
            val requestState = RequestState.valueOf(if (index % 2 == 0) REQUEST_STATE_1 else REQUEST_STATE_2)
            val userId = if (index % 2 == 0) UUID.fromString(firstUser.userId) else UUID.randomUUID()
            val adminComment = if (index / 8 % 2 == 0) ADMIN_COMMENT else null
            val dataSourcingState = DataSourcingState.valueOf(if (index / 4 % 2 == 0) DATA_SOURCING_STATE_1 else DATA_SOURCING_STATE_2)
            return ParameterizedRequestData(
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                dataType = dataType,
                requestState = requestState,
                userId = userId,
                adminComment = adminComment,
                dataSourcingState = dataSourcingState,
            )
        }

        private fun getOrCreateDataSourcingEntity(
            parameterizedRequestData: ParameterizedRequestData,
            dataSourcingEntitiesByDataDimension: MutableMap<Triple<UUID, String, String>, DataSourcingEntity>,
        ): DataSourcingEntity {
            val key =
                Triple(parameterizedRequestData.companyId, parameterizedRequestData.reportingPeriod, parameterizedRequestData.dataType)
            return dataSourcingEntitiesByDataDimension.getOrPut(key) {
                dataBaseCreationUtils.storeDataSourcing(
                    dataSourcingId = UUID.randomUUID(),
                    companyId = parameterizedRequestData.companyId,
                    reportingPeriod = parameterizedRequestData.reportingPeriod,
                    dataType = parameterizedRequestData.dataType,
                    state = parameterizedRequestData.dataSourcingState,
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
            val dataSourcingState: String?,
            val indexString: String,
        )

        companion object {
            val ALL_INDICES = (0..15).joinToString(";")

            @JvmStatic
            fun requestSearchTestCases() =
                listOf(
                    RequestSearchTestCase(COMPANY_ID_1, DATA_TYPE_1, REPORTING_PERIOD_1, REQUEST_STATE_1, null, null, null, null, "0"),
                    RequestSearchTestCase(COMPANY_ID_1, DATA_TYPE_1, REPORTING_PERIOD_1, null, null, null, null, null, "0;1"),
                    RequestSearchTestCase(null, null, null, REQUEST_STATE_1, null, null, null, null, "0;2;4;6;8;10;12;14"),
                    RequestSearchTestCase(null, null, null, null, null, null, null, null, ALL_INDICES),
                    RequestSearchTestCase(
                        null, null, "${REPORTING_PERIOD_1};${REPORTING_PERIOD_2}", null, null, null, null, null, ALL_INDICES,
                    ),
                    RequestSearchTestCase(null, null, null, "${REQUEST_STATE_1};${REQUEST_STATE_2}", null, null, null, null, ALL_INDICES),
                    RequestSearchTestCase(null, null, null, null, USER_EMAIL_SEARCH_STRING, null, null, null, "0;2;4;6;8;10;12;14"),
                    RequestSearchTestCase(null, null, null, null, null, TEST_COMPANY_SEARCH_STRING, null, null, "0;1;2;3;4;5;6;7"),
                    RequestSearchTestCase(null, null, null, null, null, null, ADMIN_COMMENT_SEARCH_STRING, null, "0;1;2;3;4;5;6;7"),
                    RequestSearchTestCase(null, null, null, null, null, null, null, DATA_SOURCING_STATE_1, "1;3;9;11"),
                    RequestSearchTestCase(null, null, null, null, null, null, null, DATA_SOURCING_STATE_2, "5;7;13;15"),
                    RequestSearchTestCase(
                        null, null, null, null, null, null, null, "${DATA_SOURCING_STATE_1};${DATA_SOURCING_STATE_2}",
                        "1;3;5;7;9;11;13;15",
                    ),
                    RequestSearchTestCase(
                        null, null, null, REQUEST_STATE_1, null, null, null, DATA_SOURCING_STATE_1,
                        "0;1;2;3;4;6;8;9;10;11;12;14",
                    ),
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
                    entity.toExtendedStoredRequest(
                        companyName = if (entity.companyId.toString() == COMPANY_ID_1) TEST_COMPANY_NAME_1 else TEST_COMPANY_NAME_2,
                        userEmailAddress = if (entity.userId.toString() == firstUser.userId) USER_EMAIL else null,
                    )
                }
            val reportingPeriods = testCase.reportingPeriod?.split(';')?.toSet()
            val requestStates =
                testCase.requestState
                    ?.split(';')
                    ?.map { RequestState.valueOf(it) }
                    ?.toSet()
            val dataSourcingStates =
                testCase.dataSourcingState
                    ?.split(';')
                    ?.map { DataSourcingState.valueOf(it) }
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
                    dataSourcingStates = dataSourcingStates,
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

        private fun setupFourTestRequests(): List<RequestEntity> {
            val timestamp = 1760428203000
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

            doReturn(listOf(defaultValidationResult2, defaultValidationResult1))
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
        fun `searchRequests by dataSourcingState does not return Withdrawn requests`() {
            val dataSourcingEntity =
                dataBaseCreationUtils.storeDataSourcing(
                    dataSourcingId = UUID.fromString("00000000-0000-0000-0000-000000000030"),
                    companyId = UUID.fromString(COMPANY_ID_1),
                    reportingPeriod = REPORTING_PERIOD_1,
                    dataType = DATA_TYPE_1,
                    state = DataSourcingState.Initialized,
                )

            val processingRequest =
                dataBaseCreationUtils.storeRequest(
                    requestId = UUID.fromString("00000000-0000-0000-0000-000000000031"),
                    companyId = UUID.fromString(COMPANY_ID_1),
                    userId = UUID.fromString(firstUser.userId),
                    dataType = DATA_TYPE_1,
                    reportingPeriod = REPORTING_PERIOD_1,
                    state = RequestState.Processing,
                    dataSourcingEntity = dataSourcingEntity,
                )

            dataBaseCreationUtils.storeRequest(
                requestId = UUID.fromString("00000000-0000-0000-0000-000000000032"),
                companyId = UUID.fromString(COMPANY_ID_2),
                userId = UUID.fromString(firstUser.userId),
                dataType = DATA_TYPE_2,
                reportingPeriod = REPORTING_PERIOD_2,
                state = RequestState.Withdrawn,
                dataSourcingEntity = dataSourcingEntity,
            )

            val filter = RequestSearchFilter<UUID>(dataSourcingStates = setOf(DataSourcingState.Initialized))
            val results = requestQueryManager.searchRequests(filter)

            Assertions.assertEquals(1, results.size)
            Assertions.assertEquals(processingRequest.id.toString(), results.first().id)
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
