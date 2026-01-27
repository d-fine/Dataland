package org.dataland.datasourcingservice.model.mixed

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.time.LocalDate

/**
 * DTO for transferring data sourcing details associated with a request.
 */
data class DataSourcingDetails(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_ID_EXAMPLE,
    )
    val dataSourcingEntityId: String? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_STATE_EXAMPLE,
    )
    val dataSourcingState: DataSourcingState? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATE_OF_NEXT_DOCUMENT_SOURCING_ATTEMPT_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.GENERAL_DATE_EXAMPLE,
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
)
