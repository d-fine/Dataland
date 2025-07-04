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
import org.dataland.documentmanager.model.DocumentMetaInfoResponse
import org.dataland.documentmanager.utils.OpenApiDescriptionsAndExamples
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
        description = OpenApiDescriptionsAndExamples.DOCUMENT_ID_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.DOCUMENT_ID_EXAMPLE,
    )
    val documentId: String,
    @Enumerated(EnumType.STRING)
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.DOCUMENT_TYPE_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.DOCUMENT_TYPE_EXAMPLE,
    )
    val documentType: DocumentType,
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.DOCUMENT_NAME_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.DOCUMENT_NAME_EXAMPLE,
    )
    var documentName: String?,
    @Convert(converter = DocumentCategoryConverter::class)
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.DOCUMENT_CATEGORY_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.DOCUMENT_CATEGORY_EXAMPLE,
    )
    var documentCategory: DocumentCategory?,
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "company_ids")
    @OrderBy("asc")
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = OpenApiDescriptionsAndExamples.COMPANY_IDS_DESCRIPTION,
                example = OpenApiDescriptionsAndExamples.COMPANY_IDS_EXAMPLE,
            ),
    )
    val companyIds: MutableSet<String> = mutableSetOf(),
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.UPLOADER_ID_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.UPLOADER_ID_EXAMPLE,
    )
    val uploaderId: String,
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.UPLOAD_TIME_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.UPLOAD_TIME_EXAMPLE,
    )
    val uploadTime: Long,
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.PUBLICATION_DATE_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.PUBLICATION_DATE_EXAMPLE,
    )
    var publicationDate: LocalDate?,
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    var reportingPeriod: String?,
    @Enumerated(EnumType.STRING)
    @field:Schema(
        description = OpenApiDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
        example = OpenApiDescriptionsAndExamples.QA_STATUS_EXAMPLE,
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
