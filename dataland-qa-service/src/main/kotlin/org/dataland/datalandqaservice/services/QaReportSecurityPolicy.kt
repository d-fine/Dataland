package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * The centralized security policy for QA reports.
 */
@Service
class QaReportSecurityPolicy(
    @Autowired private val userAuthenticatedBackendClient: UserAuthenticatedBackendClient,
) {
    /**
     * Checks if a user can change the active status of a report.
     * @param report the report to change the active status of
     * @param user the user requesting the change
     * @return true if the user can change the active status of the report, false otherwise
     */
    fun canUserSetQaReportStatus(
        report: QaReportEntity,
        user: DatalandAuthentication,
    ): Boolean = report.reporterUserId == user.userId || user.roles.contains(DatalandRealmRole.ROLE_ADMIN)

    /**
     * Checks if a user can change the active status of a report.
     * @param report the report to change the active status of
     * @param user the user requesting the change
     * @return true if the user can change the active status of the report, false otherwise
     */
    fun canUserSetQaReportStatus(
        report: DataPointQaReportEntity,
        user: DatalandAuthentication,
    ): Boolean = report.reporterUserId == user.userId || user.roles.contains(DatalandRealmRole.ROLE_ADMIN)

    private fun relayAccessRequestException(
        apiRequestException: ClientException,
        dataId: String,
    ): Exception =
        when (apiRequestException.statusCode) {
            HttpStatus.FORBIDDEN.value() -> {
                InsufficientRightsApiException(
                    "Required access rights missing",
                    "You do not have the required access rights to view the data id: $dataId",
                    apiRequestException,
                )
            }

            HttpStatus.NOT_FOUND.value() -> {
                ResourceNotFoundApiException(
                    "Data id not found",
                    "No data id with the id: $dataId could be found.",
                    apiRequestException,
                )
            }

            else -> apiRequestException
        }

    /**
     * Checks if a user can view a QA report.
     * @param dataPointId the ID of the data set the QA report is associated with
     * @param user the user requesting the view
     */
    fun ensureUserCanViewDataPoint(
        dataPointId: String,
        user: DatalandAuthentication,
    ) {
        val userAuthenticatedMetadataController =
            userAuthenticatedBackendClient
                .getDataPointControllerApiForUserAuthentication(user)
        try {
            userAuthenticatedMetadataController.getDataPointMetaInfo(dataPointId)
            return
        } catch (apiRequestException: ClientException) {
            throw relayAccessRequestException(apiRequestException, dataPointId)
        }
    }

    /**
     * Checks if a user can view a QA report.
     * @param dataId the ID of the data set the QA report is associated with
     * @param user the user requesting the view
     */
    fun ensureUserCanViewQaReportForDataId(
        dataId: String,
        user: DatalandAuthentication,
    ) {
        val userAuthenticatedMetadataController =
            userAuthenticatedBackendClient
                .getMetaDataControllerApiForUserAuthentication(user)
        try {
            userAuthenticatedMetadataController.getDataMetaInfo(dataId)
            return
        } catch (apiRequestException: ClientException) {
            throw relayAccessRequestException(apiRequestException, dataId)
        }
    }
}
