package org.dataland.frameworktoolbox.frameworks.vsme

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.frameworks.vsme.custom.DependsOnComponentCustomValue
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the VSME framework
 */
@Component
class VsmeFramework :
    PavedRoadFramework(
        identifier = "vsme",
        label = "VSME",
        explanation = "Voluntary small and medium-sized enterprises questionnaire",
        File("./dataland-framework-toolbox/inputs/vsme/vsme.xlsx"),
        order = 7,
        isPrivateFramework = true,
        enabledFeatures = FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.QaModel),
    ) {
    override fun getComponentGenerationUtils(): ComponentGenerationUtils = VsmeComponentGenerationUtils()

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        val fieldsToAddDependency =
            arrayOf(
                "grossPayMaleInEuro", "grossPayFemaleInEuro", "totalWorkHoursMale",
                "totalWorkHoursFemale", "averageWorkHoursMale", "averageWorkHoursFemale",
                "averageHourlyPayMaleInEuroPerHour",
                "averageHourlyPayFemaleInEuroPerHour", "payGap",
            )

        framework.root.edit<ComponentGroup>("basic") {
            val numberOfEmployeesInHeadCount =
                this
                    .get<ComponentGroup>("workforceGeneralCharacteristics")
                    .get<NumberBaseComponent>("numberOfEmployeesInHeadcount")
            val numberEmployeesFullTime =
                this
                    .get<ComponentGroup>("workforceGeneralCharacteristics")
                    .get<NumberBaseComponent>("numberOfEmployeesInFte")
            edit<ComponentGroup>("workforceRenumerationCollectiveBargainingAndTraining") {
                edit<SingleSelectComponent>("payGapBasis") {
                    setDependency(this, numberOfEmployeesInHeadCount, numberEmployeesFullTime)
                }
                fieldsToAddDependency.forEach { fieldIdentifier ->
                    edit<NumberBaseComponent>(fieldIdentifier) {
                        setDependency(this, numberOfEmployeesInHeadCount, numberEmployeesFullTime)
                    }
                }
            }
        }
    }

    private fun setDependency(
        component: ComponentBase,
        firstDependencyComponent2: NumberBaseComponent,
        secondDependencyComponent: NumberBaseComponent,
    ) {
        component.availableIf =
            DependsOnComponentCustomValue(
                firstDependencyComponent2,
                secondDependencyComponent,
            )
    }
}
