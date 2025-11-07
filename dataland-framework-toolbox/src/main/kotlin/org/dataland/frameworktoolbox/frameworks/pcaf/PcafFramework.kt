package org.dataland.frameworktoolbox.frameworks.pcaf

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.frameworks.sfdr.SfdrComponentGenerationUtils
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the PCAF framework
 */
@Component
class PcafFramework :
    PavedRoadFramework(
        identifier = "pcaf",
        label = "PCAF",
        explanation = "Partnership for Carbon Accounting Financials",
        File("./dataland-framework-toolbox/inputs/pcaf/pcaf.xlsx"),
        order = 6,
        enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
    ) {
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        overwriteDataPointSpecificationForEnums(framework.root)
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils = SfdrComponentGenerationUtils()

    private fun overwriteDataPointSpecificationForEnums(root: ComponentGroupApi) {
        root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("general") {
                edit<SingleSelectComponent>("fiscalYearDeviation") {
                    specificationGenerator = { categoryBuilder ->
                        categoryBuilder.addDefaultDatapointAndSpecification(
                            this,
                            "Enum",
                            "extendedEnumFiscalYearDeviation",
                        )
                    }
                }
            }
            edit<ComponentGroup>("company") {
                edit<SingleSelectComponent>("mainPcafSector") {
                    specificationGenerator = { categoryBuilder ->
                        categoryBuilder.addDefaultDatapointAndSpecification(
                            this,
                            "Enum",
                            "extendedEnumPcafMainSector",
                            dataPointTypeIdOverwrite = "extendedEnumPcafMainSector",
                        )
                    }
                }
                edit<SingleSelectComponent>("companyExchangeStatus") {
                    specificationGenerator = { categoryBuilder ->
                        categoryBuilder.addDefaultDatapointAndSpecification(
                            this,
                            "Enum",
                            "extendedEnumCompanyExchangeStatus",
                            dataPointTypeIdOverwrite = "extendedEnumCompanyExchangeStatus",
                        )
                    }
                }
            }
        }
    }
}
