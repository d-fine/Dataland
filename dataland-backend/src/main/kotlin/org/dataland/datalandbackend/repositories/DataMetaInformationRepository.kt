package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DataMetaInformationRepository : JpaRepository<DataMetaInformationEntity, String>