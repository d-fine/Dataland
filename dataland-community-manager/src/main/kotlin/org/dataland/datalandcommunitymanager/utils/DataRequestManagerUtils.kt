package org.dataland.datalandcommunitymanager.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.CompanyGetter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

/**
 * Class holding utility functions used by the both the bulk and the single data request manager
 */
class DataRequestManagerUtils(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyGetter: CompanyGetter,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val isinRegex = Regex("^[A-Z]{2}[A-Z\\d]{10}$")
    private val leiRegex = Regex("^[0-9A-Z]{18}[0-9]{2}$")
    private val permIdRegex = Regex("^\\d+$")

    private val emptyMutableListOfStoredDataRequestMessageObjectsAsString =
        objectMapper.writeValueAsString(mutableListOf<StoredDataRequestMessageObject>())

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
     * Builds a StoredDataRequest object from a DataRequestEntity
     * @param dataRequestEntity the DataRequestEntity
     * @return the resulting StoredDataRequest
     */
    fun buildStoredDataRequestFromDataRequestEntity(dataRequestEntity: DataRequestEntity): StoredDataRequest {
        return StoredDataRequest(
            dataRequestEntity.dataRequestId,
            dataRequestEntity.userId,
            dataRequestEntity.creationTimestamp,
            getDataTypeEnumForFrameworkName(dataRequestEntity.dataTypeName),
            dataRequestEntity.reportingPeriod,
            dataRequestEntity.dataRequestCompanyIdentifierType,
            dataRequestEntity.dataRequestCompanyIdentifierValue,
            objectMapper.readValue(
                dataRequestEntity.messageHistory ?: emptyMutableListOfStoredDataRequestMessageObjectsAsString,
                object : TypeReference<MutableList<StoredDataRequestMessageObject>>() {},
            ),
            dataRequestEntity.lastModifiedDate,
            dataRequestEntity.requestStatus,
        )
    }

    /**
     * Finds the entry in the DataType enum corresponding to a provided framework name
     * @param frameworkName the name of the framework
     * @return the corresponding enum entry
     */
    fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum? {
        return DataTypeEnum.entries.find { it.value == frameworkName }
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
        val dataRequestEntity = buildDataRequestEntity(
            dataType,
            reportingPeriod,
            identifierType,
            identifierValue,
            contactList,
            message,
        )
        if (!isDataRequestAlreadyExisting(identifierValue, dataType, reportingPeriod)) {
            storeDataRequestEntity(dataRequestEntity)
        }
        return dataRequestEntity
    }

    private fun buildDataRequestEntity(
        framework: DataTypeEnum,
        reportingPeriod: String,
        identifierType: DataRequestCompanyIdentifierType,
        identifierValue: String,
        contactList: List<String>?,
        message: String?,
    ): DataRequestEntity {
        val dataRequestId = UUID.randomUUID().toString()
        val currentUserId = DatalandAuthentication.fromContext().userId
        val currentTimestamp = Instant.now().toEpochMilli()
        val messageHistory = if (!contactList.isNullOrEmpty()) {
            mutableListOf(StoredDataRequestMessageObject(contactList, message, currentTimestamp))
        } else {
            mutableListOf()
        }
        return DataRequestEntity(
            dataRequestId = dataRequestId,
            userId = currentUserId,
            creationTimestamp = currentTimestamp,
            dataTypeName = framework.value,
            reportingPeriod = reportingPeriod,
            dataRequestCompanyIdentifierType = identifierType,
            dataRequestCompanyIdentifierValue = identifierValue,
            messageHistory = objectMapper.writeValueAsString(messageHistory),
            lastModifiedDate = currentTimestamp,
            requestStatus = RequestStatus.Open,
        )
    }

    private fun isDataRequestAlreadyExisting(
        identifierValue: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
    ): Boolean {
        val requestingUserId = DatalandAuthentication.fromContext().userId
        val isAlreadyExisting = dataRequestRepository
            .existsByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeNameAndReportingPeriod(
                requestingUserId, identifierValue, framework.name, reportingPeriod,
            )
        if (isAlreadyExisting) {
            dataRequestLogger
                .logMessageForCheckingIfDataRequestAlreadyExists(identifierValue, framework)
        }
        return isAlreadyExisting
    }

    private fun storeDataRequestEntity(dataRequestEntity: DataRequestEntity) {
        dataRequestRepository.save(dataRequestEntity)
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId)
    }
}
