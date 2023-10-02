package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing meta info about invites
 */
interface InviteMetaInfoRepositoryInterface : JpaRepository<InviteMetaInfoEntity, String>
