package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.AssembledDatasetMigrationApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.IdUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * Controller for the DataMigrationApi in the QA Service
 */
@RestController
class AssembledDataMigrationController
    @Autowired
    constructor(
        private val metaDataControllerApi: MetaDataControllerApi,
        private val qaReportRepository: QaReportRepository,
        private val objectMapper: ObjectMapper,
    ) : AssembledDatasetMigrationApi {
        override fun forceUploadStoredQaReport(
            dataId: String,
            data: JsonNode,
        ): ResponseEntity<QaReportMetaInformation> {
            val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
            val storedQaReport =
                qaReportRepository
                    .save(
                        QaReportEntity(
                            qaReportId = IdUtils.generateUUID(),
                            qaReport = objectMapper.writeValueAsString(data),
                            dataId = dataId,
                            dataType = dataMetaInfo.dataType.value,
                            reporterUserId = DatalandAuthentication.fromContext().userId,
                            uploadTime = Instant.now().toEpochMilli(),
                            active = true,
                        ),
                    )
            return ResponseEntity.ok(storedQaReport.toMetaInformationApiModel())
        }
    }
