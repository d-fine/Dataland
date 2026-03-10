package org.dataland.datalandqaservice.utils

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointReviewDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporter
import java.util.UUID

object MockDatasetReviewEntityForTest {
    val dummyUserId = UUID.randomUUID()
    val dummyDataPointType = "dummy-datapoint-type"
    val dummyCompanyId = UUID.randomUUID()
    val dummyDatasetId = UUID.randomUUID()
    val dummyReporterCompanyId = UUID.randomUUID()
    val dummyUserName = "Dummy User"
    val dummyUserEmail = "dummyUser@dataland.com"
    val dummyUserFirstName = "Dummy"
    val dummyUserLastName = "User"

    val dummyDatapointId = UUID.randomUUID().toString()
    val reporterCompanyId = UUID.randomUUID().toString()
    val reporterCompanyName = "Reporter Company"
    val qaReportId = UUID.randomUUID().toString()
    val customValue = """{"value": 42}"""

    fun createDummyDatasetReviewEntity(): DatasetReviewEntity =
        DatasetReviewEntity(
            dataSetReviewId = UUID.randomUUID(),
            datasetId = dummyDatasetId,
            companyId = dummyCompanyId,
            dataType = "sfdr",
            reportingPeriod = "2026",
            reviewerUserId = dummyUserId,
            reviewerUserName = dummyUserName,
            qaReporters =
                mutableListOf(
                    QaReporter(
                        reporterUserId = dummyUserId,
                        reporterUserName = dummyUserName,
                        reporterEmailAddress = dummyUserEmail,
                        reportCompanyName = reporterCompanyName,
                        reporterCompanyId = dummyReporterCompanyId,
                    ),
                ),
            dataPoints =
                mutableListOf(
                    DataPointReviewDetailsEntity(
                        dataPointType = dummyDataPointType,
                        dataPointId = UUID.randomUUID(),
                        qaReports =
                            mutableListOf(
                                QaReportDataPointWithReporterDetailsEntity(
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
