package org.dataland.datasourcingservice.model.datasourcing

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.entities.ExpectedPublicationDateDocument
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.time.LocalDate

/**
 * DTO for transferring stored data sourcing information. In contrast to DataSourcingEntity,
 * it points to a set of StoredDataRequest instead of RequestEntity to avoid circular references.
 */
data class StoredDataSourcing(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.ID_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.ID_EXAMPLE,
    )
    val id: String,
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
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_IDS_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.DOCUMENT_IDS_EXAMPLE,
            ),
    )
    val documentIds: Set<String> = emptySet(),
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = ExpectedPublicationDateDocument::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.EXPECTED_PUBLICATION_DATES_DESCRIPTION,
            ),
    )
    val expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateDocument> = emptySet(),
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATE_DOCUMENT_SOURCING_ATTEMPT_DESCRIPTION,
    )
    val dateDocumentSourcingAttempt: LocalDate? = null,
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
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DataSourcingOpenApiDescriptionsAndExamples.ASSOCIATED_REQUEST_IDS_DESCRIPTION,
            ),
    )
    val associatedRequestIds: Set<String> = emptySet(),
)
