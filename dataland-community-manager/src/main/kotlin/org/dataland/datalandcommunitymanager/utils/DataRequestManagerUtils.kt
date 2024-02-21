package org.dataland.datalandcommunitymanager.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
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
    @Autowired private val companyApi: CompanyDataControllerApi,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * Returns the ID of the company corresponding to a provided identifier value, else null if none is found
     * @param identifierValue the identifier value
     * @return the company ID or null
     */
    fun getDatalandCompanyIdForIdentifierValue(identifierValue: String): String? {
        var datalandCompanyId: String? = null
        val matchingCompanyIdsAndNamesOnDataland =
            companyApi.getCompaniesBySearchString(identifierValue)
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
            dataRequestEntity.datalandCompanyId,
            objectMapper.readValue(
                dataRequestEntity.messageHistory,
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
     * @param datalandCompanyId the companyID in dataland
     * @param dataType the enum entry corresponding to the framework
     * @param reportingPeriod the reporting period
     * @param contactList a list of email addresses to inform about the potentially stored data request
     * @param message a message to equip the notification with
     */
    fun storeDataRequestEntityIfNotExisting(
        datalandCompanyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        contactList: List<String>? = null,
        message: String? = null,
    ): DataRequestEntity {
        val dataRequestEntity = buildDataRequestEntity(
            dataType,
            reportingPeriod,
            datalandCompanyId,
            contactList,
            message,
        )
        if (!isDataRequestAlreadyExisting(datalandCompanyId, dataType, reportingPeriod)) {
            storeDataRequestEntity(dataRequestEntity)
        }
        return dataRequestEntity
    }

    private fun buildDataRequestEntity(
        framework: DataTypeEnum,
        reportingPeriod: String,
        datalandCompanyId: String,
        contactList: List<String>?,
        message: String?,
    ): DataRequestEntity {
        val dataRequestId = UUID.randomUUID().toString()
        val currentUserId = DatalandAuthentication.fromContext().userId
        val currentTimestamp = Instant.now().toEpochMilli()
        val messageHistory = if (!isContactListTrivial(contactList)) {
            mutableListOf(StoredDataRequestMessageObject(contactList!!, message, currentTimestamp))
        } else {
            mutableListOf()
        }
        return DataRequestEntity(
            dataRequestId = dataRequestId,
            userId = currentUserId,
            creationTimestamp = currentTimestamp,
            dataTypeName = framework.value,
            reportingPeriod = reportingPeriod,
            datalandCompanyId = datalandCompanyId,
            messageHistory = objectMapper.writeValueAsString(messageHistory),
            lastModifiedDate = currentTimestamp,
            requestStatus = RequestStatus.Open,
        )
    }

    /**
     * Checks whether a provided contact list is trivial, i.e. is null, empty or consists only of empty or blank strings
     * @param contactList the contact list to verify
     * @return true if the contact list is trivial, false otherwise
     */
    fun isContactListTrivial(contactList: List<String>?): Boolean {
        return contactList.isNullOrEmpty() || !contactList.any { it.isNotBlank() }
    }

    private fun isDataRequestAlreadyExisting(
        identifierValue: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
    ): Boolean {
        val requestingUserId = DatalandAuthentication.fromContext().userId
        val isAlreadyExisting = dataRequestRepository
            .existsByUserIdAndDatalandCompanyIdAndDataTypeNameAndReportingPeriod(
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
