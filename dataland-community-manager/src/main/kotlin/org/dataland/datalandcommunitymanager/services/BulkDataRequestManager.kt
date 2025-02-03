package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformationRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.ResourceResponse
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

    private fun getAlreadyExistingDatasetsResponse(
        metaDataList: List<DataMetaInformation>,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
    ): List<ResourceResponse> {
        if (metaDataList.isEmpty()) {
            return emptyList()
        }

        return metaDataList.map { metaData ->
            val companyId = metaData.companyId

            val companyMappingEntry =
                userProvidedIdentifierToDatalandCompanyIdMapping.entries
                    .firstOrNull { it.value.companyId == companyId }

            companyMappingEntry ?: throw IllegalArgumentException("Can't match: $companyId to a user provided company identifier.")

            ResourceResponse(
                userProvidedIdentifier = companyMappingEntry.key,
                companyName = companyMappingEntry.value.companyName,
                framework = metaData.dataType.toString(),
                reportingPeriod = metaData.reportingPeriod,
                resourceId = metaData.dataId,
                resourceUrl = metaData.url,
            )
        }
    }

    private fun getAcceptedRequestCombinations(
        validRequestCombinations: List<DataMetaInformationRequest>,
        existingDatasets: List<DataMetaInformation>,
    ): List<ValidSingleDataRequest> {
        // rewrite once data dimension object is available
        val existingCriteriaSet =
            existingDatasets
                .map {
                    Triple(it.companyId, it.dataType, it.reportingPeriod)
                }.toSet()

        val nonExistingRequests =
            validRequestCombinations
                .filter { request ->
                    val requestCriteria = Triple(request.companyId, request.dataType, request.reportingPeriod)

                    require(request.companyId != null && request.dataType != null && request.reportingPeriod != null) {
                        "Request cannot have null values: $request"
                    }
                    requestCriteria !in existingCriteriaSet
                }.map { request ->
                    ValidSingleDataRequest(
                        companyIdentifier = request.companyId!!,
                        dataType = request.dataType!!,
                        reportingPeriod = request.reportingPeriod!!,
                    )
                }

        // Check why this cast is here
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
                    )
                }
            }
        }

    /**
     * Processes data requests to remove already existing data requests
     * @param validSingleDataRequests list of all valid data requests
     * @param userProvidedIdentifierToDatalandCompanyIdMapping mapping of user provided company identifiers to
     * dataland company ids and names
     * @return list of data requests that already exist
     */
    private fun processExistingDataRequests(
        validSingleDataRequests: MutableList<ValidSingleDataRequest>,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
    ): List<ResourceResponse> {
        val existingDataRequests = mutableListOf<ResourceResponse>()

        for (request in validSingleDataRequests) {
            val existingRequestId =
                utils.getRequestIdForDataRequestWithNonFinalStatus(
                    request.companyIdentifier,
                    request.dataType,
                    request.reportingPeriod,
                )
            if (existingRequestId != null) {
                val (userProvidedCompanyId, companyName) =
                    extractUserProvidedIdAndName(
                        request.companyIdentifier,
                        userProvidedIdentifierToDatalandCompanyIdMapping,
                    )

                existingDataRequests.add(
                    ResourceResponse(
                        userProvidedIdentifier = userProvidedCompanyId,
                        companyName = companyName,
                        framework = request.dataType.toString(),
                        reportingPeriod = request.reportingPeriod,
                        resourceId = existingRequestId,
                        resourceUrl = "https://$proxyPrimaryUrl/requests/$existingRequestId",
                    ),
                )
                validSingleDataRequests.remove(request)
            }
        }
        return existingDataRequests
    }

    private fun extractUserProvidedIdAndName(
        companyId: String,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
    ): Pair<String, String> {
        val entry =
            userProvidedIdentifierToDatalandCompanyIdMapping.entries
                .find { it.value.companyId == companyId }
                ?: throw IllegalArgumentException("Entry not found for companyId: $companyId")

        return Pair(entry.key, entry.value.companyName)
    }

    /** Stores the data requests from requestsToProcess and provides a feedback list
     * including the user-provided company identifiers.
     *
     * @param requestsToProcess The requests to store.
     * @param userProvidedIdentifierToDatalandCompanyIdMapping A mapping that stores
     * the user-provided identifiers and associated dataland company ids.
     *
     * @return A list of ResourceResponse objects that documents the stored requests
     * and contains all necessary information to display in the frontend.
     */
    private fun storeDataRequests(
        requestsToProcess: List<ValidSingleDataRequest>,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
    ): List<ResourceResponse> {
        val acceptedDataRequests = mutableListOf<ResourceResponse>()

        for (request in requestsToProcess) {
            val (userProvidedCompanyId, companyName) =
                extractUserProvidedIdAndName(
                    request.companyIdentifier,
                    userProvidedIdentifierToDatalandCompanyIdMapping,
                )

            val storedRequest =
                utils.storeDataRequestEntityAsOpen(
                    request.companyIdentifier,
                    request.dataType,
                    request.reportingPeriod,
                )

            acceptedDataRequests.add(
                ResourceResponse(
                    userProvidedIdentifier = userProvidedCompanyId,
                    companyName = companyName,
                    framework = request.dataType.toString(),
                    reportingPeriod = request.reportingPeriod,
                    resourceId = storedRequest.dataRequestId,
                    resourceUrl = "https://$proxyPrimaryUrl/requests/${storedRequest.dataRequestId}",
                ),
            )
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

    private fun createBulkDataRequests(
        acceptedRequestCombinations: List<ValidSingleDataRequest>,
        acceptedIdentifiersToCompanyIdAndName: Map<String, CompanyIdAndName>,
        existingDatasets: List<DataMetaInformation>,
        rejectedIdentifiers: List<String>,
        correlationId: String,
    ): BulkDataRequestResponse {
        val requestsToProcess = acceptedRequestCombinations.toMutableList()
        val existingNonFinalRequests = processExistingDataRequests(requestsToProcess, acceptedIdentifiersToCompanyIdAndName)
        val acceptedRequests = storeDataRequests(requestsToProcess, acceptedIdentifiersToCompanyIdAndName)
        val existingDatasetsResponse = getAlreadyExistingDatasetsResponse(existingDatasets, acceptedIdentifiersToCompanyIdAndName)

        val bulkRequestResponse =
            BulkDataRequestResponse(
                acceptedDataRequests = acceptedRequests,
                alreadyExistingNonFinalRequests = existingNonFinalRequests,
                alreadyExistingDatasets = existingDatasetsResponse,
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
