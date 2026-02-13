package org.dataland.datalandaccountingservice.services

import org.dataland.datalandaccountingservice.entities.TransactionEntity
import org.dataland.datalandaccountingservice.model.TransactionDto
import org.dataland.datalandaccountingservice.repositories.BilledRequestRepository
import org.dataland.datalandaccountingservice.repositories.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class CreditsManagerTest {
    private val mockTransactionRepository = mock<TransactionRepository>()
    private val mockBilledRequestRepository = mock<BilledRequestRepository>()

    private lateinit var creditsManager: CreditsManager

    private val validCompanyId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private val sampleValueOfChange = BigDecimal("50.0")
    private val sampleChangeReason = "Test transaction"
    private val sampleTimestamp = System.currentTimeMillis()

    private val validTransactionDto =
        TransactionDto<UUID>(
            valueOfChange = sampleValueOfChange,
            companyId = validCompanyId,
            triggeringUser = userId,
            reasonForChange = sampleChangeReason,
            timestamp = sampleTimestamp,
        )

    private val transactionDtoString =
        TransactionDto<String>(
            valueOfChange = sampleValueOfChange,
            companyId = validCompanyId.toString(),
            triggeringUser = userId.toString(),
            reasonForChange = sampleChangeReason,
            timestamp = sampleTimestamp,
        )

    @BeforeEach
    fun setup() {
        reset(
            mockTransactionRepository,
            mockBilledRequestRepository,
        )

        creditsManager =
            CreditsManager(
                transactionRepository = mockTransactionRepository,
                billedRequestRepository = mockBilledRequestRepository,
            )
    }

    @Test
    fun `postTransaction should save transaction and return as DTO String type`() {
        val transactionEntity = validTransactionDto.toTransactionEntity()
        whenever(mockTransactionRepository.save(any<TransactionEntity>())).thenReturn(transactionEntity)
        val result = creditsManager.postTransaction(validTransactionDto)

        assertEquals(transactionDtoString, result)
        verify(mockTransactionRepository).save(
            argThat {
                valueOfChange == sampleValueOfChange &&
                    companyId == validCompanyId &&
                    triggeringUser == userId
            },
        )
    }

    @Test
    fun `getBalance should subtract debts from transactions and round to 1 decimal`() {
        val balanceTotal = BigDecimal("10.779")
        val debtTotal = BigDecimal("1.2")

        doReturn(balanceTotal).whenever(mockTransactionRepository).getTotalBalanceFromTransactions(validCompanyId)
        doReturn(debtTotal).whenever(mockBilledRequestRepository).getTotalCreditDebtFromBilledRequests(validCompanyId)

        val expected = (balanceTotal - debtTotal).setScale(1, RoundingMode.HALF_UP)
        val result = creditsManager.getBalance(validCompanyId)

        assertEquals(expected, result)
        assertEquals(BigDecimal("9.6"), result)
    }

    @Test
    fun `getBalance handles zero debt and transaction values`() {
        doReturn(BigDecimal.ZERO).whenever(mockTransactionRepository).getTotalBalanceFromTransactions(validCompanyId)
        doReturn(BigDecimal.ZERO).whenever(mockBilledRequestRepository).getTotalCreditDebtFromBilledRequests(validCompanyId)

        assertEquals(BigDecimal("0.0"), creditsManager.getBalance(validCompanyId))
    }
}
