package org.dataland.documentmanager.entities

import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.converter.DocumentCategoryConverter
import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.documentmanager.model.DocumentUploadResponse

/**
 * The entity storing the document meta info
 */
@Entity
@Table(name = "document_meta_info")
data class DocumentMetaInfoEntity(
    @Id
    val documentId: String,
    @Enumerated(EnumType.STRING)
    val documentType: DocumentType,
    val documentName: String?,
    @Convert(converter = DocumentCategoryConverter::class)
    val documentCategory: DocumentCategory,
    @ElementCollection(fetch = FetchType.EAGER)
    val companyIds: List<String>?,
    val uploaderId: String,
    val uploadTime: Long,
    val publicationDate: String?,
    val reportingPeriod: String?,
    @Enumerated(EnumType.STRING)
    var qaStatus: QaStatus,
) {
    /**
     * convert Entity to Response API Model
     */
    fun toDocumentUploadResponse() =
        DocumentUploadResponse(
            documentId = documentId,
            documentName = documentName,
            documentCategory = documentCategory,
            companyIds = companyIds,
            publicationDate = publicationDate,
            reportingPeriod = reportingPeriod,
        )
}
