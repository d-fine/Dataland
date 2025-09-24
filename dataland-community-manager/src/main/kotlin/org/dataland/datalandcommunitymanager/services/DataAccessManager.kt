package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.CommunityManagerDataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * The DataAccessManager contains methods for the data access logic.
 */
@Service
class DataAccessManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val communityManagerDataRequestProcessingUtils: CommunityManagerDataRequestProcessingUtils,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This method finds all DataRequestsEntity for a specified dataset that have a specific accessStatus.
     * @param companyId the companyId for which the access status should be checked
     * @param reportingPeriod the reportingPeriod for which the access status should be checked
     * @param dataType the framework dataType for which the access status should be checked
     * @param userId the userId for which the access status should be checked
     */
    @Transactional
    fun hasAccessToPrivateDataset(
        companyId: String,
        reportingPeriod: String,
        dataType: DataTypeEnum,
        userId: String,
    ): Boolean {
        val hasAccess =
            dataRequestRepository
                .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                    userId = userId, datalandCompanyId = companyId, dataType = dataType.name,
                    reportingPeriod = reportingPeriod,
                ).any { it.accessStatus == AccessStatus.Granted }

        if (hasAccess) {
            logger.info(
                "Access for the dataset (companyId: $companyId, framework: $dataType, " +
                    "reportingPeriod: $reportingPeriod) is granted",
            )
        }
        return hasAccess
    }

    /**
     * This method checks if the requesting user has been granted access to the information for a specific company,
     * reporting period and data type.
     * If the datatype input has no correspond entry in the datatype enum class then an exception thrown
     * If datatype is not vsme the method returns
     * If there is no access or no data set an exception is thrown.
     * @param companyId the companyId for which the access status should be checked
     * @param reportingPeriod the reportingPeriod for which the access status should be checked
     * @param dataType the framework dataType for which the access status should be checked
     * @param userId the userId for which the access status should be checked
     */
    @Transactional
    fun hasAccessToDataset(
        companyId: String,
        reportingPeriod: String,
        dataType: String,
        userId: String,
    ) {
        val dataTypeEnum =
            DataTypeEnum.decode(dataType)
                ?: throw InvalidInputApiException(
                    "The provided input did not match expected values.",
                    "The $dataType was not recognized by the system. Please check your input",
                )

        if (dataTypeEnum != DataTypeEnum.vsme) {
            return
        }

        val hasAccess =
            hasAccessToPrivateDataset(companyId, reportingPeriod, dataTypeEnum, userId)

        if (!hasAccess) {
            throw ResourceNotFoundApiException(
                "The user has no access to the dataset or the dataset does not exists.",
                "The user $userId cannot access the dataset for the company $companyId, for the data type " +
                    "$dataType and the reporting period $reportingPeriod. The dataset may not exists.",
            )
        }
    }

    /**
     * This method is used to request access to a private dataset
     * @param userId the userId of the user requesting access to the dataset
     * @param companyId the companyId of the company to which the dataset belongs
     * @param dataType the datatype of the dataset to which access was requested
     * @param reportingPeriod the reportingPeriod of the dataset to which access was requested
     */
    fun createAccessRequestToPrivateDataset(
        userId: String,
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        contacts: Set<String>?,
        message: String?,
    ) {
        val existingRequestsOfUser =
            dataRequestRepository
                .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                    userId = userId, datalandCompanyId = companyId, dataType = dataType.name,
                    reportingPeriod = reportingPeriod,
                )
        if (!existingRequestsOfUser.isEmpty()) {
            val dataRequestEntity = existingRequestsOfUser[0]

            val modificationTime = Instant.now().toEpochMilli()
            dataRequestEntity.lastModifiedDate = modificationTime
            dataRequestRepository.save(dataRequestEntity)
            if (dataRequestEntity.accessStatus == AccessStatus.Revoked ||
                dataRequestEntity.accessStatus ==
                AccessStatus.Declined
            ) {
                communityManagerDataRequestProcessingUtils.addNewRequestStatusToHistory(
                    dataRequestEntity = dataRequestEntity, requestStatus = dataRequestEntity.requestStatus,
                    accessStatus = AccessStatus.Pending, requestStatusChangeReason = null,
                    modificationTime = modificationTime,
                )
                logger.info("Patching request ${dataRequestEntity.dataRequestId} with status ${AccessStatus.Pending}")
            }
        } else {
            storeAccessRequestEntityAsPending(
                datalandCompanyId = companyId, dataType = dataType, reportingPeriod = reportingPeriod,
                contacts = contacts, message = message, userId = userId,
            )
        }
    }

    /**
     * Stores a DataRequestEntity from all necessary parameters
     * @param datalandCompanyId the companyID in Dataland
     * @param dataType the enum entry corresponding to the framework
     * @param reportingPeriod the reporting period
     * @param contacts a list of email addresses to inform about the potentially stored data request
     * @param message a message to equip the notification with
     */
    fun storeAccessRequestEntityAsPending(
        datalandCompanyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        contacts: Set<String>? = null,
        message: String? = null,
        userId: String,
    ): DataRequestEntity {
        val creationTime = Instant.now().toEpochMilli()

        val dataRequestEntity =
            DataRequestEntity(
                userId = userId,
                dataType = dataType.value,
                notifyMeImmediately = false,
                reportingPeriod = reportingPeriod,
                datalandCompanyId = datalandCompanyId,
                creationTimestamp = creationTime,
            )
        dataRequestRepository.save(dataRequestEntity)

        communityManagerDataRequestProcessingUtils.addNewRequestStatusToHistory(
            dataRequestEntity = dataRequestEntity, requestStatus = RequestStatus.Answered,
            accessStatus = AccessStatus.Pending, requestStatusChangeReason = null,
            modificationTime = creationTime,
        )

        if (!contacts.isNullOrEmpty()) {
            communityManagerDataRequestProcessingUtils.addMessageToMessageHistory(
                dataRequestEntity = dataRequestEntity,
                contacts = contacts, message = message, modificationTime = creationTime,
            )
        }
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId)

        return dataRequestEntity
    }

    /**
     * Checks whether a access already exists on Dataland in pending status
     * @param companyId the company ID of the data request
     * @param framework the framework of the data request
     * @param reportingPeriod the reporting period of the data request
     * @return true if the access request already exists in pending status for the current user, false otherwise
     */
    fun existsAccessRequestWithNonPendingStatus(
        companyId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
        userId: String,
    ): Boolean {
        val pendingDataRequests =
            findAlreadyExistingAccessRequestForUser(
                companyId = companyId, framework = framework, reportingPeriod = reportingPeriod,
                accessStatus = AccessStatus.Pending, requestingUserId = userId,
            )

        return (!pendingDataRequests.isNullOrEmpty())
    }

    /**
     * Retrieves the access requests on Dataland for the provided specifications and the current user
     * @param companyId the company ID of the access requests
     * @param framework the framework of the access requests
     * @param reportingPeriod the reporting period of the access requests
     * @param accessStatus the access status of the request
     * @return a list of the found data requests, or null if none was found
     */
    fun findAlreadyExistingAccessRequestForUser(
        companyId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
        accessStatus: AccessStatus,
        requestingUserId: String,
    ): List<DataRequestEntity>? {
        val foundRequests =
            dataRequestRepository
                .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                    requestingUserId, companyId, framework.name, reportingPeriod,
                ).filter {
                    it.accessStatus == accessStatus
                }
        return foundRequests
    }
}
