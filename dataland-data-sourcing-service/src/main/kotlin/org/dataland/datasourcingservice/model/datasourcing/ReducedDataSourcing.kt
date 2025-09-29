package org.dataland.datasourcingservice.model.datasourcing

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.entities.ExpectedPublicationDateOfDocument
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.time.LocalDate

/**
 * DTO for transferring a reduced set of stored data sourcing information. This is used to provide data to users
 * with limited permissions, omitting sensitive fields such as documentCollector, dataExtractor, adminComment,
 * and associatedRequestIds.
 */
data class ReducedDataSourcing(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
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
                implementation = ExpectedPublicationDateOfDocument::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.EXPECTED_PUBLICATION_DATES_DESCRIPTION,
            ),
    )
    val expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateOfDocument> = emptySet(),
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATE_OF_NEXT_DOCUMENT_SOURCING_ATTEMPT_DESCRIPTION,
    )
    val dateOfNextDocumentSourcingAttempt: LocalDate? = null,
)
