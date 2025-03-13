package org.dataland.datalanduserservice.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException

/**
 * Specific ResourceNotFoundApiException thrown if a portfolio cannot be retrieved.
 */
class PortfolioNotFoundApiException(
    portfolioId: String,
) : ResourceNotFoundApiException(
        summary = "Portfolio with portfolioId $portfolioId does not exist.",
        message = "Portfolio with portfolioId $portfolioId does not exist.",
    )
