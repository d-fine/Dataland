package org.dataland.documentmanager.entities

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.converter.DocumentCategoryConverter
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DocumentOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.documentmanager.model.DocumentMetaInfoResponse
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import java.time.LocalDate

/**
 * The entity storing the document meta info
 */
@Entity
@Table(name = "document_meta_info")
data class DocumentMetaInfoEntity(
    @Id
    @field:Schema(
        description = DocumentOpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
        example = DocumentOpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
    )
    val documentId: String,
    @Enumerated(EnumType.STRING)
    @field:Schema(
        description = DocumentOpenApiDescriptionsAndExamples.DOCUMENT_TYPE_DESCRIPTION,
    )
    val documentType: DocumentType,
    @field:Schema(
        description = DocumentOpenApiDescriptionsAndExamples.DOCUMENT_NAME_DESCRIPTION,
        example = DocumentOpenApiDescriptionsAndExamples.DOCUMENT_NAME_EXAMPLE,
    )
    var documentName: String?,
    @Convert(converter = DocumentCategoryConverter::class)
    @field:Schema(
        description = DocumentOpenApiDescriptionsAndExamples.DOCUMENT_CATEGORY_DESCRIPTION,
    )
    var documentCategory: DocumentCategory?,
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "company_ids")
    @OrderBy("asc")
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DocumentOpenApiDescriptionsAndExamples.COMPANY_IDS_DESCRIPTION,
                example = DocumentOpenApiDescriptionsAndExamples.COMPANY_IDS_EXAMPLE,
            ),
    )
    val companyIds: MutableSet<String> = mutableSetOf(),
    @field:Schema(
        description = DocumentOpenApiDescriptionsAndExamples.UPLOADER_ID_DESCRIPTION,
        example = DocumentOpenApiDescriptionsAndExamples.UPLOADER_ID_EXAMPLE,
    )
    val uploaderId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.UPLOAD_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:Schema(
        description = DocumentOpenApiDescriptionsAndExamples.PUBLICATION_DATE_DESCRIPTION,
        example = DocumentOpenApiDescriptionsAndExamples.PUBLICATION_DATE_EXAMPLE,
    )
    var publicationDate: LocalDate?,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    var reportingPeriod: String?,
    @Enumerated(EnumType.STRING)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
    var qaStatus: QaStatus,
) {
    /**
     * convert Entity to Response API Model
     */
    fun toDocumentMetaInfoResponse() =
        DocumentMetaInfoResponse(
            documentId = documentId,
            documentName = documentName,
            documentCategory = documentCategory,
            companyIds = companyIds,
            uploaderId = uploaderId,
            publicationDate = publicationDate,
            reportingPeriod = reportingPeriod,
        )

    /**
     * Check whether user has the right to view this document meta information.
     */
    fun isViewableByUser(): Boolean {
        val viewingUser =
            try {
                DatalandAuthentication.fromContext()
            } catch (_: IllegalArgumentException) {
                null
            }
        return (
            qaStatus == QaStatus.Accepted ||
                viewingUser?.userId == uploaderId ||
                viewingUser?.roles?.contains(DatalandRealmRole.ROLE_ADMIN) ?: false ||
                viewingUser?.roles?.contains(
                    DatalandRealmRole.ROLE_REVIEWER,
                ) ?: false
        )
    }
}
