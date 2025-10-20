package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.services.DataSourcingQueryManager
import org.dataland.datasourcingservice.utils.COMPANY_ID_1
import org.dataland.datasourcingservice.utils.COMPANY_ID_2
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_1
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_2
import org.dataland.datasourcingservice.utils.DATA_TYPE_1
import org.dataland.datasourcingservice.utils.DATA_TYPE_2
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(
    classes = [DatalandDataSourcingService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
class DataSourcingQueryManagerTest
    @Autowired
    constructor(
        private val dataSourcingQueryManager: DataSourcingQueryManager,
        private val dataSourcingRepository: DataSourcingRepository,
    ) : BaseIntegrationTest() {
        private val dataBaseCreationUtils = DataBaseCreationUtils(dataSourcingRepository = dataSourcingRepository)
        private lateinit var dataSourcingEntities: List<DataSourcingEntity>

        /**
         * Store 8 data sourcings covering all combinations of the three filter parameters other than state.
         * Note: i / 2^k % 2 is the position k binary digit of i, with k=0 for the least significant bit.
         * Roughly one third of the data sourcings have state Initialized, the rest have state DocumentSourcing.
         */
        @BeforeEach
        fun setup() {
            dataSourcingEntities =
                (0..7).map {
                    dataBaseCreationUtils.storeDataSourcing(
                        companyId =
                            if (it / 4 % 2 == 0) {
                                UUID.fromString(COMPANY_ID_1)
                            } else {
                                UUID.fromString(
                                    COMPANY_ID_2,
                                )
                            },
                        dataType = if (it / 2 % 2 == 0) DATA_TYPE_1 else DATA_TYPE_2,
                        reportingPeriod = if (it % 2 == 0) REPORTING_PERIOD_1 else REPORTING_PERIOD_2,
                        state = DataSourcingState.valueOf(if (it % 3 == 0) DATA_SOURCING_STATE_1 else DATA_SOURCING_STATE_2),
                    )
                }
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, ${DATA_SOURCING_STATE_1}, 0",
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, null, 0",
                "null, null, null, ${DATA_SOURCING_STATE_1}, 0;3;6",
                "null, null, null, null, 0;1;2;3;4;5;6;7",
            ],
            nullValues = ["null"],
        )
        fun `ensure that searching for data sourcings works as intended`(
            companyId: String?,
            dataType: String?,
            reportingPeriod: String?,
            dataSourcingState: String?,
            indexString: String,
        ) {
            val indicesOfExpectedResults = indexString.split(';').map { it.toInt() }
            val expectedResults =
                indicesOfExpectedResults
                    .map { dataSourcingEntities[it].toStoredDataSourcing() }
            val actualResults =
                dataSourcingQueryManager.searchDataSourcings(
                    companyId = companyId?.let { UUID.fromString(it) },
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    state = dataSourcingState?.let { DataSourcingState.valueOf(it) },
                )
            Assertions.assertEquals(expectedResults.size, actualResults.size)
            expectedResults.forEach {
                assert(it in actualResults) { "Expected result $it not found in actual results" }
            }
        }
    }
