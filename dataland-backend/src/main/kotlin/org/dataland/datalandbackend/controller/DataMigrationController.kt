package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataMigrationApi
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.datapoints.AssembledDataMigrationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the DataMigrationApi
 */
@RestController
class DataMigrationController
    @Autowired
    constructor(
        private val migrationManager: AssembledDataMigrationManager,
        private val dataExportService: DataExportService,
        private val dataManager: DataManager,
        private val metaDataManager: DataMetaInformationManager,
        private val objectMapper: ObjectMapper,
    ) : DataMigrationApi {
        override fun migrateStoredDatasetToAssembledDataset(dataId: String) {
            migrationManager.migrateStoredDatasetToAssembledDataset(dataId)
        }

        override fun forceUploadDatasetAsStoredDataset(
            dataType: DataType,
            companyAssociatedData: CompanyAssociatedData<JsonNode>,
            bypassQa: Boolean,
        ): ResponseEntity<DataMetaInformation> {
            val dataTypeClass = getClassForDataType(dataType)
            val data = objectMapper.treeToValue(companyAssociatedData.data, dataTypeClass)
            val controller =
                object : DataController<Any>(
                    dataManager,
                    metaDataManager,
                    dataExportService,
                    objectMapper,
                    dataTypeClass as Class<Any>,
                ) {}
            return controller
                .postCompanyAssociatedData(CompanyAssociatedData(companyAssociatedData.companyId, companyAssociatedData.reportingPeriod, data), bypassQa)
        }

        private fun getClassForDataType(dataType: DataType): Class<*> {
            val provider = ClassPathScanningCandidateComponentProvider(false)
            provider.addIncludeFilter(AnnotationTypeFilter(org.dataland.datalandbackend.annotations.DataType::class.java))
            val modelBeans = provider.findCandidateComponents("org.dataland.datalandbackend")
            val matchingClass =
                modelBeans
                    .map { Class.forName(it.beanClassName) }
                    .firstOrNull { it.getAnnotation(org.dataland.datalandbackend.annotations.DataType::class.java).name == dataType.name }
                    ?: throw IllegalArgumentException("No class found for data type ${dataType.name}")

            return matchingClass
        }
    }
