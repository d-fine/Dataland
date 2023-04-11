package org.dataland.documentmanager.repositories

import org.dataland.documentmanager.entities.DocumentMetaInfoEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the meta information of a document
 */
interface DocumentMetaInfoRepository : JpaRepository<DocumentMetaInfoEntity, String>
