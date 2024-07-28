package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportMetaInformationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface QaReportMetaInformationRepository : JpaRepository<QaReportMetaInformationEntity, String>{

}