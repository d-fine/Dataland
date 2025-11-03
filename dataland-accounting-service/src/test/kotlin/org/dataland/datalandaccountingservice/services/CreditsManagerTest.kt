package org.dataland.datalandaccountingservice.services

import org.dataland.datalandaccountingservice.entities.TransactionEntity
import org.dataland.datalandaccountingservice.model.TransactionDto
import org.dataland.datalandaccountingservice.repositories.BilledRequestRepository
import org.dataland.datalandaccountingservice.repositories.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class CreditsManagerTest {
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var billedRequestRepository: BilledRequestRepository
    private lateinit var creditsManager: CreditsManager

    @BeforeEach
    fun setUp() {
        transactionRepository = mock()
        billedRequestRepository = mock()
        creditsManager = CreditsManager(transactionRepository, billedRequestRepository)
    }

    @Test
    fun `postTransaction should save transaction and return as DTO String type`() {
        val transactionDto = mock<TransactionDto<UUID>>()
        val transactionEntity = mock<TransactionEntity>()
        val transactionDtoString = mock<TransactionDto<String>>()

        whenever(transactionDto.toTransactionEntity()).thenReturn(transactionEntity)
        whenever(transactionRepository.save(transactionEntity)).thenReturn(transactionEntity)
        whenever(transactionEntity.toTransactionDtoString()).thenReturn(transactionDtoString)

        val result = creditsManager.postTransaction(transactionDto)

        assertEquals(transactionDtoString, result)
        verify(transactionRepository).save(transactionEntity)
    }

    @Test
    fun `getBalance should subtract debts from transactions and round to 1 decimal`() {
        val companyId = UUID.randomUUID()
        val balanceTotal = BigDecimal("10.779")
        val debtTotal = BigDecimal("1.2")
        whenever(transactionRepository.getTotalBalanceFromTransactions(companyId)).thenReturn(balanceTotal)
        whenever(billedRequestRepository.getTotalCreditDebtFromBilledRequests(companyId)).thenReturn(debtTotal)

        val expected = (balanceTotal - debtTotal).setScale(1, RoundingMode.HALF_UP)
        val result = creditsManager.getBalance(companyId)

        assertEquals(expected, result)
        assertEquals(BigDecimal("9.6"), result)
    }

    @Test
    fun `getBalance handles zero debt and transaction values`() {
        val companyId = UUID.randomUUID()
        whenever(transactionRepository.getTotalBalanceFromTransactions(companyId)).thenReturn(BigDecimal.ZERO)
        whenever(billedRequestRepository.getTotalCreditDebtFromBilledRequests(companyId)).thenReturn(BigDecimal.ZERO)

        assertEquals(BigDecimal("0.0"), creditsManager.getBalance(companyId))
    }
}
