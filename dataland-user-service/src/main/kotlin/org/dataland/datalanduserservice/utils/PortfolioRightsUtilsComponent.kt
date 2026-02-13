package org.dataland.datalanduserservice.utils

import org.dataland.datalandbackendutils.utils.DerivedRightsUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalanduserservice.service.PortfolioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Utility bean for functionality concerning portfolio rights.
 */
@Component("PortfolioRightsUtilsComponent")
class PortfolioRightsUtilsComponent(
    @Autowired private val inheritedRolesControllerApi: InheritedRolesControllerApi,
    @Autowired private val portfolioService: PortfolioService,
) {
    /**
     * Check whether the specified user is a Dataland member based on their inherited roles.
     * @param userId the Dataland ID of the user in question
     * @return true if the user is a Dataland member, false otherwise
     */
    fun isUserDatalandMember(userId: String): Boolean =
        DerivedRightsUtils.isUserDatalandMember(
            inheritedRolesControllerApi.getInheritedRoles(userId),
        )

    /**
     * Check whether a non-admin user may create, replace or patch the monitoring of the specified portfolio.
     * @param userId the Dataland ID of the user in question
     * @param isMonitored whether the portfolio shall be actively monitored after the operation
     * @return true if the non-admin user may carry out this portfolio operation, false otherwise
     */
    fun mayNonAdminUserManipulatePortfolioMonitoring(
        userId: String,
        isMonitored: Boolean,
    ): Boolean = !isMonitored || isUserDatalandMember(userId)

    /**
     * Check whether the logged-in user is the owner of the specified portfolio.
     * @param portfolioId the ID of the portfolio in question
     * @return true if the user is the owner of the portfolio, false otherwise
     */
    fun isUserPortfolioOwner(portfolioId: String): Boolean {
        val correlationId = UUID.randomUUID().toString()
        return portfolioService.existsPortfolioForUser(portfolioId, correlationId)
    }

    /**
     * Check whether the specified portfolio is shared with the specified user.
     * @param userId the Dataland ID of the user in question
     * @param portfolioId the ID of the portfolio in question
     * @return true if the portfolio is shared with the user, false otherwise
     */
    fun isPortfolioSharedWithUser(
        userId: String,
        portfolioId: String,
    ): Boolean {
        val correlationId = UUID.randomUUID().toString()
        val portfolio = portfolioService.getPortfolio(portfolioId, correlationId)
        return portfolio.sharedUserIds.contains(userId)
    }
}
