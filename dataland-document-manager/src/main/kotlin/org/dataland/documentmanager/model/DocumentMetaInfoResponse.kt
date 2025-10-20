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
 * Class for giving feedback via a response to a user who tries to upload a new document.
 * @param documentId the ID of the uploaded document
 * @param documentName
 * @param documentCategory
 * @param companyIds
 * @param uploaderId
 * @param publicationDate
 * @param reportingPeriod
 */
data class DocumentMetaInfoResponse(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
        example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
    )
    val documentId: String,
    @field:Schema(
        description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_NAME_DESCRIPTION,
        example = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_NAME_EXAMPLE,
    )
    override val documentName: String?,
    @field:Schema(
        description = DocumentManagerOpenApiDescriptionsAndExamples.DOCUMENT_CATEGORY_DESCRIPTION,
    )
    override val documentCategory: DocumentCategory?,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DocumentManagerOpenApiDescriptionsAndExamples.COMPANY_IDS_DESCRIPTION,
                example = DocumentManagerOpenApiDescriptionsAndExamples.COMPANY_IDS_EXAMPLE,
            ),
    )
    override val companyIds: Set<String>?,
    @field:Schema(
        description = DocumentManagerOpenApiDescriptionsAndExamples.UPLOADER_ID_DESCRIPTION,
        example = DocumentManagerOpenApiDescriptionsAndExamples.UPLOADER_ID_EXAMPLE,
    )
    val uploaderId: String,
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
