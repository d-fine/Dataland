package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReporter

/**
 * API model for the dataset judgement objects returned.
 */
data class DatasetJudgementResponse(
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_ID_EXAMPLE,
    )
    val dataSetJudgementId: String,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
    )
    val datasetId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        implementation = DataTypeEnum::class,
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_STATE_DESCRIPTION,
    )
    var judgementState: DatasetJudgementState,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_JUDGE_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_JUDGE_ID_EXAMPLE,
    )
    var qaJudgeUserId: String?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_JUDGE_USERNAME_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.DATA_JUDGEMENT_JUDGE_USERNAME_EXAMPLE,
    )
    var qaJudgeUserName: String?,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.QA_REPORTER_COMPANIES_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.QA_REPORTER_COMPANIES_EXAMPLE,
    )
    var qaReporters: List<QaReporter>,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.DATA_POINTS_MAP_DESCRIPTION,
    )
    var dataPoints: Map<String, DataPointJudgement>,
)
