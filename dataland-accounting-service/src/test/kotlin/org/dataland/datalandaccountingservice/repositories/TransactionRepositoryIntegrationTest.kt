package org.dataland.datalandaccountingservice.repositories

import org.dataland.datalandaccountingservice.DatalandAccountingService
import org.dataland.datalandaccountingservice.entities.TransactionEntity
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
class TransactionRepositoryIntegrationTest
    @Autowired
    constructor(
        private val transactionRepository: TransactionRepository,
    ) : BaseIntegrationTest() {
        @ParameterizedTest
        @CsvSource(
            "1,10.0000",
            "2,20.0000",
            "3,33.0000",
        )
        fun `getTotalBalanceFromTransactions returns correct sum for company`(
            transactionCount: Int,
            expectedSum: BigDecimal,
        ) {
            val companyId = UUID.randomUUID()
            val triggeringUser = UUID.randomUUID()
            val timestamp = System.currentTimeMillis()
            val reasonForChange = "TestReason"

            repeat(transactionCount) { idx ->
                val value =
                    when (transactionCount) {
                        1, 2 -> BigDecimal("10.0000")
                        3 -> BigDecimal((idx + 10).toString())
                        else -> BigDecimal("0.0000")
                    }
                transactionRepository.save(
                    TransactionEntity(
                        valueOfChange = value,
                        companyId = companyId,
                        triggeringUser = triggeringUser,
                        reasonForChange = reasonForChange,
                        timestamp = timestamp + idx, // make timestamps unique
                    ),
                )
            }

            val result = transactionRepository.getTotalBalanceFromTransactions(companyId)
            assertEquals(expectedSum, result)
        }

        @Test
        fun `getTotalBalanceFromTransactions returns 0 for no transactions`() {
            val companyId = UUID.randomUUID()
            val result = transactionRepository.getTotalBalanceFromTransactions(companyId)
            assertEquals(BigDecimal.ZERO, result)
        }

        @Test
        fun `getTotalBalanceFromTransactions only sums for specified company`() {
            val targetCompany = UUID.randomUUID()
            val otherCompany = UUID.randomUUID()
            val triggeringUser = UUID.randomUUID()
            val timestamp = System.currentTimeMillis()
            val reasonForChange = "TestReason"

            transactionRepository.save(
                TransactionEntity(
                    valueOfChange = BigDecimal("5.0000"),
                    companyId = targetCompany,
                    triggeringUser = triggeringUser,
                    reasonForChange = reasonForChange,
                    timestamp = timestamp,
                ),
            )
            transactionRepository.save(
                TransactionEntity(
                    valueOfChange = BigDecimal("100.0000"),
                    companyId = otherCompany,
                    triggeringUser = triggeringUser,
                    reasonForChange = reasonForChange,
                    timestamp = timestamp + 1,
                ),
            )

            val result = transactionRepository.getTotalBalanceFromTransactions(targetCompany)
            assertEquals(BigDecimal("5.0000"), result)
        }
    }
