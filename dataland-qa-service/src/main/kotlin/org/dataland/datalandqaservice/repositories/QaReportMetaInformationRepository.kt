package org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportMetaInformationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
interface QaReportMetaInformationRepository : JpaRepository<QaReportMetaInformationEntity, String> {
    @Query(
        "SELECT qaReportMetaInformation FROM QaReportMetaInformationEntity qaReportMetaInformation " +
            "WHERE qaReportMetaInformation.dataId = :dataId " +
            "AND (:reporterUserId IS NULL OR qaReportMetaInformation.reporterUserId = :reporterUserId)",
    )
    fun searchQaReportMetaInformation(
        @Param("dataId") dataId: String,
        @Param("reporterUserId") reporterUserId: String?,
    ): List<QaReportMetaInformationEntity>
}
