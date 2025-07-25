package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DocumentManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import java.time.LocalDate

/**
 * --- API model ---
 * Holds the meta info of a document uploaded with document
 * @param documentName
 * @param documentCategory
 * @param companyIds
 * @param publicationDate
 * @param reportingPeriod only for informative purposes
 */
data class DocumentMetaInfo(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_NAME_DESCRIPTION,
        example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_NAME_EXAMPLE,
    )
    override val documentName: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_CATEGORY_DESCRIPTION,
    )
    override val documentCategory: DocumentCategory,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DocumentManagerOpenApiDescriptionsAndExamples.COMPANY_IDS_DESCRIPTION,
                example = DocumentManagerOpenApiDescriptionsAndExamples.COMPANY_IDS_EXAMPLE,
            ),
    )
    override val companyIds: Set<String>,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    @field:Schema(
        description = DocumentManagerOpenApiDescriptionsAndExamples.PUBLICATION_DATE_DESCRIPTION,
        example = DocumentManagerOpenApiDescriptionsAndExamples.PUBLICATION_DATE_EXAMPLE,
    )
    override val publicationDate: LocalDate?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    override val reportingPeriod: String?,
) : BasicDocumentMetaInfo
