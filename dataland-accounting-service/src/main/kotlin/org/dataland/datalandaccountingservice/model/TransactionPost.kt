package org.dataland.datalandaccountingservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.AccountingServiceOpenApiDescriptionsAndExamples
import java.math.BigDecimal

/**
 * This data class represents the JSON payload for posting a transaction.
 * @param valueOfChange The amount by which the transaction changes the company's credits balance (may be positive or negative).
 * @param reasonForChange An optional reason for the transaction.
 */
data class TransactionPost(
    @field:Schema(
        implementation = BigDecimal::class,
        description = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_VALUE_OF_CHANGE_DESCRIPTION,
        example = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_VALUE_OF_CHANGE_EXAMPLE,
    )
    val valueOfChange: BigDecimal,
    @field:Schema(
        description = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_REASON_FOR_CHANGE_DESCRIPTION,
        example = AccountingServiceOpenApiDescriptionsAndExamples.TRANSACTION_REASON_FOR_CHANGE_EXAMPLE,
    )
    val reasonForChange: String? = null,
)
