package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.JudgementDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetJudgementValidationHelper
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

/**
 * Service class for dataset judgement objects.
 */
@Service
class DatasetJudgementService
    @Autowired
    constructor(
        private val datasetJudgementRepository: DatasetJudgementRepository,
        private val datasetJudgementSupportService: DatasetJudgementSupportService,
        private val datasetJudgementCreationService: DatasetJudgementCreationService,
    ) {
        /**
         * Creates and stores a new dataset judgement for the given dataset ID.
         *
         * Retrieves associated metadata and data points and checks for existing pending judgements.
         * Throws an exception if the dataset does not exist or a pending judgement is already present.
         * Returns the persisted judgement entity as API response.
         *
         * @param datasetId The UUID of the dataset to judge.
         * @return DatasetJudgementResponse API response with created judgement details.
         * @throws ResourceNotFoundApiException If the dataset is not found.
         * @throws ConflictApiException If a pending judgement exists.
         */
        @Transactional
        fun postDatasetJudgement(datasetId: UUID): DatasetJudgementResponse {
            val datatypeToDatapointIds =
                try {
                    datasetJudgementSupportService.getContainedDataPoints(datasetId.toString())
                } catch (_: BackendClientException) {
                    throw ResourceNotFoundApiException(
                        "Dataset not found",
                        "Dataset with the id: $datasetId could not be found.",
                    )
                }
            if (datasetJudgementRepository.findAllByDatasetIdAndJudgementState(datasetId, DatasetJudgementState.Pending).isNotEmpty()) {
                throw ConflictApiException(
                    summary = "Pending dataset judgement entity already exists.",
                    message = "There is already a dataset judgement entity for this dataset which is pending.",
                )
            }

            val datasetJudgementEntity =
                datasetJudgementCreationService.createDatasetJudgementEntity(
                    datasetJudgementSupportService.getDataMetaInfo(datasetId.toString()),
                    datasetId,
                    datatypeToDatapointIds,
                )

            return datasetJudgementRepository.save(datasetJudgementEntity).toDatasetJudgementResponse()
        }

        /**
         * Method to set judge to current user.
         *
         * @param datasetJudgementId The UUID of the dataset judgement to update.
         * @return DatasetJudgementResponse The API response with updated judgement details.
         * @throws ResourceNotFoundApiException If the dataset judgement does not exist.
         */
        @Transactional
        fun setJudge(datasetJudgementId: UUID): DatasetJudgementResponse {
            val datasetJudgement = getDatasetJudgement(datasetJudgementId)
            val judgeId = DatalandAuthentication.fromContext().userId
            datasetJudgementCreationService.setJudge(datasetJudgement, judgeId)
            return datasetJudgementRepository.save(datasetJudgement).toDatasetJudgementResponse()
        }

        /**
         * Sets the judgement state for a dataset judgement entity.
         *
         * Validates judges permissions and updates the judgement state to the specified value.
         * Persists the change and returns the updated dataset judgement as an API response.
         * Throws an exception if the current user is not the judge.
         *
         * @param datasetJudgementId The UUID of the dataset judgement to update.
         * @param state The new judgement state to apply.
         * @return DatasetJudgementResponse The API response with updated review details.
         */
        @Transactional
        fun setJudgementState(
            datasetJudgementId: UUID,
            state: DatasetJudgementState,
        ): DatasetJudgementResponse {
            val datasetJudgement = getDatasetJudgement(datasetJudgementId)
            DatasetJudgementValidationHelper.validateUserIsJudge(datasetJudgement.qaJudgeUserId)
            datasetJudgement.judgementState = state
            return datasetJudgementRepository.save(datasetJudgement).toDatasetJudgementResponse()
        }

        /**
         * Updates judgement details for a specific data point in a dataset judgement.
         *
         * Validates and applies patch values for accepted source, custom value, and QA report company ID to the specified data point.
         * Throws exceptions for invalid input or missing required values, and persists the updated judgement entity.
         * Returns the modified dataset judgement as API response.
         *
         * @param datasetJudgementId The UUID of the dataset judgement to update.
         * @param dataPointType The type identifier for the data point to patch.
         * @param patch The patch object containing updates for judgement details.
         * @return DatasetJudgementResponse API response with updated judgement details.
         * @throws InvalidInputApiException If input values are invalid or required values are missing.
         */
        @Transactional
        fun patchJudgementDetails(
            datasetJudgementId: UUID,
            dataPointType: String,
            patch: JudgementDetailsPatch,
        ): DatasetJudgementResponse {
            val datasetJudgement = getDatasetJudgement(datasetJudgementId)
            DatasetJudgementValidationHelper.validateUserIsJudge(datasetJudgement.qaJudgeUserId)
            DatasetJudgementValidationHelper.validatePatchContainsCustomDataPointOrAcceptedSource(patch)
            val dataPoint =
                datasetJudgement.dataPoints
                    .find { it.dataPointType == dataPointType }
                    ?: throw InvalidInputApiException(
                        "Invalid input.",
                        "Data point with type '$dataPointType' not found.",
                    )

            applyCustomDataPoint(dataPointType, patch.customDataPoint, dataPoint)
            applyAcceptedSource(dataPoint, patch)

            return datasetJudgementRepository.save(datasetJudgement).toDatasetJudgementResponse()
        }

        /**
         * Validates and applies the custom data point value to the given data point.
         *
         * @param dataPointType The type identifier used for validation.
         * @param customDataPoint The custom value from the patch, or null.
         * @param dataPoint The data point entity to update.
         * @throws InvalidInputApiException If the custom value fails validation.
         */
        private fun applyCustomDataPoint(
            dataPointType: String,
            customDataPoint: String?,
            dataPoint: DataPointJudgementEntity,
        ) {
            if (customDataPoint != null) {
                try {
                    datasetJudgementSupportService.validateCustomDataPoint(customDataPoint, dataPointType)
                } catch (e: BackendClientException) {
                    throw InvalidInputApiException(
                        "Custom datapoint not valid.",
                        "Custom datapoint given does not match the specification of $dataPointType.",
                        e,
                    )
                }
                dataPoint.customValue = customDataPoint
            }
        }

        /**
         * Applies the accepted source and related fields based on the patch.
         *
         * @param dataPoint The data point entity to update.
         * @param patch The patch containing the accepted source and related data.
         */
        private fun applyAcceptedSource(
            dataPoint: DataPointJudgementEntity,
            patch: JudgementDetailsPatch,
        ) {
            when (patch.acceptedSource) {
                AcceptedDataPointSource.Original -> {
                    dataPoint.apply {
                        this.acceptedSource = AcceptedDataPointSource.Original
                        this.reporterUserIdOfAcceptedQaReport = null
                    }
                }
                AcceptedDataPointSource.Qa -> {
                    DatasetJudgementValidationHelper.validateReporterUserIdOfAcceptedQaReport(
                        dataPoint.qaReports,
                        patch.reporterUserIdOfAcceptedQaReport,
                    )
                    dataPoint.apply {
                        this.acceptedSource = AcceptedDataPointSource.Qa
                        this.reporterUserIdOfAcceptedQaReport =
                            UUID.fromString(patch.reporterUserIdOfAcceptedQaReport)
                    }
                }
                AcceptedDataPointSource.Custom -> {
                    DatasetJudgementValidationHelper.validateCustomDataPointIsSet(dataPoint)
                    dataPoint.apply {
                        this.acceptedSource = AcceptedDataPointSource.Custom
                        this.reporterUserIdOfAcceptedQaReport = null
                    }
                }
                null -> return
            }
        }

        /**
         * Method to get a dataset judgement entity by id and convert to response.
         *
         * @param datasetJudgementId The UUID of the dataset judgement to fetch.
         * @return DatasetJudgementResponse API response for the requested judgement.
         * @throws ResourceNotFoundApiException If the dataset judgement does not exist.
         */
        @Transactional(readOnly = true)
        fun getDatasetJudgementById(datasetJudgementId: UUID): DatasetJudgementResponse {
            val datasetJudgement = getDatasetJudgement(datasetJudgementId)
            return datasetJudgement.toDatasetJudgementResponse()
        }

        /**
         * Method to get dataset judgement objects by dataset id.
         *
         * @param datasetId The UUID of the dataset whose judgements should be fetched.
         * @return List of DatasetJudgementResponse for the given dataset.
         */
        @Transactional(readOnly = true)
        fun getDatasetJudgementsByDatasetId(datasetId: UUID): List<DatasetJudgementResponse> =
            datasetJudgementRepository.findAllByDatasetId(datasetId).map {
                it.toDatasetJudgementResponse()
            }

        /**
         * Loads the dataset judgement entity or throws if it does not exist.
         *
         * @param datasetJudgementId The UUID of the dataset judgement to load.
         * @return The dataset judgement entity for the given id.
         * @throws ResourceNotFoundApiException If no dataset judgement exists for the given id.
         */
        private fun getDatasetJudgement(datasetJudgementId: UUID): DatasetJudgementEntity =
            datasetJudgementSupportService.getDatasetJudgementEntityById(datasetJudgementId)
                ?: throw ResourceNotFoundApiException(
                    "Dataset review object not found",
                    "No Dataset review object with the id: $datasetJudgementId could be found.",
                )
    }
