package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.AssembledDatasetMigrationApi
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.datapoints.AssembledDataMigrationManager
import org.dataland.datalandbackend.services.datapoints.AssembledDataMigrationTrigger
import org.dataland.datalandbackend.services.datapoints.DataControllerProviderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the DataMigrationApi
 */
@RestController
class AssembledDatasetMigrationController
    @Autowired
    constructor(
        private val migrationManager: AssembledDataMigrationManager,
        private val migrationTrigger: AssembledDataMigrationTrigger,
        private val dataControllerProviderService: DataControllerProviderService,
        private val objectMapper: ObjectMapper,
    ) : AssembledDatasetMigrationApi {
        override fun migrateStoredDatasetToAssembledDataset(dataId: String) {
            migrationManager.migrateStoredDatasetToAssembledDataset(dataId)
        }

        override fun triggerMigrationForAllStoredDatasets() {
            migrationTrigger.onApplicationEvent(null)
        }

        override fun forceUploadDatasetAsStoredDataset(
            dataType: DataType,
            companyAssociatedData: CompanyAssociatedData<JsonNode>,
            bypassQa: Boolean,
        ): ResponseEntity<DataMetaInformation> {
            val dataTypeClass = dataControllerProviderService.getClassForDataType(dataType)
            val data = objectMapper.treeToValue(companyAssociatedData.data, dataTypeClass)
            val controller = dataControllerProviderService.getStoredDataControllerForFramework(dataType)
            return controller
                .postCompanyAssociatedData(
                    CompanyAssociatedData(
                        companyAssociatedData.companyId, companyAssociatedData.reportingPeriod, data,
                    ),
                    bypassQa,
                )
        }
    }
