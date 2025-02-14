package org.dataland.documentmanager.entities

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
import java.time.LocalDate

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
    var documentName: String?,
    @Convert(converter = DocumentCategoryConverter::class)
    var documentCategory: DocumentCategory?,
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "company_ids")
    @OrderBy("asc")
    val companyIds: MutableSet<String> = mutableSetOf(),
    val uploaderId: String,
    val uploadTime: Long,
    var publicationDate: LocalDate?,
    var reportingPeriod: String?,
    @Enumerated(EnumType.STRING)
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
            publicationDate = publicationDate,
            reportingPeriod = reportingPeriod,
        )
}
