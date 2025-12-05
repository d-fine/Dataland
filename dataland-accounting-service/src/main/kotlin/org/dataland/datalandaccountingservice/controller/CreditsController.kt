package org.dataland.datalandaccountingservice.controller

import org.dataland.datalandaccountingservice.api.CreditsApi
import org.dataland.datalandaccountingservice.model.TransactionDto
import org.dataland.datalandaccountingservice.model.TransactionPost
import org.dataland.datalandaccountingservice.services.CreditsManager
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Controller for handling credit-related API endpoints.
 */
@RestController
class CreditsController(
    private val creditsManager: CreditsManager,
) : CreditsApi {
    override fun postTransaction(
        companyId: String,
        transactionPost: TransactionPost,
    ): ResponseEntity<TransactionDto<String>> =
        ResponseEntity.ok(
            creditsManager.postTransaction(
                TransactionDto<UUID>(
                    valueOfChange = transactionPost.valueOfChange,
                    companyId = ValidationUtils.convertToUUID(companyId),
                    triggeringUser = UUID.fromString(DatalandAuthentication.fromContext().userId),
                    reasonForChange = transactionPost.reasonForChange,
                    timestamp = Instant.now().toEpochMilli(),
                ),
            ),
        )

    override fun getBalance(companyId: String): ResponseEntity<BigDecimal> =
        ResponseEntity.ok(
            creditsManager.getBalance(
                ValidationUtils.convertToUUID(companyId),
            ),
        )
}
