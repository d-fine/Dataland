package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataUploadApi
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataExportStore
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.http.ResponseEntity
import java.time.Instant

/**
 * Implementation of the controller for data exchange of an abstract type T that additionally supports dataset uploads
 * @param datasetStorageService service to handle data storage
 * @param dataMetaInformationManager service for retrieving data meta-information
 * @param dataExportService service for handling data export to CSV and JSON
 * @param companyQueryManager service to retrieve company information
 */
open class UploadableDataController<T>(
    datasetStorageService: DatasetStorageService,
    dataMetaInformationManager: DataMetaInformationManager,
    dataExportService: DataExportService<T>,
    dataExportStorage: DataExportStore,
    companyQueryManager: CompanyQueryManager,
    clazz: Class<out T>,
) : DataController<T>(
        datasetStorageService,
        dataMetaInformationManager,
        dataExportService,
        dataExportStorage,
        companyQueryManager,
        clazz,
    ),
    DataUploadApi<T> {
    override fun postCompanyAssociatedData(
        companyAssociatedData: CompanyAssociatedData<T>,
        bypassQa: Boolean,
    ): ResponseEntity<DataMetaInformation> {
        val companyId = companyAssociatedData.companyId
        val reportingPeriod = companyAssociatedData.reportingPeriod
        val userId = DatalandAuthentication.fromContext().userId
        logger.info(logMessageBuilder.postCompanyAssociatedDataMessage(userId, dataType, companyId, reportingPeriod))

        val uploadTime = Instant.now().toEpochMilli()
        val datasetToStore = buildStorableDataset(companyAssociatedData, userId, uploadTime)
        val correlationId = IdUtils.generateCorrelationId(companyId = companyAssociatedData.companyId, dataId = null)
        val dataIdOfPostedData = datasetStorageService.storeDataset(datasetToStore, bypassQa, correlationId)

        logger.info(logMessageBuilder.postCompanyAssociatedDataSuccessMessage(companyId, correlationId))

        return ResponseEntity.ok(
            DataMetaInformation(
                dataId = dataIdOfPostedData, companyId = companyId, dataType = dataType,
                uploaderUserId = userId, uploadTime = uploadTime, reportingPeriod = reportingPeriod,
                currentlyActive = false, qaStatus = QaStatus.Pending,
            ),
        )
    }

    private fun buildStorableDataset(
        companyAssociatedData: CompanyAssociatedData<T>,
        userId: String,
        uploadTime: Long,
    ): StorableDataset =
        StorableDataset(
            companyId = companyAssociatedData.companyId,
            dataType = dataType,
            uploaderUserId = userId,
            uploadTime = uploadTime,
            reportingPeriod = companyAssociatedData.reportingPeriod,
            data = defaultObjectMapper.writeValueAsString(companyAssociatedData.data),
        )
}
