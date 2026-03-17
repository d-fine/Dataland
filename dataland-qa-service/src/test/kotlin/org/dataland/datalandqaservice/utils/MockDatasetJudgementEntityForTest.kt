package org.dataland.datalandqaservice.utils

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporter
import java.util.UUID

object MockDatasetJudgementEntityForTest {
    val dummyUserId = UUID.randomUUID()
    const val DUMMY_DATA_POINT_TYPE = "dummy-datapoint-type"
    val dummyCompanyId = UUID.randomUUID()
    val dummyDatasetId = UUID.randomUUID()
    const val DUMMY_USER_NAME = "Dummy User"
    const val DUMMY_USER_EMAIL = "dummyUser@dataland.com"
    const val DUMMY_USER_FIRST_NAME = "Dummy"
    const val DUMMY_USER_LAST_NAME = "User"

    val dummyDatapointId = UUID.randomUUID().toString()
    val qaReportId = UUID.randomUUID().toString()
    const val CUSTOM_VALUE = """{"value": 42}"""

    fun createDummyDatasetJudgementEntity(): DatasetJudgementEntity =
        DatasetJudgementEntity(
            dataSetJudgementId = UUID.randomUUID(),
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
                    ),
                ),
            dataPoints =
                mutableListOf(
                    DataPointJudgementEntity(
                        dataPointType = DUMMY_DATA_POINT_TYPE,
                        dataPointId = UUID.randomUUID(),
                        qaReports =
                            mutableListOf(
                                DataPointQaReportEntity(
                                    qaReportId = UUID.randomUUID().toString(),
                                    verdict = QaReportDataPointVerdict.QaAccepted,
                                    correctedData = null,
                                    reporterUserId = dummyUserId.toString(),
                                    comment = "dummy comment",
                                    dataPointId = UUID.randomUUID().toString(),
                                    dataPointType = DUMMY_DATA_POINT_TYPE,
                                    uploadTime = 1000,
                                    active = true,
                                ),
                            ),
                        acceptedSource = null,
                        reporterUserIdOfAcceptedQaReport = null,
                        customValue = null,
                        datasetJudgement = null,
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
