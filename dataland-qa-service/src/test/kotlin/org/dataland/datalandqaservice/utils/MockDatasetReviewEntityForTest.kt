package org.dataland.datalandqaservice.utils

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporter
import java.util.UUID

object MockDatasetReviewEntityForTest {
    val dummyUserId = UUID.randomUUID()
    const val DUMMY_DATA_POINT_TYPE = "dummy-datapoint-type"
    val dummyCompanyId = UUID.randomUUID()
    val dummyDatasetId = UUID.randomUUID()
    val dummyReporterCompanyId = UUID.randomUUID()
    const val DUMMY_USER_NAME = "Dummy User"
    const val DUMMY_USER_EMAIL = "dummyUser@dataland.com"
    const val DUMMY_USER_FIRST_NAME = "Dummy"
    const val DUMMY_USER_LAST_NAME = "User"

    val dummyDatapointId = UUID.randomUUID().toString()
    val reporterCompanyId = UUID.randomUUID().toString()
    const val REPORTER_COMPANY_NAME = "Reporter Company"
    val qaReportId = UUID.randomUUID().toString()
    const val CUSTOM_VALUE = """{"value": 42}"""

    fun createDummyDatasetReviewEntity(): DatasetJudgementEntity =
        DatasetJudgementEntity(
            dataSetReviewId = UUID.randomUUID(),
            datasetId = dummyDatasetId,
            companyId = dummyCompanyId,
            dataType = "sfdr",
            reportingPeriod = "2026",
            qaJudgeUserId = dummyUserId,
            qaJudgeUserName = DUMMY_USER_NAME,
            qaReporters =
                mutableListOf(
                    QaReporter(
                        reporterUserId = dummyUserId,
                        reporterUserName = DUMMY_USER_NAME,
                        reporterEmailAddress = DUMMY_USER_EMAIL,
                        reportCompanyName = REPORTER_COMPANY_NAME,
                        reporterCompanyId = dummyReporterCompanyId,
                    ),
                ),
            dataPoints =
                mutableListOf(
                    DataPointJudgementEntity(
                        dataPointType = DUMMY_DATA_POINT_TYPE,
                        dataPointId = UUID.randomUUID(),
                        qaReports =
                            mutableListOf(
                                QaReportDataPointWithReporterEntity(
                                    qaReportId = UUID.randomUUID(),
                                    verdict = QaReportDataPointVerdict.QaAccepted,
                                    correctedData = null,
                                    reporterUserId = dummyUserId,
                                    reporterCompanyId = dummyReporterCompanyId,
                                ),
                            ),
                        acceptedSource = null,
                        reporterUserIdOfAcceptedQaReport = null,
                        companyIdOfAcceptedQaReport = null,
                        customValue = null,
                        datasetReview = null,
                    ),
                ),
        )

    fun createDummyMetaData(): DataMetaInformation =
        DataMetaInformation(
            dataId = UUID.randomUUID().toString(),
            companyId = dummyCompanyId.toString(),
            dataType = DataTypeEnum.sfdr,
            uploadTime = 0L,
            reportingPeriod = "2026",
            currentlyActive = true,
            qaStatus = QaStatus.Pending,
        )
}
