package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.services.CompanyGetter
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
    @Autowired private val companyGetter: CompanyGetter,
) {
    private val isinRegex = Regex("^[A-Z]{2}[A-Z\\d]{10}$")
    private val leiRegex = Regex("^[0-9A-Z]{18}[0-9]{2}$")
    private val permIdRegex = Regex("^\\d+$")

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
     * Determines the type corresponding to the value of a provided company identifier
     * @param identifierValue the identifier value
     * @return the identifier type
     */
    fun determineIdentifierTypeViaRegex(identifierValue: String): DataRequestCompanyIdentifierType? {
        val matchingRegexes =
            listOf(leiRegex, isinRegex, permIdRegex).filter { it.matches(identifierValue) }
        return when (matchingRegexes.size) {
            0 -> null
            1 -> {
                when {
                    matchingRegexes[0] == leiRegex -> DataRequestCompanyIdentifierType.Lei
                    matchingRegexes[0] == isinRegex -> DataRequestCompanyIdentifierType.Isin
                    matchingRegexes[0] == permIdRegex -> DataRequestCompanyIdentifierType.PermId
                    else -> null
                }
            }

            else -> DataRequestCompanyIdentifierType.MultipleRegexMatches
        }
    }

    /**
     * Returns the ID of the company corresponding to a provided identifier value, else null if none is found
     * @param identifierValue the identifier value
     * @return the company ID or null
     */
    fun getDatalandCompanyIdForIdentifierValue(identifierValue: String): String? {
        var datalandCompanyId: String? = null
        val bearerTokenOfRequestingUser = DatalandAuthentication.fromContext().credentials as String
        val matchingCompanyIdsAndNamesOnDataland =
            companyGetter.getCompanyIdsAndNamesForSearchString(identifierValue, bearerTokenOfRequestingUser)
        if (matchingCompanyIdsAndNamesOnDataland.size == 1) {
            datalandCompanyId = matchingCompanyIdsAndNamesOnDataland.first().companyId
        }
        dataRequestLogger
            .logMessageWhenCrossReferencingIdentifierValueWithDatalandCompanyId(identifierValue, datalandCompanyId)
        return datalandCompanyId
    }

    /**
     * Stores a DataRequestEntity from all necessary parameters if this object does not already exist in the database
     * @param identifierValue the value of the company identifier
     * @param identifierType the type of the company identifier
     * @param dataType the enum entry corresponding to the framework
     * @param reportingPeriod the reporting period
     * @param contactList a list of email addresses to inform about the potentially stored data request
     * @param message a message to equip the notification with
     */
    fun storeDataRequestEntityIfNotExisting(
        identifierValue: String,
        identifierType: DataRequestCompanyIdentifierType,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        contactList: List<String>? = null,
        message: String? = null,
    ): DataRequestEntity {
        findAlreadyExistingDataRequestForCurrentUser(identifierValue, dataType, reportingPeriod)?.also {
            return it
        }
        val dataRequestEntity = DataRequestEntity(
            DatalandAuthentication.fromContext().userId,
            dataType,
            reportingPeriod,
            identifierType,
            identifierValue,
        )
        dataRequestRepository.save(dataRequestEntity)
        val messageHistory = if (!contactList.isNullOrEmpty()) {
            mutableListOf(StoredDataRequestMessageObject(contactList, message, Instant.now().toEpochMilli()))
        } else {
            mutableListOf()
        }
        dataRequestEntity.associateMessages(messageHistory)
        messageRepository.saveAllAndFlush(dataRequestEntity.messageHistory)
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
            .findByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeAndReportingPeriod(
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
fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum? {
    return DataTypeEnum.entries.find { it.value == frameworkName }
}
