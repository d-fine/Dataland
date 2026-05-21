package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class PreApprovalServiceTest {
    private val dummyReporter1 = UUID.randomUUID().toString()
    private val dummyReporter2 = UUID.randomUUID().toString()

    private fun buildQaReport(
        reporterUserId: String,
        verdict: QaReportDataPointVerdict,
        dataPointId: String = UUID.randomUUID().toString(),
    ): DataPointQaReportEntity =
        DataPointQaReportEntity(
            qaReportId = UUID.randomUUID().toString(),
            comment = "",
            verdict = verdict,
            correctedData = null,
            dataPointId = dataPointId,
            dataPointType = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
            reporterUserId = reporterUserId,
            uploadTime = 1000L,
            active = true,
        )

    private fun buildDataPointJudgementEntity(
        qaReports: List<DataPointQaReportEntity>,
        dataPointType: String = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): DataPointJudgementEntity =
        DataPointJudgementEntity(
            dataPointType = dataPointType,
            dataPointId = UUID.randomUUID().toString(),
            qaReports = qaReports.toMutableList(),
            acceptedSource = null,
            reporterUserIdOfAcceptedQaReport = null,
            customValue = null,
        )

    private fun runWorkflow(
        service: PreApprovalService,
        reports: List<DataPointQaReportEntity>,
        dataPointType: String = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): AcceptedDataPointSource? {
        val dataPoint = buildDataPointJudgementEntity(reports, dataPointType = dataPointType)
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(dataPoint)
        return service
            .runPreApprovalWorkflow(entity)
            .dataPoints
            .first()
            .acceptedSource
    }

    @Nested
    inner class GeneralPreApprovalTests {
        @Test
        fun `No preapproval when environment variable is set to false`() {
            val service = PreApprovalService(autoPreApprovalEnabled = false, exemptFieldsConfig = PreApprovalExemptFieldsConfig())
            val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()

            val result = service.runPreApprovalWorkflow(entity)

            assertNull(result.dataPoints.first().acceptedSource)
        }

        @Test
        fun `Preapproval works when environment variable is true, there is only 1 reporter and report is QaAccepted`() {
            val service = PreApprovalService(autoPreApprovalEnabled = true, exemptFieldsConfig = PreApprovalExemptFieldsConfig())
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
        }

        @Test
        fun `Preapproval works when environment variable is true, there are 2 reporter and all reports are QaAccepted`() {
            val service = PreApprovalService(autoPreApprovalEnabled = true, exemptFieldsConfig = PreApprovalExemptFieldsConfig())
            val reports =
                listOf(
                    buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted),
                    buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaAccepted),
                )

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
        }

        @Test
        fun `No preapproval when there are two reports with mixed verdicts`() {
            val service = PreApprovalService(autoPreApprovalEnabled = true, exemptFieldsConfig = PreApprovalExemptFieldsConfig())
            val reports =
                listOf(
                    buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted),
                    buildQaReport(dummyReporter2, QaReportDataPointVerdict.QaRejected),
                )

            assertNull(runWorkflow(service, reports))
        }

        @Test
        fun `No preapproval when there are no QA reports`() {
            val service = PreApprovalService(autoPreApprovalEnabled = true, exemptFieldsConfig = PreApprovalExemptFieldsConfig())

            assertNull(runWorkflow(service, emptyList()))
        }
    }

    @Nested
    inner class ExemptFieldsTests {
        @Test
        fun `No preapproval for exempt field even if all reports are QaAccepted`() {
            val exemptField = "exempt-field-type"
            val service =
                PreApprovalService(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf(exemptField))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertNull(runWorkflow(service, reports, dataPointType = exemptField))
        }

        @Test
        fun `Preapproval works for non-exempt field when all reports are QaAccepted`() {
            val nonExemptField = "non-exempt-field-type"
            val service =
                PreApprovalService(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf("some-exempt-field-type"))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports, dataPointType = nonExemptField))
        }

        @Test
        fun `All qualifying fields are auto-accepted when exempt fields list is empty`() {
            val service =
                PreApprovalService(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(emptyMap()),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
        }

        @Test
        fun `Only non-exempt fields are auto-accepted when multiple fields are present`() {
            val exemptField = "exempt-field-type"
            val nonExemptField = "non-exempt-field-type"
            val service =
                PreApprovalService(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf(exemptField))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))
            val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
            entity.dataPoints.clear()
            entity.dataPoints.add(buildDataPointJudgementEntity(reports, dataPointType = exemptField))
            entity.dataPoints.add(buildDataPointJudgementEntity(reports, dataPointType = nonExemptField))

            val result = service.runPreApprovalWorkflow(entity)

            assertNull(result.dataPoints.first { it.dataPointType == exemptField }.acceptedSource)
            assertEquals(
                AcceptedDataPointSource.Original,
                result.dataPoints.first { it.dataPointType == nonExemptField }.acceptedSource,
            )
        }

        @Test
        fun `Qualifying fields are auto-accepted when exempt fields list contains only non-existent fields`() {
            val service =
                PreApprovalService(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.sfdr to setOf("non-existent-field"))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports))
        }

        @Test
        fun `Exempt field in one framework does not block preapproval for the same field in another framework`() {
            val fieldName = "shared-field-type"
            val service =
                PreApprovalService(
                    autoPreApprovalEnabled = true,
                    exemptFieldsConfig = PreApprovalExemptFieldsConfig(mapOf(DataTypeEnum.vsme to setOf(fieldName))),
                )
            val reports = listOf(buildQaReport(dummyReporter1, QaReportDataPointVerdict.QaAccepted))

            assertEquals(AcceptedDataPointSource.Original, runWorkflow(service, reports, dataPointType = fieldName))
        }
    }
}
