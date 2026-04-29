package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials

import org.dataland.frameworktoolbox.frameworks.EuTaxonomyNonFinancialsBaseFramework
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyNonFinancialsFrameworkConfig
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Eu Taxonomy Non-Financials framework
 */
@Component
class EuTaxonomyNonFinancialsFramework :
    EuTaxonomyNonFinancialsBaseFramework(
        EuTaxonomyNonFinancialsFrameworkConfig(
            identifier = "eutaxonomy-non-financials",
            label = "EU Taxonomy Non-Financials",
            explanation = "The EU-Taxonomy framework for non-financial companies",
            frameworkTemplateCsvFile =
                File(
                    "./dataland-framework-toolbox/inputs/euTaxonomyNonFinancials/EutaxonomyNonFinancials.xlsx",
                ),
            order = 4,
        ),
        enabledFeatures =
            FrameworkGenerationFeatures
                .allExcept(FrameworkGenerationFeatures.UploadPage, FrameworkGenerationFeatures.ViewPage),
        tooLargeClasses =
            listOf(
                "org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex.EutaxonomyNonFinancialsCapex",
                "org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.revenue.EutaxonomyNonFinancialsRevenue",
            ),
        includeNfrdMandatory = true,
    )
