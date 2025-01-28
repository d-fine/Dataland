package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformationRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AcceptedDataRequestsResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.AlreadyExistingDataSetsResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Implementation of a request manager service for all operations concerning the processing of bulk data requests
 */
@Service("BulkDataRequestManager")
class BulkDataRequestManager(
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val emailMessageSender: BulkDataRequestEmailMessageSender,
    @Autowired private val utils: DataRequestProcessingUtils,
    @Autowired private val metaDataController: MetaDataControllerApi,
) {
    /**
     * Processes a bulk data request from a user
     * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
     * @return relevant info to the user as a response after posting a bulk data request
     */
    @Transactional
    fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
        utils.throwExceptionIfNotJwtAuth()
        assureValidityOfRequests(bulkDataRequest)
        val correlationId = UUID.randomUUID().toString()
        dataRequestLogger.logMessageForBulkDataRequest(correlationId)

        val rejectedIdentifiers = mutableListOf<String>()
        val acceptedUserIdToCompanyIdAndName = mutableMapOf<String, CompanyIdAndName>()

        for (userProvidedIdentifier in bulkDataRequest.companyIdentifiers) {
            val datalandCompanyIdAndName =
                utils.getDatalandCompanyIdAndNameForIdentifierValue(userProvidedIdentifier, returnOnlyUnique = true)
            if (datalandCompanyIdAndName == null) {
                rejectedIdentifiers.add(userProvidedIdentifier)
                continue
            }
            acceptedUserIdToCompanyIdAndName[userProvidedIdentifier] = datalandCompanyIdAndName
        }

        val acceptedCompanyIdsSet: Set<String> = acceptedUserIdToCompanyIdAndName.values.map { it.companyId }.toSet()
        val acceptedUserInputIdentifiers: Set<String> = acceptedUserIdToCompanyIdAndName.keys

        val validRequestsList =
            getInformationRequestList(
                dataTypes = bulkDataRequest.dataTypes,
                reportingPeriods = bulkDataRequest.reportingPeriods,
                datalandCompanyIds = acceptedCompanyIdsSet,
            )

        val alreadyExistingDatasets = metaDataController.postListOfDataMetaInfoRequests(validRequestsList)

        val alreadyExistingDataSetsResponse =
            convertToAlreadyExistingDataSetsResponse(alreadyExistingDatasets, acceptedUserIdToCompanyIdAndName)

        val nonExistingDatasets = getNonexistingDataSets(validRequestsList, alreadyExistingDatasets)

        val acceptedDataRequestsResponse =
            storeDataRequests(dataMetaInformationRequest = nonExistingDatasets, acceptedUserIdToCompanyIdAndName)

        if (acceptedUserInputIdentifiers.isEmpty()) throwInvalidInputApiExceptionBecauseAllIdentifiersRejected()
        sendBulkDataRequestInternalEmailMessage(
            bulkDataRequest, acceptedUserIdToCompanyIdAndName.values.toList(), correlationId,
        )
        return buildResponseForBulkDataRequest(acceptedDataRequestsResponse, alreadyExistingDataSetsResponse, rejectedIdentifiers)
    }

    private fun convertToAlreadyExistingDataSetsResponse(
        metaDataList: List<DataMetaInformation>,
        userProvidedIdentifierToDatalandCompanyIdMapping: MutableMap<String, CompanyIdAndName>,
    ): List<AlreadyExistingDataSetsResponse> {
        val alreadyExistingDataSetsResponse = mutableListOf<AlreadyExistingDataSetsResponse>()
        for (metaData in metaDataList) {
            val companyId = metaData.companyId
            val entry =
                userProvidedIdentifierToDatalandCompanyIdMapping.entries
                    .find { it.value.companyId == companyId }
            val userProvidedCompanyId = entry?.key
            val companyName = entry?.value?.companyName
            if (userProvidedCompanyId != null && companyName != null) {
                val element =
                    AlreadyExistingDataSetsResponse(
                        userProvidedCompanyId = userProvidedCompanyId,
                        companyName = companyName,
                        framework = metaData.dataType.toString(),
                        reportingPeriod = metaData.reportingPeriod,
                        datasetId = metaData.dataId,
                        datasetUrl = metaData.url,
                    )
                alreadyExistingDataSetsResponse.add(element)
            }
        }

        return alreadyExistingDataSetsResponse
    }

    private fun getNonexistingDataSets(
        informationRequestList: List<DataMetaInformationRequest>,
        alreadyExistingDatasets: List<DataMetaInformation>,
    ): List<DataMetaInformation> =
        alreadyExistingDatasets.filter { existingData ->
            val existingCriteria = Triple(existingData.companyId, existingData.dataType, existingData.reportingPeriod)

            val doesExist =
                informationRequestList.any { request ->
                    val requestCriteria = Triple(request.companyId, request.dataType, request.reportingPeriod)
                    existingCriteria == requestCriteria
                }
            !doesExist
        }

    private fun getInformationRequestList(
        dataTypes: Set<DataTypeEnum>,
        reportingPeriods: Set<String>,
        datalandCompanyIds: Set<String>,
    ): List<DataMetaInformationRequest> {
        val dataMetaInformationRequests = mutableListOf<DataMetaInformationRequest>()

        for (companyId in datalandCompanyIds) {
            for (dataType in dataTypes) {
                for (period in reportingPeriods) {
                    val request =
                        DataMetaInformationRequest(
                            companyId = companyId,
                            dataType = dataType,
                            reportingPeriod = period,
                            showOnlyActive = true,
                            uploaderUserIds = null,
                            qaStatus = null,
                        )
                    dataMetaInformationRequests.add(request)
                }
            }
        }
        return dataMetaInformationRequests
    }

    private fun storeDataRequests(
        dataMetaInformationRequest: List<DataMetaInformation>,
        userProvidedIdentifierToDatalandCompanyIdMapping: MutableMap<String, CompanyIdAndName>,
    ): List<AcceptedDataRequestsResponse> {
        val acceptedDataRequests = mutableListOf<AcceptedDataRequestsResponse>()

        for (dataMetaInformation in dataMetaInformationRequest) {
            if (!utils.existsDataRequestWithNonFinalStatus(
                    dataMetaInformation.companyId,
                    dataMetaInformation.dataType,
                    dataMetaInformation.reportingPeriod,
                )
            ) {
                val response =
                    utils.storeDataRequestEntityAsOpen(
                        dataMetaInformation.companyId,
                        dataMetaInformation.dataType,
                        dataMetaInformation.reportingPeriod,
                    )

                val companyId = dataMetaInformation.companyId
                val entry =
                    userProvidedIdentifierToDatalandCompanyIdMapping.entries
                        .find { it.value.companyId == companyId }
                val userProvidedId = entry?.key
                val companyName = entry?.value?.companyName

                if (userProvidedId != null && companyName != null) {
                    val element =
                        AcceptedDataRequestsResponse(
                            userProvidedCompanyId = userProvidedId,
                            companyName = companyName,
                            framework = dataMetaInformation.dataType.toString(),
                            reportingPeriod = dataMetaInformation.reportingPeriod,
                            requestId = response.dataRequestId,
                            requestUrl = "https://www.dataland.com/requests/" + response.dataRequestId,
                        )
                    acceptedDataRequests.add(element)
                }
            }
        }
        return acceptedDataRequests
    }

    private fun errorMessageForEmptyInputConfigurations(
        identifiers: Set<String>,
        frameworks: Set<DataTypeEnum>,
        reportingPeriods: Set<String>,
    ): String =
        when {
            identifiers.isEmpty() && frameworks.isEmpty() && reportingPeriods.isEmpty() ->
                "All " +
                    "provided lists are empty."

            identifiers.isEmpty() && frameworks.isEmpty() ->
                "The lists of company identifiers and " +
                    "frameworks are empty."

            identifiers.isEmpty() && reportingPeriods.isEmpty() ->
                "The lists of company identifiers and " +
                    "reporting periods are empty."

            frameworks.isEmpty() && reportingPeriods.isEmpty() ->
                "The lists of frameworks and reporting " +
                    "periods are empty."

            identifiers.isEmpty() -> "The list of company identifiers is empty."
            frameworks.isEmpty() -> "The list of frameworks is empty."
            else -> "The list of reporting periods is empty."
        }

    private fun assureValidityOfRequests(bulkDataRequest: BulkDataRequest) {
        val identifiers = bulkDataRequest.companyIdentifiers
        val frameworks = bulkDataRequest.dataTypes
        val reportingPeriods = bulkDataRequest.reportingPeriods
        if (identifiers.isEmpty() || frameworks.isEmpty() || reportingPeriods.isEmpty()) {
            val errorMessage =
                errorMessageForEmptyInputConfigurations(
                    identifiers, frameworks, reportingPeriods,
                )
            throw InvalidInputApiException(
                "No empty lists are allowed as input for bulk data request.",
                errorMessage,
            )
        }
    }

    private fun buildResponseMessageForBulkDataRequest(
        totalNumberOfRequestedCompanyIdentifiers: Int,
        numberOfRejectedCompanyIdentifiers: Int,
    ): String =
        when (numberOfRejectedCompanyIdentifiers) {
            0 -> "All of your $totalNumberOfRequestedCompanyIdentifiers distinct company identifiers were accepted."
            1 ->
                "One of your $totalNumberOfRequestedCompanyIdentifiers distinct company identifiers was rejected " +
                    "because it could not be uniquely matched with an existing company on Dataland."

            else ->
                "$numberOfRejectedCompanyIdentifiers of your $totalNumberOfRequestedCompanyIdentifiers distinct " +
                    "company identifiers were rejected because they could not be uniquely matched with existing " +
                    "companies on Dataland."
        }

    private fun buildResponseForBulkDataRequest(
        acceptedDataRequestsResponse: List<AcceptedDataRequestsResponse>,
        alreadyExistingDataSetsResponse: List<AlreadyExistingDataSetsResponse>,
        rejectedIdentifiers: List<String>,
    ): BulkDataRequestResponse {
        val message =
            buildResponseMessageForBulkDataRequest(
                acceptedDataRequestsResponse.size,
                rejectedIdentifiers.size,
            )
        val bulkDataRequestResponse =
            BulkDataRequestResponse(
                message = message,
                acceptedDataRequests = acceptedDataRequestsResponse,
                alreadyExistingDataRequests = alreadyExistingDataSetsResponse,
                rejectedCompanyIdentifiers = rejectedIdentifiers,
            )
        return bulkDataRequestResponse
    }

    private fun sendBulkDataRequestInternalEmailMessage(
        bulkDataRequest: BulkDataRequest,
        acceptedDatalandCompanyIdsAndNames: List<CompanyIdAndName>,
        correlationId: String,
    ) {
        emailMessageSender.sendBulkDataRequestInternalMessage(
            bulkDataRequest,
            acceptedDatalandCompanyIdsAndNames,
            correlationId,
        )
        dataRequestLogger.logMessageForSendBulkDataRequestEmailMessage(correlationId)
    }

    private fun throwInvalidInputApiExceptionBecauseAllIdentifiersRejected() {
        val summary = "All provided company identifiers are not unique or could not be recognized."
        val message =
            "The company identifiers you provided could not be uniquely matched with an existing " +
                "company on dataland"
        throw InvalidInputApiException(
            summary,
            message,
        )
    }
}
