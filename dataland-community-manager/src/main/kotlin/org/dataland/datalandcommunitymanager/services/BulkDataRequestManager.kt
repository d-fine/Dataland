package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformationRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.DataSetsResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.ValidSingleDataRequest
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
    @Value("\${dataland.community-manager.proxy-primary-url}") private val proxyPrimaryUrl: String,
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
        val acceptedIdentifiersToCompanyIdAndName = mutableMapOf<String, CompanyIdAndName>()

        for (userProvidedIdentifier in bulkDataRequest.companyIdentifiers) {
            val datalandCompanyIdAndName =
                utils.getDatalandCompanyIdAndNameForIdentifierValue(userProvidedIdentifier, returnOnlyUnique = true)
            if (datalandCompanyIdAndName == null) {
                rejectedIdentifiers.add(userProvidedIdentifier)
                continue
            }
            acceptedIdentifiersToCompanyIdAndName[userProvidedIdentifier] = datalandCompanyIdAndName
        }

        val validRequestCombinations = getValidRequestCombinations(bulkDataRequest, acceptedIdentifiersToCompanyIdAndName)

        val existingDatasets = metaDataController.postListOfDataMetaInfoRequests(validRequestCombinations)

        val acceptedRequestCombinations = getAcceptedRequestCombinations(validRequestCombinations, existingDatasets)

        sendBulkDataRequestInternalEmailMessage(
            bulkDataRequest, acceptedIdentifiersToCompanyIdAndName.values.toList(), correlationId,
        )

        return createBulkDataRequests(
            acceptedRequestCombinations = acceptedRequestCombinations,
            acceptedIdentifiersToCompanyIdAndName = acceptedIdentifiersToCompanyIdAndName,
            existingDatasets = existingDatasets,
            rejectedIdentifiers = rejectedIdentifiers,
            correlationId = correlationId,
        )
    }

    private fun getValidRequestCombinations(
        bulkDataRequest: BulkDataRequest,
        acceptedIdentifiersToCompanyIdAndName: Map<String, CompanyIdAndName>,
    ): List<DataMetaInformationRequest> {
        val acceptedCompanyIdsSet: Set<String> = acceptedIdentifiersToCompanyIdAndName.values.map { it.companyId }.toSet()

        return generateRequestCombinations(
            dataTypes = bulkDataRequest.dataTypes,
            reportingPeriods = bulkDataRequest.reportingPeriods,
            datalandCompanyIds = acceptedCompanyIdsSet,
        )
    }

    private fun getAlreadyExistingDataSetsResponse(
        metaDataList: List<DataMetaInformation>,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
    ): List<DataSetsResponse> {
        if (metaDataList.isEmpty()) {
            return emptyList()
        }

        return metaDataList.map { metaData ->
            val companyId = metaData.companyId

            val companyMappingEntry =
                userProvidedIdentifierToDatalandCompanyIdMapping.entries
                    .firstOrNull { it.value.companyId == companyId }

            companyMappingEntry ?: throw IllegalArgumentException("Can't match: $companyId to a user provided company identifier.")

            DataSetsResponse(
                userProvidedCompanyId = companyMappingEntry.key,
                companyName = companyMappingEntry.value.companyName,
                framework = metaData.dataType.toString(),
                reportingPeriod = metaData.reportingPeriod,
                datasetId = metaData.dataId,
                datasetUrl = metaData.url,
            )
        }
    }

    private fun getAcceptedRequestCombinations(
        validRequestCombinations: List<DataMetaInformationRequest>,
        existingDatasets: List<DataMetaInformation>,
    ): List<ValidSingleDataRequest> {
        val existingCriteriaSet =
            existingDatasets
                .map {
                    Triple(it.companyId, it.dataType, it.reportingPeriod)
                }.toSet()

        val nonExistingRequests =
            validRequestCombinations
                .filter { request ->
                    val requestCriteria = Triple(request.companyId, request.dataType, request.reportingPeriod)

                    if (request.companyId == null || request.dataType == null || request.reportingPeriod == null) {
                        throw IllegalArgumentException("Request cannot have null values: $request")
                    }
                    requestCriteria !in existingCriteriaSet
                }.map { request ->
                    ValidSingleDataRequest(
                        companyIdentifier = request.companyId!!,
                        dataType = request.dataType!!,
                        reportingPeriod = request.reportingPeriod!!,
                    )
                }

        return nonExistingRequests.ifEmpty { emptyList() }
    }

    private fun generateRequestCombinations(
        dataTypes: Set<DataTypeEnum>,
        reportingPeriods: Set<String>,
        datalandCompanyIds: Set<String>,
    ): List<DataMetaInformationRequest> =
        datalandCompanyIds.flatMap { companyId ->
            dataTypes.flatMap { dataType ->
                reportingPeriods.map { period ->
                    DataMetaInformationRequest(
                        companyId = companyId,
                        dataType = dataType,
                        reportingPeriod = period,
                        showOnlyActive = true,
                        uploaderUserIds = null,
                        qaStatus = null,
                    )
                }
            }
        }

    private fun storeDataRequests(
        validSingleDataRequests: List<ValidSingleDataRequest>,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
    ): Pair<List<DataRequestResponse>, List<DataRequestResponse>> {
        val acceptedDataRequests = mutableListOf<DataRequestResponse>()
        val userNonFinalRequests = mutableListOf<DataRequestResponse>()

        for (request in validSingleDataRequests) {
            val existingRequestId =
                utils.getRequestIdForDataRequestWithNonFinalStatus(
                    request.companyIdentifier,
                    request.dataType,
                    request.reportingPeriod,
                )

            val companyId = request.companyIdentifier
            val entry =
                userProvidedIdentifierToDatalandCompanyIdMapping.entries
                    .find { it.value.companyId == companyId }
                    ?: throw IllegalArgumentException("Entry not found for companyId: $companyId")

            val userProvidedCompanyId = entry.key
            val companyName = entry.value.companyName

            if (existingRequestId == null) {
                val storedRequest =
                    utils.storeDataRequestEntityAsOpen(
                        request.companyIdentifier,
                        request.dataType,
                        request.reportingPeriod,
                    )

                val singleAcceptedDataRequest =
                    DataRequestResponse(
                        userProvidedCompanyId = userProvidedCompanyId,
                        companyName = companyName,
                        framework = request.dataType.toString(),
                        reportingPeriod = request.reportingPeriod,
                        requestId = storedRequest.dataRequestId,
                        requestUrl = "https://$proxyPrimaryUrl/requests/" + storedRequest.dataRequestId,
                    )
                acceptedDataRequests.add(singleAcceptedDataRequest)
            } else {
                val userNonFinalRequest =
                    DataRequestResponse(
                        userProvidedCompanyId = userProvidedCompanyId,
                        companyName = companyName,
                        framework = request.dataType.toString(),
                        reportingPeriod = request.reportingPeriod,
                        requestId = existingRequestId,
                        requestUrl = "https://$proxyPrimaryUrl/requests/" + existingRequestId,
                    )
                userNonFinalRequests.add(userNonFinalRequest)
            }
        }
        return acceptedDataRequests to userNonFinalRequests
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

    private fun createBulkDataRequests(
        acceptedRequestCombinations: List<ValidSingleDataRequest>,
        acceptedIdentifiersToCompanyIdAndName: Map<String, CompanyIdAndName>,
        existingDatasets: List<DataMetaInformation>,
        rejectedIdentifiers: List<String>,
        correlationId: String,
    ): BulkDataRequestResponse {
        val requestResults =
            storeDataRequests(acceptedRequestCombinations, acceptedIdentifiersToCompanyIdAndName)

        val acceptedRequests = requestResults.first
        val existingNonFinalRequests = requestResults.second

        val existingDataSets =
            getAlreadyExistingDataSetsResponse(existingDatasets, acceptedIdentifiersToCompanyIdAndName)

        val bulkRequestResponse =
            BulkDataRequestResponse(
                acceptedDataRequests = acceptedRequests,
                alreadyExistingNonFinalRequests = existingNonFinalRequests,
                alreadyExistingDataSets = existingDataSets,
                rejectedCompanyIdentifiers = rejectedIdentifiers,
            )

        dataRequestLogger.logBulkDataOverwiew(bulkRequestResponse, correlationId)

        return bulkRequestResponse
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
}
