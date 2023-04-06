package org.dataland.documentmanager.model

import org.dataland.datalandbackendutils.model.QAStatus

/**
 * --- API model ---
 * Holds the meta info of a document
 * @param [documentId] document ID
 * @param [displayTitle] title displayed
 * @param [uploaderId] uploader ID
 * @param [uploadTime] timestamp of the upload
 * @param [qaStatus] qa status of the document
 */
data class DocumentMetaInfo(
    val documentId: String,
    val displayTitle: String,
    val uploaderId: String,
    val uploadTime: Long,
    var qaStatus: QAStatus,
)
