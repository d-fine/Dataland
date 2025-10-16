package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.dataland.datasourcingservice.utils.COMPANY_ID_1
import org.dataland.datasourcingservice.utils.COMPANY_ID_2
import org.dataland.datasourcingservice.utils.DATA_TYPE_1
import org.dataland.datasourcingservice.utils.DATA_TYPE_2
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2
import org.dataland.datasourcingservice.utils.REQUEST_STATE_1
import org.dataland.datasourcingservice.utils.REQUEST_STATE_2
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(
    classes = [DatalandDataSourcingService::class],
    properties = ["spring.profiles.active=norabbitmq"],
)
class RequestQueryManagerTest
    @Autowired
    constructor(
        private val requestQueryManager: RequestQueryManager,
        private val requestRepository: RequestRepository,
    ) : BaseIntegrationTest() {
        private val dataBaseCreationUtils = DataBaseCreationUtils(requestRepository = requestRepository)
        private lateinit var requestEntities: List<RequestEntity>

        /**
         * Store 16 requests covering all combinations of the four filter parameters defined above.
         * Note: i / 2^k % 2 is the position k binary digit of i, with k=0 for the least significant bit.
         */
        @BeforeEach
        fun setup() {
            requestEntities =
                (0..15).map {
                    dataBaseCreationUtils.storeRequest(
                        companyId = UUID.fromString(if (it / 8 % 2 == 0) COMPANY_ID_1 else COMPANY_ID_2),
                        dataType = if (it / 4 % 2 == 0) DATA_TYPE_1 else DATA_TYPE_2,
                        reportingPeriod = if (it / 2 % 2 == 0) REPORTING_PERIOD_1 else REPORTING_PERIOD_2,
                        state = RequestState.valueOf(if (it % 2 == 0) REQUEST_STATE_1 else REQUEST_STATE_2),
                    )
                }
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, ${REQUEST_STATE_1}, 0",
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, null, 0;1",
                "null, null, null, ${REQUEST_STATE_1}, 0;2;4;6;8;10;12;14",
                "null, null, null, null, 0;1;2;3;4;5;6;7;8;9;10;11;12;13;14;15",
            ],
            nullValues = ["null"],
        )
        fun `ensure that searching for requests works as intended`(
            companyId: String?,
            dataType: String?,
            reportingPeriod: String?,
            requestState: String?,
            indexString: String,
        ) {
            val indicesOfExpectedResults = indexString.split(';').map { it.toInt() }
            val expectedResults =
                indicesOfExpectedResults.map { requestEntities[it].toStoredDataRequest() }
            val actualResults =
                requestQueryManager.searchRequests(
                    RequestSearchFilter<UUID>(
                        companyId = companyId?.let { UUID.fromString(it) },
                        dataTypes = dataType?.let { setOf(it) },
                        reportingPeriods = reportingPeriod?.let { setOf(it) },
                        requestStates = requestState?.let { setOf(RequestState.valueOf(it)) },
                    ),
                )
            val actualNumberOfResultsAccordingToEndpoint =
                requestQueryManager.getNumberOfRequests(
                    RequestSearchFilter<UUID>(
                        companyId = companyId?.let { UUID.fromString(it) },
                        dataTypes = dataType?.let { setOf(it) },
                        reportingPeriods = reportingPeriod?.let { setOf(it) },
                        userId = null,
                        requestStates = requestState?.let { setOf(RequestState.valueOf(it)) },
                        requestPriorities = null,
                    ),
                )
            Assertions.assertEquals(expectedResults.size, actualResults.size)
            Assertions.assertEquals(expectedResults.size, actualNumberOfResultsAccordingToEndpoint)
            expectedResults.forEach {
                assert(it in actualResults) { "Expected result $it not found in actual results." }
            }
        }
    }
