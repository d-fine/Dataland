package org.dataland.datasourcingservice.entities

import jakarta.persistence.Embeddable
import java.time.LocalDate

/**
 * Represents the expected publication date of a document for a specific document category.
 *
 * @property documentCategory The category of the document (e.g., "Annual Report", "Sustainability Report").
 * @property expectedPublicationDate The expected publication date of the document.
 */
@Embeddable
data class ExpectedPublicationDateOfDocument(
    val documentCategory: String,
    val expectedPublicationDate: LocalDate,
)
