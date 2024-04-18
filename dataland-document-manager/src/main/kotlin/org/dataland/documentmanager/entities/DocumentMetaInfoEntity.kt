package org.dataland.documentmanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.documentmanager.model.DocumentMetaInfo
import org.dataland.documentmanager.model.DocumentType

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
    val uploaderId: String,
    val uploadTime: Long,
    @Enumerated(EnumType.STRING)
    var qaStatus: QaStatus,
) {
    constructor(documentMetaInfo: DocumentMetaInfo) :
        this(
            documentId = documentMetaInfo.documentId,
            documentType = documentMetaInfo.documentType,
            uploaderId = documentMetaInfo.uploaderId,
            uploadTime = documentMetaInfo.uploadTime,
            qaStatus = documentMetaInfo.qaStatus,
        )
}
