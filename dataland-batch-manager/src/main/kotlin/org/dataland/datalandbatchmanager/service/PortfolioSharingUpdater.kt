package org.dataland.datalandbatchmanager.service
import org.dataland.dataSourcingService.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.userService.openApiClient.api.PortfolioControllerApi
import org.dataland.userService.openApiClient.infrastructure.ServerException
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

        /**
         * Updates portfolio sharing settings by removing shared user IDs from portfolios
         * which were shared by users who lost their admin or member status
         */
        fun updatePortfolioSharing() {
            val userIdsOfAdminsAndMembers = getAllUserIdsOfAdminsAndMembers()

            var chunkIndex = 0
            var totalProcessedPortfolios = 0
            var totalClearedPortfolios = 0

            do {
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
                chunkIndex++
            } while (chunkOfPortfolios.size == resultsPerPage)

            logger.info("Cleared portfolio sharing for $totalClearedPortfolios portfolios out of $totalProcessedPortfolios portfolios")
        }

        /**
         * Grabs all user IDs of Dataland admins and members and returns them as a set
         */
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
            val emptyPatch = PortfolioSharingPatch(sharedUserIds = emptySet())
            try {
                portfolioControllerApi.patchSharing(portfolioId, emptyPatch)
            } catch (e: ClientException) {
                logger.error("Client error (HTTP ${e.statusCode}) for portfolio $portfolioId: ${e.message}")
            } catch (e: ServerException) {
                logger.error("Server error (HTTP ${e.statusCode}) for portfolio $portfolioId")
            } catch (e: IllegalStateException) {
                logger.error("Request misconfigured for portfolio $portfolioId", e)
            }
        }
    }
