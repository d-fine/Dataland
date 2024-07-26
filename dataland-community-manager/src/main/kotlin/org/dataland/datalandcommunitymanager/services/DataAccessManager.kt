package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.jvm.optionals.getOrElse

@Service
class DataAccessManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val dataRequestProcessingUtils: DataRequestProcessingUtils,
) {

    /**
     * This method finds all DataRequestsEntity for a specified dataset that have a specific accessStatus.
     * @param companyId the companyId for which the access status should be checked
     * @param reportingPeriod the reportingPeriod for which the access status should be checked
     * @param dataType the framework dataType for which the access status should be checked
     * @param userId the userId for which the access status should be checked
     * @param accessStatus the accessStatus for which the check should be conducted
     */
    fun findRequestsByAccessStatus(
        companyId: String,
        reportingPeriod: String,
        dataType: DataTypeEnum,
        userId: String,
        accessStatus: AccessStatus,
    ): List<DataRequestEntity>? {
        val foundAccess = dataRequestRepository
            .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId, companyId, dataType.name, reportingPeriod,
            )?.filter {
            it.accessStatus == accessStatus
        }
        if (foundAccess != null) {
            dataRequestLogger.logMessageForCheckingIfUserHasAccessToDataset(
                companyId,
                dataType,
                reportingPeriod,
                accessStatus,
            )
        }
        // TODO Test if this only checks the toplevel value of accessStatus or if this goes through the requestHistory
        //  and checks if there was at least on Granted accessStatus
        // TODO if it his the second case logic has to be adapted
        // TODO name of this method is not quite right in regards to the inputs
        return foundAccess
    }

    /**
     * This method checks if the requesting user has granted access to the information for a specific company,
     * reporting period and data type.
     * If there is no access or no data set an exception is thrown.
     * @param companyId the companyId for which the access status should be checked
     * @param reportingPeriod the reportingPeriod for which the access status should be checked
     * @param dataType the framework dataType for which the access status should be checked
     * @param userId the userId for which the access status should be checked
     */
    @Transactional
    fun hasAccessToPrivateDataset(
        companyId: String,
        reportingPeriod: String,
        dataType: String,
        userId: String,
    ) {
        val dataTypeEnum = DataTypeEnum.decode(dataType)
            ?: throw InvalidInputApiException(
                "The provided input did not match expected values.",
                "The $dataType was not recognized by the system. Please check your input",
            )

        if (dataTypeEnum != DataTypeEnum.vsme) {
            return
        }

        val grantedAccessStatuses =
            findRequestsByAccessStatus(companyId, reportingPeriod, dataTypeEnum, userId, AccessStatus.Granted)

        if (!grantedAccessStatuses.isNullOrEmpty()) {
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
        val existingRequestsOfUser = dataRequestRepository
            .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId, companyId, dataType.name, reportingPeriod,
            )
        if (!existingRequestsOfUser.isNullOrEmpty()) {
            val dataRequestId = existingRequestsOfUser[0].dataRequestId
            val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
                throw DataRequestNotFoundApiException(dataRequestId)
            }
            val modificationTime = Instant.now().toEpochMilli()
            dataRequestEntity.lastModifiedDate = modificationTime
            dataRequestRepository.save(dataRequestEntity)
            // TODO discuss if declined should be removed from this condition
            if (dataRequestEntity.accessStatus == AccessStatus.Revoked || dataRequestEntity.accessStatus ==
                AccessStatus.Declined || dataRequestEntity.accessStatus == AccessStatus.Public
            ) {
                dataRequestProcessingUtils.addNewRequestStatusToHistory(
                    dataRequestEntity, dataRequestEntity.requestStatus, AccessStatus.Pending, modificationTime,
                )

                dataRequestLogger.logMessageForPatchingAccessStatus(dataRequestId, AccessStatus.Pending)
            }
        } else {
            storeAccessRequestEntityAsPending(
                companyId, dataType, reportingPeriod,
                contacts.takeIf { !it.isNullOrEmpty() },
                message.takeIf { !it.isNullOrBlank() },
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
    ): DataRequestEntity {
        val creationTime = Instant.now().toEpochMilli()

        val dataRequestEntity = DataRequestEntity(
            DatalandAuthentication.fromContext().userId,
            dataType.value,
            reportingPeriod,
            datalandCompanyId,
            creationTime,
        )
        dataRequestRepository.save(dataRequestEntity)

        dataRequestProcessingUtils.addNewRequestStatusToHistory(
            dataRequestEntity, RequestStatus.Answered, AccessStatus.Pending, creationTime,
        )

        if (!contacts.isNullOrEmpty()) {
            dataRequestProcessingUtils.addNewMessageToHistory(dataRequestEntity, contacts, message, creationTime)
        }
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId)

        return dataRequestEntity
    }
}
