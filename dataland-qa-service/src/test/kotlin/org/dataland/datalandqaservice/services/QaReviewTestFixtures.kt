package org.dataland.datalandqaservice.services

import org.dataland.dataSourcingService.openApiClient.model.DataSourcingPriorityByDataDimensions
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import java.util.UUID

data class TestSetResult(
    val qaReview: QaReviewEntity,
    val datasetJudgement: DatasetJudgementEntity,
    val response: QaReviewResponse,
    val dataSourcingPriority: DataSourcingPriorityByDataDimensions,
)

data class BuildTestSetArgs(
    val dataId: String,
    val companyId: String,
    val companyName: String,
    val reportingPeriod: String,
    val timestamp: Long = 0L,
    val triggeringUserId: String = "",
    val comment: String? = null,
    val judgementId: UUID,
    val qaJudgeUserId: UUID,
    val qaJudgeUserName: String,
    val numberQaReports: Long = 0L,
    val priority: Int = 0,
    val framework: String = "sfdr",
)

private fun buildDataSourcingPriority(args: BuildTestSetArgs): DataSourcingPriorityByDataDimensions {
    val dataSourcingPriority =
        DataSourcingPriorityByDataDimensions(
            dataType = args.framework,
            reportingPeriod = args.reportingPeriod,
            companyId = args.companyId,
            priority = args.priority,
        )

    return dataSourcingPriority
}

/**
 * Build a reusable test fixture bundle for QA review tests.
 *
 * This helper constructs a fully-populated set of objects used across tests:
 * - [QaReviewEntity]
 * - [DatasetJudgementEntity]
 * - [QaReviewResponse]
 * - [DataSourcingPriorityByDataDimensions]
 *
 * The function accepts a single [BuildTestSetArgs] parameter that contains only
 * the values that vary between test cases (IDs, names, timestamps, counts,
 * priority, and optional comment).
 *
 * @param args configuration values for the generated fixtures
 * @return a [TestSetResult] containing the four constructed test objects
 */
internal fun buildTestSet(args: BuildTestSetArgs): TestSetResult {
    val qaReview =
        QaReviewEntity(
            dataId = args.dataId,
            companyId = args.companyId,
            companyName = args.companyName,
            framework = args.framework,
            reportingPeriod = args.reportingPeriod,
            timestamp = args.timestamp,
            qaStatus = QaStatus.Pending,
            triggeringUserId = args.triggeringUserId,
            comment = args.comment,
        )

    val datasetJudgement =
        DatasetJudgementEntity(
            dataSetJudgementId = args.judgementId,
            datasetId = UUID.fromString(args.dataId),
            companyId = UUID.randomUUID(),
            dataType = DataTypeEnum.sfdr,
            reportingPeriod = args.reportingPeriod,
            judgementState = DatasetJudgementState.Pending,
            qaJudgeUserId = args.qaJudgeUserId,
            qaJudgeUserName = args.qaJudgeUserName,
            qaReporters = mutableListOf(),
            dataPoints = mutableListOf(),
        )

    val response =
        QaReviewResponse(
            dataId = args.dataId,
            companyId = args.companyId,
            companyName = args.companyName,
            framework = args.framework,
            reportingPeriod = args.reportingPeriod,
            timestamp = args.timestamp,
            qaStatus = QaStatus.Pending,
            qaJudgeUserId = args.qaJudgeUserId.toString(),
            qaJudgeUserName = args.qaJudgeUserName,
            datasetReviewId = args.judgementId.toString(),
            numberQaReports = args.numberQaReports,
            comment = args.comment,
            triggeringUserId = null,
            priorityOfAssociatedDataSourcing = args.priority,
        )

    val dataSourcingPriority = buildDataSourcingPriority(args)

    return TestSetResult(qaReview, datasetJudgement, response, dataSourcingPriority)
}
