package org.dataland.documentmanager.model

data class DocumentMetaInfo(
    val documentId: String,
    val displayTitle: String,
    val uploaderId: String,
    val uploadTime: Long,
    val qaStatus: DocumentQAStatus,
)
