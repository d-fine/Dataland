package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Config to manage two types of export service.
 * Necessary since Springs wiring is static, datasetStorageService cannot be specified dynamically.
 */
@Configuration
class DataExportConfig(
    private val datasetAssembler: DatasetAssembler,
    private val specificationApi: SpecificationControllerApi,
    private val companyQueryManager: CompanyQueryManager,
) {
    /**
     * DataExportService for assembled frameworks which should be most frameworks
     */
    @Bean
    @Qualifier("AssembledExportService")
    fun assembledExportService(
        @Qualifier("AssembledDataManager") datasetStorageService: DatasetStorageService,
    ): DataExportService<*> = createExportService(datasetStorageService)

    /**
     * DataExportService for unassembled frameworks e.g. lksg
     */
    @Bean
    @Qualifier("UnassembledExportService")
    fun unassembledExportService(
        @Qualifier("DataManager") datasetStorageService: DatasetStorageService,
    ): DataExportService<*> = createExportService(datasetStorageService)

    private fun createExportService(datasetStorageService: DatasetStorageService): DataExportService<Any> =
        DataExportService(
            datasetAssembler,
            specificationApi,
            companyQueryManager,
            datasetStorageService,
        )
}
