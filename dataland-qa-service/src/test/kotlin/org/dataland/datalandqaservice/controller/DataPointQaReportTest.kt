package org.dataland.datalandqaservice.controller

import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointToValidate
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.DataPointQaReportController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportStatusPatch
import org.dataland.datalandqaservice.utils.NoBackendRequestQaReportConfiguration
import org.dataland.datalandqaservice.utils.UtilityFunctions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.test.context.SpringRabbitTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.dataland.datalandbackendutils.model.QaStatus as UtilsQaStatus

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
        NoBackendRequestQaReportConfiguration::class,
    ],
    properties = ["spring.profiles.active=nodb"],
)
@SpringRabbitTest
class DataPointQaReportTest(
    @Autowired private val dataPointQaReportController: DataPointQaReportController,
    @Autowired private val qaController: QaController,
) {
    @MockitoBean private lateinit var dataPointApi: DataPointControllerApi

    @MockitoBean private lateinit var companyDataControllerApi: CompanyDataControllerApi

    // Mocked to avoid keycloak token request
    @Suppress("UnusedPrivateProperty")
    @MockitoBean
    private lateinit var metaDataControllerApi: MetaDataControllerApi

    // Mocked to avoid RabbitMQ connection exceptions
    @Suppress("UnusedPrivateProperty")
    @MockitoBean
    private lateinit var rabbitTemplate: RabbitTemplate

    private val dummyQaReportDataPoint =
        QaReportDataPoint<String?>(
            comment = "comment",
            verdict = QaReportDataPointVerdict.QaAccepted,
            correctedData = null,
        )

    private val dummyDataId = "dummyDataId"
    private val dummyCompanyId = "dummyCompanyId"
    private val dummyDataPointType = "dummyDataPointType"
    private val incorrectDataPlaceholder = "incorrect-data"

    private val dummyDataMetaInformation =
        DataPointMetaInformation(
            dataPointId = dummyDataId,
            dataPointType = dummyDataPointType,
            qaStatus = QaStatus.Pending,
            reportingPeriod = "dummyReportingPeriod",
            companyId = dummyCompanyId,
            uploadTime = 0,
            uploaderUserId = "some-reviewer",
            currentlyActive = true,
        )

    private val dummyCompanyInformation =
        StoredCompany(
            companyId = dummyCompanyId,
            companyInformation =
                CompanyInformation(
                    companyName = "dummyCompanyName",
                    headquarters = "dummyHeadquarters",
                    countryCode = "de",
                    identifiers = emptyMap(),
                ),
            dataRegisteredByDataland = emptyList(),
        )

    @BeforeEach
    fun `setup api mocks`() {
        `when`(dataPointApi.getDataPointMetaInfo(dummyDataId)).thenReturn(dummyDataMetaInformation)
        `when`(dataPointApi.validateDataPoint(DataPointToValidate(incorrectDataPlaceholder, dummyDataPointType)))
            .thenThrow(ClientException(statusCode = 400))
        `when`(companyDataControllerApi.getCompanyById(dummyCompanyId)).thenReturn(dummyCompanyInformation)
    }

    @Test
    fun `uploading a data point QA report should work`() {
        UtilityFunctions.withReviewerAuthentication {
            val qaReportObject = dataPointQaReportController.postQaReport(dummyDataId, dummyQaReportDataPoint)
            val retrievedQaReportObject = dataPointQaReportController.getQaReport(dummyDataId, qaReportObject.body!!.qaReportId)
            assert(retrievedQaReportObject.body!! == qaReportObject.body!!)
        }
    }

    @Test
    fun `uploading a data point QA report with incorrect data should not work`() {
        UtilityFunctions.withReviewerAuthentication {
            assertThrows<InvalidInputApiException> {
                dataPointQaReportController.postQaReport(
                    dummyDataId,
                    QaReportDataPoint(
                        comment = "",
                        verdict = QaReportDataPointVerdict.QaRejected,
                        correctedData = incorrectDataPlaceholder,
                    ),
                )
            }
        }
    }

    @Test
    fun `uploading a data point QA report should update the QA status`() {
        UtilityFunctions.withReviewerAuthentication {
            dataPointQaReportController.postQaReport(dummyDataId, dummyQaReportDataPoint)
            val retrievedDataPointMetaInformation = qaController.getDataPointQaReviewInformationByDataId(dummyDataId)
            assert(retrievedDataPointMetaInformation.body!![0].qaStatus == UtilsQaStatus.Accepted)
        }
    }

    @Test
    fun `setting the QA report status should work`() {
        UtilityFunctions.withReviewerAuthentication {
            val qaReportObject = dataPointQaReportController.postQaReport(dummyDataId, dummyQaReportDataPoint)
            dataPointQaReportController.setQaReportStatus(dummyDataId, qaReportObject.body!!.qaReportId, QaReportStatusPatch(false))
            val retrievedQaReportObject = dataPointQaReportController.getQaReport(dummyDataId, qaReportObject.body!!.qaReportId)
            assert(!retrievedQaReportObject.body!!.active)
        }
    }
}
