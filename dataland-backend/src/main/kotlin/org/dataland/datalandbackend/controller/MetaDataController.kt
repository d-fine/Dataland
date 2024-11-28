package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.MetaDataApi
import org.dataland.datalandbackend.entities.NonSourceableEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.repositories.NonSourceableDataRepository
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.NonSourceableDataManager
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the company metadata endpoints
 * @param dataMetaInformationManager service for handling data meta information
 * @param logMessageBuilder a helper for building log messages
 * @param nonSourceableDataManager service for handling information on data sets and theirs sourceability
 * @param nonSourceableDataRepository repository for meta info on sourceability of data sets
 */

@RestController
class MetaDataController(
    @Autowired var dataMetaInformationManager: DataMetaInformationManager,
    @Autowired val logMessageBuilder: LogMessageBuilder,
    @Autowired val nonSourceableDataManager: NonSourceableDataManager,
    @Autowired val nonSourceableDataRepository: NonSourceableDataRepository,
) : MetaDataApi {
    override fun getListOfDataMetaInfo(
        companyId: String?,
        dataType: DataType?,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
        uploaderUserIds: Set<UUID>?,
        qaStatus: QaStatus?,
    ): ResponseEntity<List<DataMetaInformation>> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        return ResponseEntity.ok(
            dataMetaInformationManager
                .searchDataMetaInfo(
                    companyId,
                    dataType,
                    showOnlyActive,
                    reportingPeriod,
                    uploaderUserIds,
                    qaStatus,
                ).filter { it.isDatasetViewableByUser(currentUser) }
                .map { it.toApiModel(currentUser) },
        )
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<DataMetaInformation> {
        val currentUser = DatalandAuthentication.fromContextOrNull()
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(
                logMessageBuilder.generateAccessDeniedExceptionMessage(
                    metaInfo.qaStatus,
                ),
            )
        }
        return ResponseEntity.ok(metaInfo.toApiModel(currentUser))
    }

    override fun getInfoOnSourceabilityOfDataSets(
        eventId: UUID?,
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        nonSourceable: Boolean?,
    ): ResponseEntity<List<NonSourceableEntity>> =
        ResponseEntity.ok(
            nonSourceableDataManager
                .getNonSourceableDataByFilters(
                    eventId,
                    companyId,
                    dataType,
                    reportingPeriod,
                    nonSourceable,
                ),
        )

    override fun postSourceabilityOfADataSet(nonSourceableInfo: NonSourceableInfo) {
        nonSourceableDataManager.createEventDatasetNonSourceable(nonSourceableInfo)
    }

    override fun isDataNonSourceable(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ) {
        nonSourceableDataManager.responseDataNonSourceable(
            companyId,
            dataType,
            reportingPeriod,
        )
    }
}
