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
import org.dataland.datalandcommunitymanager.model.dataRequest.ValidSingleDataRequest
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.slf4j.LoggerFactory
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
    private val bulkDataRequestLogger = LoggerFactory.getLogger(BulkDataRequestManager::class.java)

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

        bulkDataRequestLogger.info("Number of accepted company ids: ${acceptedCompanyIdsSet.size}")
        bulkDataRequestLogger.info("Number of rejected company ids: ${rejectedIdentifiers.size}")

        val validRequestsList =
            getInformationRequestList(
                dataTypes = bulkDataRequest.dataTypes,
                reportingPeriods = bulkDataRequest.reportingPeriods,
                datalandCompanyIds = acceptedCompanyIdsSet,
            )

        bulkDataRequestLogger.info(
            "Number of total single requests considered: ${validRequestsList.size}",
        )
        val alreadyExistingDatasets = metaDataController.postListOfDataMetaInfoRequests(validRequestsList)
        bulkDataRequestLogger.info(
            "Number of datasets that already exist: ${alreadyExistingDatasets.size}",
        )
        val alreadyExistingDataSetsResponse =
            convertToAlreadyExistingDataSetsResponse(alreadyExistingDatasets, acceptedUserIdToCompanyIdAndName)
        val nonExistingDatasets = getNonexistingDataSets(validRequestsList, alreadyExistingDatasets)
        bulkDataRequestLogger.info(
            "Number of requests that will be newly created: ${nonExistingDatasets.size}",
        )

        val acceptedDataRequestsResponse =
            storeDataRequests(nonExistingDatasets, acceptedUserIdToCompanyIdAndName)

        bulkDataRequestLogger.info(
            "Number of requests that have been newly created: ${acceptedDataRequestsResponse.size}",
        )

        sendBulkDataRequestInternalEmailMessage(
            bulkDataRequest, acceptedUserIdToCompanyIdAndName.values.toList(), correlationId,
        )
        bulkDataRequestLogger.info(
            "Returning Accepted: $acceptedDataRequestsResponse",
        )

        bulkDataRequestLogger.info(
            "Returning Already Existing: $alreadyExistingDataSetsResponse",
        )

        bulkDataRequestLogger.info(
            "Returning Rejected Identifiers: $rejectedIdentifiers",
        )

        return buildResponseForBulkDataRequest(acceptedDataRequestsResponse, alreadyExistingDataSetsResponse, rejectedIdentifiers)
    }

    private fun convertToAlreadyExistingDataSetsResponse(
        metaDataList: List<DataMetaInformation>,
        userProvidedIdentifierToDatalandCompanyIdMapping: MutableMap<String, CompanyIdAndName>,
    ): List<AlreadyExistingDataSetsResponse> {
        val alreadyExistingDataSetsResponse = mutableListOf<AlreadyExistingDataSetsResponse>()
        if (metaDataList.isNotEmpty()) {
            for (metaData in metaDataList) {
                val companyId = metaData.companyId
                val entry =
                    userProvidedIdentifierToDatalandCompanyIdMapping.entries
                        .find { it.value.companyId == companyId }
                val userProvidedCompanyId = entry?.key
                val companyName = entry?.value?.companyName

                if (userProvidedCompanyId == null || companyName == null) {
                    throw IllegalArgumentException("Entry not found for companyId: $companyId")
                }

                val singleExistingDataSetsResponse =
                    AlreadyExistingDataSetsResponse(
                        userProvidedCompanyId = userProvidedCompanyId,
                        companyName = companyName,
                        framework = metaData.dataType.toString(),
                        reportingPeriod = metaData.reportingPeriod,
                        datasetId = metaData.dataId,
                        datasetUrl = metaData.url,
                    )
                alreadyExistingDataSetsResponse.add(singleExistingDataSetsResponse)
            }
        }
        return alreadyExistingDataSetsResponse
    }

    private fun getNonexistingDataSets(
        allRequestsList: List<DataMetaInformationRequest>,
        existingDatasets: List<DataMetaInformation>,
    ): List<ValidSingleDataRequest> {
        val existingCriteriaSet =
            existingDatasets
                .map {
                    Triple(it.companyId, it.dataType, it.reportingPeriod)
                }.toSet()

        val nonExistingRequests =
            allRequestsList
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

        return if (nonExistingRequests.isNotEmpty()) nonExistingRequests else emptyList()
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
        validSingleDataRequests: List<ValidSingleDataRequest>,
        userProvidedIdentifierToDatalandCompanyIdMapping: MutableMap<String, CompanyIdAndName>,
    ): List<AcceptedDataRequestsResponse> {
        val acceptedDataRequests = mutableListOf<AcceptedDataRequestsResponse>()

        for (validSingleRequest in validSingleDataRequests) {
            if (!utils.existsDataRequestWithNonFinalStatus(
                    validSingleRequest.companyIdentifier,
                    validSingleRequest.dataType,
                    validSingleRequest.reportingPeriod,
                )
            ) {
                val response =
                    utils.storeDataRequestEntityAsOpen(
                        validSingleRequest.companyIdentifier,
                        validSingleRequest.dataType,
                        validSingleRequest.reportingPeriod,
                    )

                val companyId = validSingleRequest.companyIdentifier
                val entry =
                    userProvidedIdentifierToDatalandCompanyIdMapping.entries
                        .find { it.value.companyId == companyId }
                val userProvidedCompanyId = entry?.key
                val companyName = entry?.value?.companyName

                if (userProvidedCompanyId == null || companyName == null) {
                    throw IllegalArgumentException("Entry not found for companyId: $companyId")
                }
                val singleAcceptedDataRequest =
                    AcceptedDataRequestsResponse(
                        userProvidedCompanyId = userProvidedCompanyId,
                        companyName = companyName,
                        framework = validSingleRequest.dataType.toString(),
                        reportingPeriod = validSingleRequest.reportingPeriod,
                        requestId = response.dataRequestId,
                        requestUrl = "https://$proxyPrimaryUrl/requests/" + response.dataRequestId,
                    )
                acceptedDataRequests.add(singleAcceptedDataRequest)
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

    private fun buildResponseForBulkDataRequest(
        acceptedRequests: List<AcceptedDataRequestsResponse>,
        existingDataSets: List<AlreadyExistingDataSetsResponse>,
        rejectedIdentifiers: List<String>,
    ): BulkDataRequestResponse {
        val bulkRequestRespone =
            BulkDataRequestResponse(
                acceptedDataRequests = acceptedRequests,
                alreadyExistingDataRequests = existingDataSets,
                rejectedCompanyIdentifiers = rejectedIdentifiers,
            )

        bulkDataRequestLogger.info(
            "Bulk request response: $bulkRequestRespone",
        )

        return bulkRequestRespone
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
