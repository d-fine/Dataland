package org.dataland.frameworktoolbox.frameworks.additionalcompanyinformation

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.specific.qamodel.FrameworkQaModelBuilder
import org.springframework.stereotype.Component
import java.io.File

/**
 * The additional company information framework
 */
@Component
class AdditionalCompanyInformationFramework :
    PavedRoadFramework(
        identifier = "additional-company-information",
        label = "Additional Company Information",
        explanation = "Additional Company Information",
        File("./dataland-framework-toolbox/inputs/additional-company-information/additional-company-information.xlsx"),
        order = 10,
        enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
    ) {
    override fun customizeQaModel(dataModel: FrameworkQaModelBuilder) {
        require(
            dataModel.rootPackageBuilder.childElements
                .find { it.name == "general" && it is PackageBuilder }!!
                .let { it as PackageBuilder }
                .childElements
                .find { it.name == "general" && it is PackageBuilder }!!
                .let { it as PackageBuilder }
                .childElements
                .find { it.name == "AdditionalCompanyInformationGeneralGeneral" && it is DataClassBuilder }!!
                .let { it as DataClassBuilder }
                .properties
                .removeIf { it.name == "referencedReports" },
        ) { "Property 'referencedReports' could not be removed from QA Report." }
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("general") {
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
        }
    }
}
