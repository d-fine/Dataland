package org.dataland.datalandaccountingservice.repositories

import org.dataland.datalandaccountingservice.DatalandAccountingService
import org.dataland.datalandaccountingservice.entities.BilledRequestEntity
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
        @Test
        fun `getTotalCreditDebtFromBilledRequests returns correct sum for single member`() {
            val billedCompanyId = UUID.randomUUID()
            val requestedCompanyId = UUID.randomUUID()
            val dataSourcingId = UUID.randomUUID()
            val billedRequest =
                BilledRequestEntity(
                    billedCompanyId = billedCompanyId,
                    dataSourcingId = dataSourcingId,
                    requestedCompanyId = requestedCompanyId,
                    requestedReportingPeriod = "2024",
                    requestedFramework = "sfdr",
                )
            billedRequestRepository.save(billedRequest)

            val result = billedRequestRepository.getTotalCreditDebtFromBilledRequests(billedCompanyId)
            assertEquals(BigDecimal("1.0"), result)
        }

        @Test
        fun `getTotalCreditDebtFromBilledRequests returns correct sum for three members`() {
            val requestedCompanyId = UUID.randomUUID()
            val dataSourcingId = UUID.randomUUID()
            var billedCompanyId = UUID.randomUUID()
            repeat(3) { _ ->
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
            assertEquals(BigDecimal("0.4"), result)
        }
    }
