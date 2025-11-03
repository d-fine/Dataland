package org.dataland.datalandaccountingservice.repositories

import org.dataland.datalandaccountingservice.DatalandAccountingService
import org.dataland.datalandaccountingservice.entities.BilledRequestEntity
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest(
    classes = [DatalandAccountingService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
class BilledRequestRepositoryIntegrationTest
    @Autowired
    constructor(
        private val billedRequestRepository: BilledRequestRepository,
    ) : BaseIntegrationTest() {
        @ParameterizedTest
        @CsvSource(
            "1,1.0",
            "2,0.5",
            "3,0.4",
            "4,0.3",
            "5,0.2",
            "9,0.2",
            "10,0.1",
            "12,0.1",
        )
        fun `getTotalCreditDebtFromBilledRequests returns correct sum for multiple members requesting the same data`(
            groupSize: Int,
            expectedDebt: BigDecimal,
        ) {
            val requestedCompanyId = UUID.randomUUID()
            val dataSourcingId = UUID.randomUUID()
            var billedCompanyId = UUID.randomUUID()
            repeat(groupSize) {
                billedCompanyId = UUID.randomUUID()
                billedRequestRepository.save(
                    BilledRequestEntity(
                        billedCompanyId = billedCompanyId,
                        dataSourcingId = dataSourcingId,
                        requestedCompanyId = requestedCompanyId,
                        requestedReportingPeriod = "2024",
                        requestedFramework = "sfdr",
                    ),
                )
            }
            val result = billedRequestRepository.getTotalCreditDebtFromBilledRequests(billedCompanyId)
            assertEquals(expectedDebt, result)
        }

        @Test
        fun `getTotalCreditDebtFromBilledRequests returns correct sum for two requests per company`() {
            val requestedCompanyId = UUID.randomUUID()
            val dataSourcingId1 = UUID.randomUUID()
            val dataSourcingId2 = UUID.randomUUID()
            val billedCompanyId = UUID.randomUUID()
            billedRequestRepository.save(
                BilledRequestEntity(
                    billedCompanyId = billedCompanyId,
                    dataSourcingId = dataSourcingId1,
                    requestedCompanyId = requestedCompanyId,
                    requestedReportingPeriod = "2024",
                    requestedFramework = "sfdr",
                ),
            )
            billedRequestRepository.save(
                BilledRequestEntity(
                    billedCompanyId = billedCompanyId,
                    dataSourcingId = dataSourcingId2,
                    requestedCompanyId = requestedCompanyId,
                    requestedReportingPeriod = "2020",
                    requestedFramework = "sfdr",
                ),
            )
            val result = billedRequestRepository.getTotalCreditDebtFromBilledRequests(billedCompanyId)
            assertEquals(2.0, result.toDouble())
        }

        @Test
        fun `getTotalCreditDebtFromBilledRequests returns 0 for no billed requests`() {
            val companyId = UUID.randomUUID()
            val result = billedRequestRepository.getTotalCreditDebtFromBilledRequests(companyId)
            assertEquals(0.0, result.toDouble())
        }
    }
