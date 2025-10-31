package org.dataland.datalandaccountingservice.repositories

import org.dataland.datalandaccountingservice.entities.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.util.UUID

/**
 * A JPA repository for managing TransactionEntity instances.
 */
interface TransactionRepository : JpaRepository<TransactionEntity, UUID> {
    /**
     * For the Dataland member company with the specified company ID, calculate the sum of the valueOfChange values
     * from all associated transactions.
     */
    @Query(
        "SELECT SUM(e.valueOfChange) FROM TransactionEntity e WHERE e.companyId = :billedCompanyId",
    )
    fun getTotalBalanceFromTransactions(
        @Param("billedCompanyId") billedCompanyId: UUID,
    ): BigDecimal
}
