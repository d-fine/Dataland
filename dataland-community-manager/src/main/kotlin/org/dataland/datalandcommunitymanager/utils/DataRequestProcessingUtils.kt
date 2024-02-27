package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Class holding utility functions used by the both the bulk and the single data request manager
 */
@Service
class DataRequestProcessingUtils(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val messageRepository: MessageRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyApi: CompanyDataControllerApi,
) {
    /**
     * We want to avoid users from using other authentication methods than jwt-authentication, such as
     * api-key-authentication.
     */
    fun throwExceptionIfNotJwtAuth() {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        }
    }

    /**
     * Returns the ID of the company corresponding to a provided identifier value, else null if none is found
     * @param identifierValue the identifier value
     * @return the company ID or null
     */
    fun getDatalandCompanyIdForIdentifierValue(identifierValue: String): String? {
        val matchingCompanyIdsAndNamesOnDataland =
            companyApi.getCompaniesBySearchString(identifierValue)
        val datalandCompanyId = if (matchingCompanyIdsAndNamesOnDataland.size == 1) {
            matchingCompanyIdsAndNamesOnDataland.first().companyId
        } else if (matchingCompanyIdsAndNamesOnDataland.size > 1) {
            throw InvalidInputApiException(
                summary = "No unique identifier. Multiple companies could be found.",
                message = "Multiple companies have been found for the identifier you specified.",
            )
        } else {
            null
        }
        dataRequestLogger
            .logMessageWhenCrossReferencingIdentifierValueWithDatalandCompanyId(identifierValue, datalandCompanyId)
        return datalandCompanyId
    }

    /**
     * Stores a DataRequestEntity from all necessary parameters if this object does not already exist in the database
     * @param datalandCompanyId the companyID in dataland
     * @param dataType the enum entry corresponding to the framework
     * @param reportingPeriod the reporting period
     * @param contacts a list of email addresses to inform about the potentially stored data request
     * @param message a message to equip the notification with
     */
    fun storeDataRequestEntityIfNotExisting(
        datalandCompanyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        contacts: Set<String>? = null,
        message: String? = null,
    ): DataRequestEntity {
        findAlreadyExistingDataRequestForCurrentUser(datalandCompanyId, dataType, reportingPeriod)?.also {
            return it
        }
        val dataRequestEntity = DataRequestEntity(
            DatalandAuthentication.fromContext().userId,
            dataType,
            reportingPeriod,
            datalandCompanyId,
            Instant.now().toEpochMilli(),
        )
        dataRequestRepository.save(dataRequestEntity)
        if (!contacts.isNullOrEmpty()) {
            val messageHistory = listOf(StoredDataRequestMessageObject(contacts, message, Instant.now().toEpochMilli()))
            dataRequestEntity.associateMessages(messageHistory)
            messageRepository.saveAllAndFlush(dataRequestEntity.messageHistory)
        }
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId)
        return dataRequestEntity
    }

    private fun findAlreadyExistingDataRequestForCurrentUser(
        identifierValue: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
    ): DataRequestEntity? {
        val requestingUserId = DatalandAuthentication.fromContext().userId
        val foundRequest = dataRequestRepository
            .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                requestingUserId, identifierValue, framework.name, reportingPeriod,
            )
        if (foundRequest != null) {
            dataRequestLogger.logMessageForCheckingIfDataRequestAlreadyExists(identifierValue, framework)
        }
        return foundRequest
    }
}

/**
 * Finds the entry in the DataType enum corresponding to a provided framework name
 * @param frameworkName the name of the framework
 * @return the corresponding enum entry
 */
fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum {
    return DataTypeEnum.entries.find { it.value == frameworkName }
        ?: throw InvalidInputApiException("Framework non-existent", "Framework type is non-existent")
}
