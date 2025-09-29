package org.dataland.datasourcingservice.model.datasourcing

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.time.LocalDate

/**
 * A simplified version of StoredDataSourcing without fields that are lazily fetched
 * in DataSourcingEntity.
 */
data class DataSourcingWithoutReferences(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
    )
    val dataSourcingId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
    )
    val dataType: String,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.STATE_DESCRIPTION,
    )
    val state: DataSourcingState,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATE_OF_NEXT_DOCUMENT_SOURCING_ATTEMPT_DESCRIPTION,
    )
    val dateOfNextDocumentSourcingAttempt: LocalDate? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_COLLECTOR_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_COLLECTOR_EXAMPLE,
    )
    val documentCollector: String? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_EXTRACTOR_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_EXTRACTOR_EXAMPLE,
    )
    val dataExtractor: String? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
    )
    val adminComment: String? = null,
)
