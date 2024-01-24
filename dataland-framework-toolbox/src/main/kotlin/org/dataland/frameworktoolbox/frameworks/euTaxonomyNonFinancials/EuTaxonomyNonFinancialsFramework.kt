package org.dataland.frameworktoolbox.frameworks.euTaxonomyNonFinancials

import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Eu Taxonomy Non-Financials framework
 */
@Component
class EuTaxonomyNonFinancialsFramework : PavedRoadFramework(
    identifier = "euTaxonomyNonFinancials",
    label = "EU Taxonomy Non-Financials",
    explanation = "Das Eu Taxonomy Non-Financials Framework",
    File("./dataland-framework-toolbox/inputs/dataDictionary-New EU Taxo non-financial toolb.csv"),
) {
    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return EuTaxonomyNonFinancialsComponentGenerationUtils()
    }
}
