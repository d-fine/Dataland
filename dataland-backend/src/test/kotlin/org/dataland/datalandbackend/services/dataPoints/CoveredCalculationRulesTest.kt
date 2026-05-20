package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Test

class CoveredCalculationRulesTest {
    @Test
    fun `check that all calculation rules specified in the framework toolbox are also implemented`() {
        val specificationControllerApi = SpecificationControllerApi()
        val specificationService = SpecificationService(specificationControllerApi)
        val dataCompositionService = DataCompositionService(specificationService)

        val frameworkIds = specificationControllerApi.listFrameworkSpecifications().map { it.framework.id }
        val dataPointTypes = frameworkIds.flatMap { dataCompositionService.getRelevantDataPointTypes(it) }.distinct()
        val specs = specificationService.getDataPointSpecifications(dataPointTypes)

        // get all calculation rules specified in the framework toolbox
        val specifiedCalculationRules = specs.values.flatMap { it.calculationRules.orEmpty() }.distinct()

        // Collect unimplemented rules
        val unimplementedRules =
            specifiedCalculationRules.filter { rule ->
                try {
                    DataPointConversion.byId(rule.calculationMethod)
                    false
                } catch (_: IllegalArgumentException) {
                    true
                }
            }

        // Assert that there are no unimplemented calculation rules
        assertTrue(
            unimplementedRules.isEmpty(),
            "The following calculation rules are specified in the framework toolbox but not implemented:" +
                " ${unimplementedRules.joinToString(", ")}",
        )
    }
}
