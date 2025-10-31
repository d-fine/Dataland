package org.dataland.datalandaccountingservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandaccountingservice.entities.TransactionEntity
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.AccountingServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import java.math.BigDecimal
import java.util.UUID

/**
 * This data class represents the transaction DTO sent from the controller layer through to the databases.
 */
data class TransactionDto<IdType>(
    @field:Schema(
        implementation = BigDecimal::class,
        description = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_VALUE_OF_CHANGE_DESCRIPTION,
        example = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_VALUE_OF_CHANGE_EXAMPLE,
    )
    val valueOfChange: BigDecimal,
    @field:Schema(
        description = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: IdType,
    @field:Schema(
        description = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_TRIGGERING_USER_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val triggeringUser: IdType,
    @field:Schema(
        description = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_REASON_FOR_CHANGE_DESCRIPTION,
        example = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_REASON_FOR_CHANGE_EXAMPLE,
    )
    val reasonForChange: String?,
    @field:Schema(
        description = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_TIMESTAMP_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.GENERAL_TIMESTAMP_EXAMPLE,
    )
    val timestamp: Long,
) {
    /**
     * Converts this TransactionDto object to a TransactionEntity object.
     */
    fun toTransactionEntity(): TransactionEntity =
        TransactionEntity(
            valueOfChange = valueOfChange,
            companyId = UUID.fromString(companyId.toString()),
            triggeringUser = UUID.fromString(triggeringUser.toString()),
            reasonForChange = reasonForChange,
            timestamp = timestamp,
        )
}
