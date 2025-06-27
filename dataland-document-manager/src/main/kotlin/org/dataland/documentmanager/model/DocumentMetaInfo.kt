package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.model.DocumentCategory
import java.time.LocalDate

/**
 * --- API model ---
 * Holds the meta info of a document uploaded with document
 * @param documentName
 * @param documentCategory
 * @param companyIds
 * @param publicationDate
 * @param reportingPeriod only for informative purposes
 */
data class DocumentMetaInfo(
    @field:JsonProperty(required = true)
    @field:Schema(
        description =
            "Name under which the document is saved on Dataland. " +
                "Does not need to coincide with the name of the uploaded file, " +
                "nor include the file type suffix (such as '.pdf').",
        example = "\"Company_X_Annual_Report_2024\"",
    )
    override val documentName: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = "The Dataland document category to which the document belongs.",
        example = "\"AnnualReport\"",
    )
    override val documentCategory: DocumentCategory,
    @field:JsonProperty(required = true)
    @field:Schema(
        description =
            "The set of Dataland company IDs of the companies using this document " +
                "as a referenced report.",
        example = "[\n\t\"72c5cbdc-4244-49dd-8368-be4e64b399ae\",\n\t\"a31733e0-42ed-47c9-9909-e1d2ecf08083\"\n]",
    )
    override val companyIds: Set<String>,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    @field:Schema(
        description =
            "The date on which this document was published by the responsible company, specified in format " +
                "'yyyy-MM-dd'.",
        example = "\"2024-02-13\"",
    )
    override val publicationDate: LocalDate?,
    @field:Schema(
        description = "The reporting period, specified as a year number, for which this document provides information.",
        example = "\"2023\"",
    )
    override val reportingPeriod: String?,
) : BasicDocumentMetaInfo
