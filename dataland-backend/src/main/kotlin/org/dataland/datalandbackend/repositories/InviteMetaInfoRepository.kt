package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing meta info about invites
 */
interface InviteMetaInfoRepository : JpaRepository<InviteMetaInfoEntity, String>
