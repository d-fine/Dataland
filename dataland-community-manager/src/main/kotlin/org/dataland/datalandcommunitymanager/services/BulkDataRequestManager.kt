package org.dataland.datalandcommunitymanager.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.DatasetDimensions
import org.dataland.datalandcommunitymanager.model.dataRequest.ResourceResponse
import org.dataland.datalandcommunitymanager.services.messaging.BulkDataRequestEmailMessageBuilder
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
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
    @Autowired private val emailMessageSender: BulkDataRequestEmailMessageBuilder,
    @Autowired private val utils: DataRequestProcessingUtils,
    @Autowired private val metaDataController: MetaDataControllerApi,
    @PersistenceContext private val entityManager: EntityManager,
    @Value("\${dataland.community-manager.proxy-primary-url}") private val proxyPrimaryUrl: String,
) {
    /**
     * Processes a bulk data request from a user
     * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
     * @return relevant info to the user as a response after posting a bulk data request
     */
    @Transactional
    fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
        assureValidityOfRequests(bulkDataRequest)
        val correlationId = UUID.randomUUID().toString()
        val logger = LoggerFactory.getLogger(javaClass)
        dataRequestLogger.logMessageForBulkDataRequest(correlationId)

        val (acceptedIdentifiersToCompanyIdAndName, rejectedIdentifiers) =
            utils.performIdentifierValidation(bulkDataRequest.companyIdentifiers.toList())

        val validRequestCombinations =
            getValidRequestCombinations(bulkDataRequest, acceptedIdentifiersToCompanyIdAndName)

        val existingDatasets =
            metaDataController.retrieveMetaDataOfActiveDatasets(
                basicDataDimensions = validRequestCombinations.map { it.toBasicDataDimensions() },
            )

        val acceptedRequestCombinations = getDimensionsWithoutRequests(validRequestCombinations, existingDatasets)

        if (DatalandAuthentication.fromContext() is DatalandJwtAuthentication) {
            sendBulkDataRequestInternalEmailMessage(
                bulkDataRequest, acceptedIdentifiersToCompanyIdAndName.values.toList(), correlationId,
            )
        } else {
            logger.info("Not Jwt-authenticated: No InternalEmailMessage sent.")
        }

        return createBulkDataRequests(
            acceptedRequestCombinations = acceptedRequestCombinations,
            acceptedIdentifiersToCompanyIdAndName = acceptedIdentifiersToCompanyIdAndName,
            existingDatasets = existingDatasets,
            rejectedIdentifiers = rejectedIdentifiers,
            notifyMeImmediately = bulkDataRequest.notifyMeImmediately,
            correlationId = correlationId,
        )
    }

    private fun getValidRequestCombinations(
        bulkDataRequest: BulkDataRequest,
        acceptedIdentifiersToCompanyIdAndName: Map<String, CompanyIdAndName>,
    ): List<DatasetDimensions> {
        val acceptedCompanyIdsSet: Set<String> =
            acceptedIdentifiersToCompanyIdAndName.values.map { it.companyId }.toSet()

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

            companyMappingEntry
                ?: throw IllegalArgumentException("Can't match: $companyId to a user provided company identifier.")

            ResourceResponse(
                userProvidedIdentifier = companyMappingEntry.key,
                companyName = companyMappingEntry.value.companyName,
                framework = metaData.dataType.toString(),
                reportingPeriod = metaData.reportingPeriod,
                resourceId = metaData.dataId,
                resourceUrl = metaData.ref,
            )
        }
    }

    private fun getDimensionsWithoutRequests(
        validRequestCombinations: List<DatasetDimensions>,
        existingDatasets: List<DataMetaInformation>,
    ): List<DatasetDimensions> {
        val dimensionsWithExistingRequests =
            existingDatasets
                .map {
                    DatasetDimensions(it.companyId, it.dataType, it.reportingPeriod)
                }.toSet()

        val allValidDimensions = validRequestCombinations.toSet()
        val dimensionsWithoutRequests = allValidDimensions - dimensionsWithExistingRequests
        return dimensionsWithoutRequests.toList()
    }

    private fun generateRequestCombinations(
        dataTypes: Set<DataTypeEnum>,
        reportingPeriods: Set<String>,
        datalandCompanyIds: Set<String>,
    ): List<DatasetDimensions> =
        datalandCompanyIds.flatMap { companyId ->
            dataTypes.flatMap { dataType ->
                reportingPeriods.map { period ->
                    DatasetDimensions(
                        companyId = companyId,
                        dataType = dataType,
                        reportingPeriod = period,
                    )
                }
            }
        }

    /**
     * Function to retrieve all active data requests for a user based on the provided data dimensions and the user ID.
     * For performance reasons tupel matching is used and requires a native query via entity manager. JPA does not support tupel matching.
     * @param dataDimensions List of data dimensions to filter the requests.
     * @param userId The ID of the user for whom to retrieve the active requests.
     */
    fun getActiveRequests(
        dataDimensions: List<DatasetDimensions>,
        userId: String,
    ): List<DataRequestEntity> {
        val formattedTuples =
            dataDimensions.joinToString(", ") {
                "('${it.companyId}', '${it.dataType}', '${it.reportingPeriod}')"
            }

        val queryToExecute =
            """WITH history AS (SELECT *, ROW_NUMBER()
                OVER (PARTITION BY data_request_id ORDER BY creation_timestamp DESC) AS num_row FROM request_status_history)
                SELECT d.* FROM data_requests d
                JOIN (SELECT * FROM history
                WHERE num_row = 1 AND request_status IN ('Open', 'Answered')) h
                ON d.data_request_id = h.data_request_id
                WHERE (d.dataland_company_id, d.data_type, d.reporting_period) IN ($formattedTuples)
                AND d.user_id = '$userId'"""

        return if (dataDimensions.isNotEmpty()) {
            val query = entityManager.createNativeQuery(queryToExecute, DataRequestEntity::class.java)
            JsonUtils.convertQueryResults(query.resultList)
        } else {
            emptyList()
        }
    }

    /**
     * Processes data requests to remove already existing data requests
     * @param validDataDimensions list of all valid data requests
     * @param userProvidedIdentifierToDatalandCompanyIdMapping mapping of user provided company identifiers to
     * dataland company ids and names
     * @return list of data requests that already exist
     */
    private fun processExistingDataRequests(
        validDataDimensions: MutableList<DatasetDimensions>,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
    ): List<ResourceResponse> {
        val userId = DatalandAuthentication.fromContext().userId
        val existingRequests = getActiveRequests(validDataDimensions, userId)
        val existingDataRequestsResponse =
            existingRequests.map {
                val (userProvidedCompanyId, companyName) =
                    extractUserProvidedIdAndName(
                        it.datalandCompanyId, userProvidedIdentifierToDatalandCompanyIdMapping,
                    )
                ResourceResponse(
                    userProvidedIdentifier = userProvidedCompanyId,
                    companyName = companyName,
                    framework = it.dataType,
                    reportingPeriod = it.reportingPeriod,
                    resourceId = it.dataRequestId,
                    resourceUrl = "https://$proxyPrimaryUrl/requests/${it.dataRequestId}",
                )
            }

        val existingDatasetDimensions =
            existingRequests.map { dimension ->
                DatasetDimensions(
                    companyId = dimension.datalandCompanyId,
                    dataType =
                        DataTypeEnum.decode(dimension.dataType)
                            ?: throw IllegalArgumentException("Invalid data type: ${dimension.dataType}"),
                    reportingPeriod = dimension.reportingPeriod,
                )
            }
        validDataDimensions.removeAll(existingDatasetDimensions)
        return existingDataRequestsResponse
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

    /** Stores the data requests from requestsToProcess and provides a feedback list including the user-provided company identifiers.
     * @param dimensionsToProcess A list of data dimensions that requests are required for.
     * @param userProvidedIdentifierToDatalandCompanyIdMapping A mapping that stores the user-provided identifiers and
     * associated dataland company ids and name.
     * @return A list of ResourceResponse objects that documents the stored requests and contains all necessary information
     * to display in the frontend.
     */
    private fun storeDataRequests(
        dimensionsToProcess: List<DatasetDimensions>,
        userProvidedIdentifierToDatalandCompanyIdMapping: Map<String, CompanyIdAndName>,
        notifyMeImmediately: Boolean,
    ): List<ResourceResponse> {
        val userId = DatalandAuthentication.fromContext().userId
        val acceptedDataRequests = mutableListOf<ResourceResponse>()

        dimensionsToProcess.forEach {
            val (userProvidedCompanyId, companyName) =
                extractUserProvidedIdAndName(
                    it.companyId,
                    userProvidedIdentifierToDatalandCompanyIdMapping,
                )

            val storedRequest =
                utils.storeDataRequestEntityAsOpen(
                    userId = userId,
                    datalandCompanyId = it.companyId,
                    dataType = it.dataType,
                    notifyMeImmediately = notifyMeImmediately,
                    reportingPeriod = it.reportingPeriod,
                )

            acceptedDataRequests.add(
                ResourceResponse(
                    userProvidedIdentifier = userProvidedCompanyId,
                    companyName = companyName,
                    framework = it.dataType.toString(),
                    reportingPeriod = it.reportingPeriod,
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
        acceptedRequestCombinations: List<DatasetDimensions>,
        acceptedIdentifiersToCompanyIdAndName: Map<String, CompanyIdAndName>,
        existingDatasets: List<DataMetaInformation>,
        rejectedIdentifiers: List<String>,
        notifyMeImmediately: Boolean,
        correlationId: String,
    ): BulkDataRequestResponse {
        val requestsToProcess = acceptedRequestCombinations.toMutableList()
        val existingNonFinalRequests =
            processExistingDataRequests(requestsToProcess, acceptedIdentifiersToCompanyIdAndName)
        val acceptedRequests =
            storeDataRequests(
                requestsToProcess,
                acceptedIdentifiersToCompanyIdAndName,
                notifyMeImmediately,
            )
        val existingDatasetsResponse =
            getAlreadyExistingDatasetsResponse(existingDatasets, acceptedIdentifiersToCompanyIdAndName)

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
        emailMessageSender.buildBulkDataRequestInternalMessageAndSendCEMessage(
            bulkDataRequest,
            acceptedDatalandCompanyIdsAndNames,
            correlationId,
        )
        dataRequestLogger.logMessageForSendBulkDataRequestEmailMessage(correlationId)
    }
}
