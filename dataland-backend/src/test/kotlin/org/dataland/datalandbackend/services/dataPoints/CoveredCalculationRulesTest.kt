package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.junit.jupiter.api.Test

class CoveredCalculationRulesTest {
    private val specificationControllerApi = SpecificationControllerApi()
    private val specificationService = SpecificationService(specificationControllerApi)
    private val dataCompositionService = DataCompositionService(specificationService)

    private val frameworkIds = specificationControllerApi.listFrameworkSpecifications().map { it.framework.id }

    private val dataPointTypes = frameworkIds.flatMap { dataCompositionService.getRelevantDataPointTypes(it) }.distinct()

    private val specs = specificationService.getDataPointSpecifications(dataPointTypes)

    @Test
    fun `check that all calculation rules specified in the framework toolbox are also implemented`() {
    }
}
