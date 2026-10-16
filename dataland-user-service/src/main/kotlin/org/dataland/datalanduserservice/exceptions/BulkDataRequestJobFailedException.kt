package org.dataland.datalanduserservice.exceptions

import org.dataland.datalanduserservice.model.PortfolioRequestFailure

/**
 * Thrown at the end of the nightly bulk data request job if the processing of at least one portfolio failed.
 * The first failure is the cause, all further failures are attached as suppressed exceptions.
 *
 * @param failures all failures that occurred during the job run
 */
class BulkDataRequestJobFailedException(
    val failures: List<PortfolioRequestFailure>,
) : RuntimeException(
        "Bulk data request job failed for ${failures.size} portfolio(s): " +
            failures.joinToString { "${it.portfolioId} (user ${it.userId}, ${it.stage})" },
        failures.firstOrNull()?.cause,
    ) {
    init {
        failures.drop(1).forEach { addSuppressed(it.cause) }
    }
}
