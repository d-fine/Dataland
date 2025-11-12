package org.dataland.datalandaccountingservice.repositories

import org.dataland.datalandaccountingservice.entities.BilledRequestEntity
import org.dataland.datalandaccountingservice.model.BilledRequestEntityId
import org.dataland.datalandaccountingservice.repositories.utils.JPQLQueryFragments
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.util.UUID

/**
 * A JPA repository for managing BilledRequestEntity instances.
 */
interface BilledRequestRepository : JpaRepository<BilledRequestEntity, BilledRequestEntityId> {
    /**
     * For the Dataland member with the specified company ID, get the total amount of credits to deduct
     * based on billed requests in which they are involved.
     */
    @Query(
        "SELECT COALESCE(SUM(credit_debts), 0) FROM (${JPQLQueryFragments.CREDIT_DEBTS_FROM_BILLED_REQUESTS_FOR_MEMBER_TO_BILL})",
    )
    fun getTotalCreditDebtFromBilledRequests(
        @Param("billedCompanyId") billedCompanyId: UUID,
    ): BigDecimal
}
