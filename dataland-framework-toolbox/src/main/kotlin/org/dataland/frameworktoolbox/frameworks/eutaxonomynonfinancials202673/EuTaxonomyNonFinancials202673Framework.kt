package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials202673

import org.dataland.frameworktoolbox.frameworks.EuTaxonomyNonFinancialsBaseFramework
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyNonFinancialsFrameworkConfig
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Eu Taxonomy Non-Financials framework
 */
@Component
class EuTaxonomyNonFinancials202673Framework :
    EuTaxonomyNonFinancialsBaseFramework(
        EuTaxonomyNonFinancialsFrameworkConfig(
            identifier = "eutaxonomy-non-financials-2026-73",
            label = "EU Taxonomy Non-Financials (2026/73)",
            explanation = "The EU-Taxonomy framework for non-financial companies as of Regulation (EU) 2026/73",
            frameworkTemplateCsvFile =
                File(
                    "./dataland-framework-toolbox/inputs/eutaxonomy-non-financials-2026-73/" +
                        "eutaxonomy-non-financials-2026-73.xlsx",
                ),
            order = 5,
        ),
        enabledFeatures = FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.UploadPage),
    )
