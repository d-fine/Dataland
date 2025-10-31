package org.dataland.datalandaccountingservice.services

import org.dataland.datalandaccountingservice.model.TransactionDto
import org.dataland.datalandaccountingservice.repositories.BilledRequestRepository
import org.dataland.datalandaccountingservice.repositories.TransactionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/**
 * Service class for managing requests concerning Dataland credits.
 */
@Service("CreditsManager")
class CreditsManager
    @Autowired
    constructor(
        private val transactionRepository: TransactionRepository,
        private val billedRequestRepository: BilledRequestRepository,
    ) {
        /**
         * Post a Dataland credits transaction.
         * @param transactionDto contains all relevant information about the transaction
         * @return the saved transaction where all IDs are of type String
         */
        fun postTransaction(transactionDto: TransactionDto<UUID>): TransactionDto<String> =
            transactionRepository.save(transactionDto.toTransactionEntity()).toTransactionDtoString()

        /**
         * Calculate the current Dataland credits balance of the specified company.
         * @param companyId the ID of the company whose balance is to be calculated
         * @return the current Dataland credits balance of the specified company
         */
        fun getBalance(companyId: UUID): BigDecimal =
            (
                transactionRepository.getTotalBalanceFromTransactions(companyId) -
                    billedRequestRepository.getTotalCreditDebtFromBilledRequests(companyId)
            ).setScale(1, RoundingMode.HALF_UP)
    }
