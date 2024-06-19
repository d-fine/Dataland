package org.dataland.frameworktoolbox.frameworks.vsme

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.frameworks.vsme.custom.DependsOnComponentCustomValue
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the VSME framework
 */
@Component
class VsmeFramework : InDevelopmentPavedRoadFramework(
    identifier = "vsme",
    label = "VSME",
    explanation = "Voluntary small and medium-sized enterprises questionnaire",
    File("./dataland-framework-toolbox/inputs/vsme/vsme.xlsx"),
    order = 6,
    isPrivateFramework = true,
) {
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        val fieldsToAddDependency = arrayOf(
            "grossPayMale", "grossPayFemale", "totalWorkHoursMale",
            "totalWorkHoursFemale", "averageWorkHoursMale", "averageWorkHoursFemale", "averageHourlyPayMale",
            "averageHourlyPayFemale", "payGap",
        )

        framework.root.edit<ComponentGroup>("basic") {
            val numberOfEmployeesInHeadCount = this.get<ComponentGroup>("workforceGeneralCharacteristics")
                .get<NumberBaseComponent>("numberOfEmployeesInHeadcount")
            val numberEmployeesFullTime = this.get<ComponentGroup>("workforceGeneralCharacteristics")
                .get<NumberBaseComponent>("numberOfEmployeesInFtes")
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
        component.availableIf = DependsOnComponentCustomValue(
            firstDependencyComponent2, ">=150", // TODO wenn sowieso immer ">=150", warum dann Variable?
            secondDependencyComponent,
        )
    }
}
