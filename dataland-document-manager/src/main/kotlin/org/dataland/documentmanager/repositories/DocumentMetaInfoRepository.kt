package org.dataland.documentmanager.repositories

import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the meta information of a document
 */
interface DocumentMetaInfoRepository : JpaRepository<DocumentMetaInfoEntity, String> {
    /**
     * Retrieve function based on documentId
     */
    fun getByDocumentId(documentId: String): DocumentMetaInfoEntity?

    /**
     * Retrieve database entries based on companyId, documentCategory and reportingPeriod.
     * Search results are ordered by publication date in reverse chronological order, and
     * only at most limit many results are returned, skipping the first offset many.
     */
    @Query(
        "SELECT d FROM DocumentMetaInfoEntity d WHERE (:companyId is null or :companyId MEMBER OF d.companyIds) and " +
            "(:documentCategory is null or d.documentCategory = :documentCategory) and " +
            "(:reportingPeriod is null or d.reportingPeriod = :reportingPeriod) and " +
            "d.qaStatus = 'Accepted' ORDER BY d.publicationDate DESC " +
            "LIMIT :limit OFFSET :offset",
    )
    fun findByCompanyIdAndDocumentCategoryAndReportingPeriod(
        @Param("companyId") companyId: String? = null,
        @Param("documentCategory") documentCategory: DocumentCategory? = null,
        @Param("reportingPeriod") reportingPeriod: String? = null,
        @Param("limit") limit: Int = 100,
        @Param("offset") offset: Int = 0,
    ): List<DocumentMetaInfoEntity>
}
