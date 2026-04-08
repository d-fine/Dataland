package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service for non-sourceability QA review listing and decision workflows.
 */
@Service
class NonSourceabilityQaReviewManager(
    @Autowired private val nonSourceableQaReviewRepository: NonSourceableQaReviewRepository,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun NonSourceableQaReviewInformationEntity.toApiModel(): NonSourceableQaReviewInformation =
        NonSourceableQaReviewInformation(
            nonSourceabilityId = nonSourceabilityId.toString(),
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            qaStatus = qaStatus,
            reason = reason,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            reviewerUserId = reviewerUserId,
            qaComment = qaComment,
        )

    /**
     * Retrieves non-sourceability QA review records filtered by optional criteria and paginated.
     *
     * @param filters the filters and pagination parameters
     * @return a paginated list of non-sourceability QA review information
     */
    @Transactional(readOnly = true)
    fun getNonSourceabilityReviews(filters: NonSourceabilityReviewQueryParams): List<NonSourceableQaReviewInformation> {
        val reviewFilters =
            NonSourceabilityReviewFilters(
                companyId = filters.companyId,
                dataType = filters.dataType,
                reportingPeriod = filters.reportingPeriod,
                qaStatus = filters.qaStatus,
                showOnlyActive = filters.showOnlyActive,
            )
        val filtered = applyFilters(reviewFilters)

        val offset = filters.chunkIndex * filters.chunkSize
        return filtered
            .drop(offset)
            .take(filters.chunkSize)
            .map { it.toApiModel() }
    }

    private fun applyFilters(filters: NonSourceabilityReviewFilters): List<NonSourceableQaReviewInformationEntity> =
        nonSourceableQaReviewRepository
            .findAll()
            .asSequence()
            .filter { filters.companyId.isNullOrBlank() || it.companyId == filters.companyId }
            .filter { filters.dataType.isNullOrBlank() || it.dataType == filters.dataType }
            .filter { filters.reportingPeriod.isNullOrBlank() || it.reportingPeriod == filters.reportingPeriod }
            .filter { filters.qaStatus == null || it.qaStatus == filters.qaStatus }
            .filter { !filters.showOnlyActive || it.qaStatus != QaStatus.Rejected }
            .sortedByDescending { it.uploadTime }
            .toList()

    /**
     * Query parameters for retrieving non-sourceability QA reviews with filtering and pagination.
     *
     * @property companyId optional company identifier to filter by
     * @property dataType optional data type to filter by
     * @property reportingPeriod optional reporting period to filter by
     * @property qaStatus optional QA status to filter by
     * @property showOnlyActive whether to exclude rejected reviews
     * @property chunkSize the number of records per page
     * @property chunkIndex the page index (0-based)
     */
    data class NonSourceabilityReviewQueryParams(
        val companyId: String? = null,
        val dataType: String? = null,
        val reportingPeriod: String? = null,
        val qaStatus: QaStatus? = null,
        val showOnlyActive: Boolean = true,
        val chunkSize: Int = 10,
        val chunkIndex: Int = 0,
    )

    private data class NonSourceabilityReviewFilters(
        val companyId: String? = null,
        val dataType: String? = null,
        val reportingPeriod: String? = null,
        val qaStatus: QaStatus? = null,
        val showOnlyActive: Boolean = true,
    )

    /**
     * Retrieves all pending non-sourceability QA reviews in chronological order.
     *
     * @return a list of pending non-sourceability QA review information
     */
    @Transactional(readOnly = true)
    fun getPendingNonSourceabilityQueue(): List<NonSourceableQaReviewInformation> =
        nonSourceableQaReviewRepository
            .findAllByQaStatusOrderByUploadTimeAsc(QaStatus.Pending)
            .map { it.toApiModel() }

    /**
     * Records a non-sourceability QA decision (Accepted or Rejected).
     * Publishes a lifecycle event to notify downstream services.
     *
     * @param nonSourceabilityId the canonical non-sourceability record id
     * @param qaStatus the QA decision status (Accepted or Rejected)
     * @param qaComment optional comment from the reviewer
     * @return the updated non-sourceability QA review information
     * @throws InvalidInputApiException if qaStatus is not Accepted or Rejected, or if id is invalid
     * @throws ResourceNotFoundApiException if the review entry is not found
     */
    @Transactional
    fun decideNonSourceability(
        nonSourceabilityId: String,
        qaStatus: QaStatus,
        qaComment: String?,
    ): NonSourceableQaReviewInformation {
        if (qaStatus !in setOf(QaStatus.Accepted, QaStatus.Rejected)) {
            throw InvalidInputApiException(
                summary = "Unsupported non-sourceability decision status.",
                message = "Only Accepted and Rejected decisions are supported for non-sourceability reviews.",
            )
        }

        val parsedId =
            runCatching { UUID.fromString(nonSourceabilityId) }
                .getOrElse {
                    throw InvalidInputApiException(
                        summary = "Invalid nonSourceabilityId format.",
                        message = "nonSourceabilityId must be a valid UUID.",
                    )
                }

        val entity =
            nonSourceableQaReviewRepository.findById(parsedId).orElseThrow {
                ResourceNotFoundApiException(
                    summary = "Non-sourceability review entry not found.",
                    message = "No non-sourceability QA review entry found for id $nonSourceabilityId.",
                )
            }

        val reviewerUserId = DatalandAuthentication.fromContext().userId
        entity.qaStatus = qaStatus
        entity.qaComment = qaComment
        entity.reviewerUserId = reviewerUserId

        val saved = nonSourceableQaReviewRepository.save(entity)

        val eventType = if (qaStatus == QaStatus.Accepted) NonSourceabilityEventType.QA_ACCEPTED else NonSourceabilityEventType.QA_REJECTED
        val lifecycleEvent =
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = saved.nonSourceabilityId.toString(),
                companyId = saved.companyId,
                dataType = saved.dataType,
                reportingPeriod = saved.reportingPeriod,
                eventType = eventType,
                qaStatus = saved.qaStatus,
                currentlyActive = saved.qaStatus == QaStatus.Accepted,
                reason = saved.reason,
                uploaderUserId = saved.uploaderUserId,
                uploadTime = saved.uploadTime,
            )

        val correlationId = UUID.randomUUID().toString()
        logger.info(
            "Persisted non-sourceability QA decision {} for {} (correlationId: {})",
            qaStatus,
            saved.nonSourceabilityId,
            correlationId,
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(lifecycleEvent),
            MessageType.NON_SOURCEABILITY_LIFECYCLE,
            correlationId,
            ExchangeName.BACKEND_DATA_NONSOURCEABLE,
            RoutingKeyNames.NON_SOURCEABILITY_LIFECYCLE,
        )

        return saved.toApiModel()
    }
}
