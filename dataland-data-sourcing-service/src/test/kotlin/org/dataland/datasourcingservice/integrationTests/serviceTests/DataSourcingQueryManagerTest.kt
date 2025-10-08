package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.services.DataSourcingQueryManager
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
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_1 as dataSourcingStateToFilterBy
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_2 as otherDataSourcingState
import org.dataland.datasourcingservice.utils.DATA_TYPE_1 as dataTypeToFilterBy
import org.dataland.datasourcingservice.utils.DATA_TYPE_2 as otherDataType
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1 as reportingPeriodToFilterBy
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2 as otherReportingPeriod

@SpringBootTest(classes = [DatalandDataSourcingService::class])
class DataSourcingQueryManagerTest
    @Autowired
    constructor(
        private val dataSourcingQueryManager: DataSourcingQueryManager,
        private val dataSourcingRepository: DataSourcingRepository,
    ) : BaseIntegrationTest() {
        private val dataBaseCreationUtils = DataBaseCreationUtils(dataSourcingRepository = dataSourcingRepository)
        private lateinit var dataSourcingEntities: MutableList<DataSourcingEntity>

        /**
         * Store 8 data sourcings covering all combinations of the three filter parameters other than state.
         * Note: i / 2^k % 2 is the position k binary digit of i, with k=0 for the least significant bit.
         * 2/3 of the data sourcings have state Initialized, the rest have state DocumentSourcing.
         */
        @BeforeEach
        fun setup() {
            dataSourcingEntities = mutableListOf()
            for (i in 0..7) {
                val dataSourcingEntity =
                    dataBaseCreationUtils.storeDataSourcing(
                        companyId = if (i % 2 == 0) UUID.fromString(companyIdToFilterBy) else UUID.fromString(otherCompanyId),
                        dataType = if (i / 2 % 2 == 0) dataTypeToFilterBy else otherDataType,
                        reportingPeriod = if (i / 4 % 2 == 0) reportingPeriodToFilterBy else otherReportingPeriod,
                        state =
                            if (i % 3 == 0) {
                                DataSourcingState.valueOf(dataSourcingStateToFilterBy)
                            } else {
                                DataSourcingState.valueOf(
                                    otherDataSourcingState,
                                )
                            },
                    )
                dataSourcingEntities.add(dataSourcingEntity)
            }
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "$companyIdToFilterBy, $dataTypeToFilterBy, $reportingPeriodToFilterBy, $dataSourcingStateToFilterBy",
                "$companyIdToFilterBy, $dataTypeToFilterBy, $reportingPeriodToFilterBy, null",
                "null, null, null, $dataSourcingStateToFilterBy",
                "null, null, null, null",
            ],
            nullValues = ["null"],
        )
        fun `ensure that searching for data sourcings works as intended`(
            companyId: String?,
            dataType: String?,
            reportingPeriod: String?,
            dataSourcingState: String?,
        ) {
            val expectedResults =
                dataSourcingEntities
                    .filter {
                        (companyId == null || it.companyId == UUID.fromString(companyId)) &&
                            (dataType == null || it.dataType == dataType) &&
                            (reportingPeriod == null || it.reportingPeriod == reportingPeriod) &&
                            (dataSourcingState == null || it.state == DataSourcingState.valueOf(dataSourcingState))
                    }.map { it.toStoredDataSourcing() }
            val actualResults =
                dataSourcingQueryManager.searchDataSourcings(
                    companyId = companyId?.let { UUID.fromString(it) },
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    state = dataSourcingState?.let { DataSourcingState.valueOf(it) },
                )
            assertEquals(expectedResults.size, actualResults.size)
            expectedResults.forEach {
                assert(it in actualResults) { "Expected result $it not found in actual results" }
            }
        }
    }
