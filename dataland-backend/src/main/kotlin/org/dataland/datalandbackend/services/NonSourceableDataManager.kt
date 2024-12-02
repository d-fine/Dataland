package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfoResponse
import org.dataland.datalandbackend.repositories.NonSourceableDataRepository
import org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * A service class for managing information about the sourceabilty of datasets.
 */
@Service("NonSourceableDataManager")
class NonSourceableDataManager(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val nonSourceableDataRepository: NonSourceableDataRepository,
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * The method stores a non-sourceable dataset in the nonSourceableDataRepository
     * @param nonSourceableInfo the of the dataset
     */
    @Transactional
    fun storeNonSourceableData(nonSourceableInfo: NonSourceableInfo): NonSourceableInfo? {
        val creationTime = Instant.now().toEpochMilli()
        val userId = DatalandAuthentication.fromContext().userId
        val nonSourceableEntity =
            NonSourceableEntity(
                eventId = null,
                companyId = nonSourceableInfo.companyId,
                dataType = nonSourceableInfo.dataType,
                reportingPeriod = nonSourceableInfo.reportingPeriod,
                isNonSourceable = nonSourceableInfo.isNonSourceable,
                reason = nonSourceableInfo.reason,
                creationTime = creationTime,
                userId = userId,
            )
        return nonSourceableDataRepository.save(nonSourceableEntity).toApiModel()
    }

    /**
     * Processes a request to store information about a dataset being labeled as non-sourceable.
     * This includes verifying the existence of the company, checking if the dataset already exists,
     * storing the non-sourceable data, and sending a corresponding message to a message queue.
     * @param nonSourceableInfo the NonSourceableInfo of the dataset
     */
    fun processSourceabilityDataStorageRequest(nonSourceableInfo: NonSourceableInfo) {
        val correlationId = generateCorrelationId(nonSourceableInfo.companyId, null)
        companyQueryManager.verifyCompanyIdExists(nonSourceableInfo.companyId)
        val dataMetaInfo =
            dataMetaInformationManager.searchDataMetaInfo(
                nonSourceableInfo.companyId, nonSourceableInfo.dataType,
                false, nonSourceableInfo.reportingPeriod, null, null,
            )
        if (dataMetaInfo.isNotEmpty()) {
            logger.info("Creating a NonSourceableEntity failed because the dataset exists (correlationId: $correlationId)")
            return
        }
        storeNonSourceableData(nonSourceableInfo)
        logger.info("NonSourceableEntity has been saved to data base during process with correlationId $correlationId")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(nonSourceableInfo),
            type = MessageType.DATA_NONSOURCEABLE,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            routingKey = RoutingKeyNames.DATA_NONSOURCEABLE,
        )
    }

    /**
     * The method retrieves non-sourceable datasets by given filters.
     * @param companyId if not empty, filters the requested information by companyId.
     * @param dataType if not empty, filters the requested information by data type.
     * @param reportingPeriod if not empty, filters the requested information by reporting period.
     * @param nonSourceable if not null, filters the requested information to include only datasets
     *                      with a non-sourceable flag matching the provided value (true or false).
     * @return a list of NonSourceableInfo objects that match the specified filters.
     */
    fun getNonSourceableDataByFilters(
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        nonSourceable: Boolean?,
    ): List<NonSourceableInfoResponse> {
        val nonSourceableEntities =
            nonSourceableDataRepository
                .searchNonSourceableData(
                    NonSourceableDataSearchFilter(
                        companyId,
                        dataType,
                        reportingPeriod,
                        nonSourceable,
                    ),
                )
        return nonSourceableEntities.map { it.toApiModelResponse() }
    }

    /**
     * The method throws an exception if a specific dataset is sourceable or not found.
     * @param companyId filters for the specific company
     * @param dataType filters for the specific data type
     * @param reportingPeriod filters for the specific reporting period
     */

    fun checkDataIsNonSourceable(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ) {
        if (isDataNonSourceable(companyId, dataType, reportingPeriod) != true) {
            throw ResourceNotFoundApiException(
                summary = "Dataset is sourceable or not found.",
                message =
                    "No non-sourceable dataset found for company $companyId, dataType $dataType, " +
                        "and reportingPeriod $reportingPeriod.",
            )
        }
    }

    /**
     * The method checks if a specific data set is non-sourceable.
     * @param companyId filters for the specific company
     * @param dataType filters for the specific data type
     * @param reportingPeriod filters for the specific reporting period
     */
    fun isDataNonSourceable(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ): Boolean? {
        val latestNonSourceableEntity =
            nonSourceableDataRepository.getLatestNonSourceableInfoForDataset(
                NonSourceableDataSearchFilter(
                    companyId,
                    dataType,
                    reportingPeriod,
                    null,
                ),
            )
        return latestNonSourceableEntity?.isNonSourceable
    }

    /**
     * Stores a NonSourceableEntity in the data-sourceability table, marking the previously
     * non-sourceable dataset as sourceable. This is triggered by the upload of the dataset.
     *
     * @param companyId the ID of the company associated with the dataset.
     * @param dataType the type of the dataset being uploaded.
     * @param reportingPeriod the reporting period of the dataset, typically a specific year or quarter.
     * @param uploaderId the ID of the user who uploaded the dataset, used for audit purposes.
     */
    @Transactional
    fun storeSourceableData(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
        uploaderId: String,
    ) {
        val creationTime = Instant.now().toEpochMilli()

        if (isDataNonSourceable(companyId, dataType, reportingPeriod) != true) {
            return
        }
        val nonSourceableEntity =
            NonSourceableEntity(
                eventId = null,
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                isNonSourceable = false,
                reason = "Uploaded by a user with the Id:$uploaderId",
                creationTime = creationTime,
                userId = uploaderId,
            )
        nonSourceableDataRepository.save(nonSourceableEntity)
    }
}
