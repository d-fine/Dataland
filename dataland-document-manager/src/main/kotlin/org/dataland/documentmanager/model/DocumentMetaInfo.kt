package org.dataland.documentmanager.model

import org.dataland.datalandbackendutils.model.QAStatus

/**
 * Holds the meta info of a document
 */
data class DocumentMetaInfo(
    val documentId: String,
    val displayTitle: String,
    val uploaderId: String,
    val uploadTime: Long,
    var qaStatus: QAStatus,
)
