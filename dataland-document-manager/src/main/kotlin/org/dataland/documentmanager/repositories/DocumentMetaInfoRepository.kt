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
     */
    @Query(
        "SELECT d FROM DocumentMetaInfoEntity d WHERE (:companyId is null or :companyId MEMBER OF d.companyIds) and " +
            "(:documentCategory is null or d.documentCategory = :documentCategory) and " +
            "(:reportingPeriod is null or d.reportingPeriod = :reportingPeriod) and " +
            "d.qaStatus = 'Accepted' ORDER BY d.publicationDate DESC ",
    )
    fun findByCompanyIdAndDocumentCategoryAndReportingPeriodUnlimited(
        @Param("companyId") companyId: String? = null,
        @Param("documentCategory") documentCategory: DocumentCategory? = null,
        @Param("reportingPeriod") reportingPeriod: String? = null,
    ): List<DocumentMetaInfoEntity>

    /**
     * Retrieve database entries based on companyId, documentCategory and reportingPeriod.
     * This variant allows the specification of a limit and offset for improved efficiency
     * when only a part of the search results is needed.
     */
    @Query(
        "SELECT d FROM DocumentMetaInfoEntity d WHERE (:companyId is null or :companyId MEMBER OF d.companyIds) and " +
            "(:documentCategory is null or d.documentCategory = :documentCategory) and " +
            "(:reportingPeriod is null or d.reportingPeriod = :reportingPeriod) and " +
            "d.qaStatus = 'Accepted' ORDER BY d.publicationDate DESC " +
            "LIMIT :limit OFFSET :offset",
    )
    fun findByCompanyIdAndDocumentCategoryAndReportingPeriodLimited(
        @Param("companyId") companyId: String? = null,
        @Param("documentCategory") documentCategory: DocumentCategory? = null,
        @Param("reportingPeriod") reportingPeriod: String? = null,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<DocumentMetaInfoEntity>
}
