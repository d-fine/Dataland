package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DataMetaInformationRepository : JpaRepository<DataMetaInformationEntity, String> {
    fun getByCompanyCompanyId(companyId: String): List<DataMetaInformationEntity>
    fun getByCompanyCompanyIdAndDataType(companyId: String, dataType: String): List<DataMetaInformationEntity>
}
