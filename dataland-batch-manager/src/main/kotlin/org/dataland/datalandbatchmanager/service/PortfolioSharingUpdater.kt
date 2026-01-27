package org.dataland.datalandbatchmanager.service
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.userService.openApiClient.api.PortfolioControllerApi
import org.dataland.userService.openApiClient.model.PortfolioSharingPatch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Service Class for updating the portfolio sharing for users which are no longer members or admin in a company
 *
 */

@Service
class PortfolioSharingUpdater
    @Autowired
    constructor(
        private val keycloakUserService: KeycloakUserService,
        private val companyRolesControllerApi: CompanyRolesControllerApi,
        private val portfolioControllerApi: PortfolioControllerApi,
        @Value("\${dataland.batch-manager.results-per-page:100}") private val resultsPerPage: Int,
        private val derivedRightsUtilsComponent: DerivedRightsUtilsComponent,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        fun updatePortfolioSharing() {
            val userIdsOfAdminsAndMembers = getAllUserIdsOfAdminsAndMembers()

            var chunkIndex = 0
            var totalProcessedPortfolios = 0
            var totalClearedPortfolios = 0

            while (true) {
                val chunkOfPortfolios =
                    portfolioControllerApi.getAllPortfolios(
                        chunkSize = resultsPerPage,
                        chunkIndex = chunkIndex,
                    )
                if (chunkOfPortfolios.isEmpty()) break

                chunkOfPortfolios.forEach { portfolio ->
                    if (portfolio.userId !in userIdsOfAdminsAndMembers && portfolio.sharedUserIds.isNotEmpty()) {
                        removePortfolioSharingForSinglePortfolio(portfolio.portfolioId)
                        totalClearedPortfolios++
                    }
                }
                totalProcessedPortfolios += chunkOfPortfolios.size
                if (chunkOfPortfolios.size < resultsPerPage) break
                chunkIndex++
            }

            logger.info("Cleared portfolio sharing for $totalClearedPortfolios portfolios out of $totalProcessedPortfolios portfolios")
        }

        fun getAllUserIdsOfAdminsAndMembers(): Set<String> {
            val userIdsOfAdmins = keycloakUserService.getUsersByRole(role = "ROLE_ADMIN").map { it.userId }.toSet()

            val userIdsOfMembers =
                companyRolesControllerApi
                    .getExtendedCompanyRoleAssignments()
                    .map { it.userId }
                    .filter { derivedRightsUtilsComponent.isUserDatalandMember(it) }
                    .toSet()

            val userIdsOfAdminsAndMembers = userIdsOfAdmins + userIdsOfMembers
            require(userIdsOfAdminsAndMembers.isNotEmpty()) {
                "No Dataland admins or members found. Portfolio sharing update failed."
            }

            logger.info("Found ${userIdsOfAdminsAndMembers.size} users who are Dataland admins or members")

            return userIdsOfAdminsAndMembers
        }

        private fun removePortfolioSharingForSinglePortfolio(portfolioId: String) {
            val emptyPortfolioSharingPatch =
                PortfolioSharingPatch(
                    sharedUserIds = emptySet(),
                )
            try {
                portfolioControllerApi.patchSharing(portfolioId = portfolioId, portfolioSharingPatch = emptyPortfolioSharingPatch)
            } catch (
                e: Exception,
            ) {
                logger.error("Failed to clear portfolio sharing for portfolio with ID: $portfolioId", e)
            }
        }
    }
