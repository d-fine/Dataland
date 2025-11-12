package org.dataland.datalandaccountingservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.dataland.datalandaccountingservice.model.TransactionDto
import java.math.BigDecimal
import java.util.UUID

/**
 * The database entity for storing transactions.
 */
@Entity
@Table(
    name = "transactions",
    indexes = [Index(name = "idx_transactions_company_id", columnList = "company_id")],
)
class TransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", nullable = false, updatable = false)
    val transactionId: UUID? = null,
    @Column(name = "value_of_change", nullable = false, precision = 14, scale = 4)
    val valueOfChange: BigDecimal,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "triggering_user", nullable = false)
    val triggeringUser: UUID,
    @Column(name = "reason_for_change")
    val reasonForChange: String?,
    @Column(name = "timestamp", nullable = false)
    val timestamp: Long,
) {
    /**
     * Converts this TransactionEntity object to a TransactionDto object with String IDs.
     */
    fun toTransactionDtoString(): TransactionDto<String> =
        TransactionDto(
            valueOfChange = valueOfChange,
            companyId = companyId.toString(),
            triggeringUser = triggeringUser.toString(),
            reasonForChange = reasonForChange,
            timestamp = timestamp,
        )
}
