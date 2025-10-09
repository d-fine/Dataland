package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import org.dataland.datasourcingservice.utils.COMPANY_ID_1 as companyIdToFilterBy
import org.dataland.datasourcingservice.utils.COMPANY_ID_2 as otherCompanyId
import org.dataland.datasourcingservice.utils.DATA_TYPE_1 as dataTypeToFilterBy
import org.dataland.datasourcingservice.utils.DATA_TYPE_2 as otherDataType
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1 as reportingPeriodToFilterBy
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2 as otherReportingPeriod
import org.dataland.datasourcingservice.utils.REQUEST_STATE_1 as requestStateToFilterBy
import org.dataland.datasourcingservice.utils.REQUEST_STATE_2 as otherRequestState

@SpringBootTest(classes = [DatalandDataSourcingService::class])
class RequestQueryManagerTest
    @Autowired
    constructor(
        private val requestQueryManager: RequestQueryManager,
        private val requestRepository: RequestRepository,
    ) : BaseIntegrationTest() {
        private val dataBaseCreationUtils = DataBaseCreationUtils(requestRepository = requestRepository)
        private lateinit var requestEntities: MutableList<RequestEntity>

        /**
         * Store 16 requests covering all combinations of the four filter parameters defined above.
         * Note: i / 2^k % 2 is the position k binary digit of i, with k=0 for the least significant bit.
         */
        @BeforeEach
        fun setup() {
            requestEntities = mutableListOf()
            for (i in 0..15) {
                val requestEntity =
                    dataBaseCreationUtils.storeRequest(
                        companyId = if (i / 8 % 2 == 0) UUID.fromString(companyIdToFilterBy) else UUID.fromString(otherCompanyId),
                        dataType = if (i / 4 % 2 == 0) dataTypeToFilterBy else otherDataType,
                        reportingPeriod = if (i / 2 % 2 == 0) reportingPeriodToFilterBy else otherReportingPeriod,
                        state =
                            if (i % 2 == 0) {
                                RequestState.valueOf(requestStateToFilterBy)
                            } else {
                                RequestState.valueOf(
                                    otherRequestState,
                                )
                            },
                    )
                requestEntities.add(requestEntity)
            }
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "$companyIdToFilterBy, $dataTypeToFilterBy, $reportingPeriodToFilterBy, $requestStateToFilterBy, 0",
                "$companyIdToFilterBy, $dataTypeToFilterBy, $reportingPeriodToFilterBy, null, 0;1",
                "null, null, null, $requestStateToFilterBy, 0;2;4;6;8;10;12;14",
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
                    companyId = companyId?.let { UUID.fromString(it) },
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    state = requestState?.let { RequestState.valueOf(it) },
                )
            assertEquals(expectedResults.size, actualResults.size)
            expectedResults.forEach {
                assert(it in actualResults) { "Expected result $it not found in actual results." }
            }
        }
    }
