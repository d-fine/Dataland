package org.dataland.documentmanager.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException

/**
 * Specific ResourceNotFoundApiException thrown if a document cannot be retrieved.
 */
class DocumentNotFoundException(
    documentId: String,
    correlationId: String,
) : ResourceNotFoundApiException(
        summary = "Document with documentId $documentId or corresponding meta info does not exist.",
        message = "Document with documentID $documentId or corresponding meta info does not exist. Correlation ID: $correlationId.",
    )
