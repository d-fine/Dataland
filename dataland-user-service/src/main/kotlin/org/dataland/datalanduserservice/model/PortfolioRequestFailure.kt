package org.dataland.datalanduserservice.model

/**
 * Describes a failure that occurred while creating bulk data requests for a portfolio.
 *
 * @param portfolioId the id of the affected portfolio
 * @param userId the id of the user owning the portfolio
 * @param stage the processing stage in which the failure occurred
 * @param cause the exception that was thrown
 */
data class PortfolioRequestFailure(
    val portfolioId: String,
    val userId: String,
    val stage: Stage,
    val cause: Exception,
) {
    /**
     * The processing stage of the nightly job in which a failure can occur.
     */
    enum class Stage {
        UPDATE_COMPANIES,
        POST_REQUESTS,
    }
}
