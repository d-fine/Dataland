package org.dataland.frameworktoolbox.frameworks.sme

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the SME framework
 */
@Component
class SmeFramework : InDevelopmentPavedRoadFramework(
    identifier = "sme",
    label = "SME",
    explanation = "Small and medium-sized enterprises questionnaire",
    File("./dataland-framework-toolbox/inputs/sme/sme.xlsx"),
    order = 6,
    isPrivateFramework = true,
) {
    /*
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("basic") {
            edit<ComponentGroup>("workforceRenumerationCollectiveBargainingAndTraining") {

                edit<SingleSelectComponent>("payGapBasis") {
                    setDependencies(this)
                }
            }
        }
    }

   private fun configureComponentGroupColorsAndExpansion(root: ComponentGroupApi) {
       root.edit<ComponentGroup>("basic") {
           viewPageExpandOnPageLoad = true
           uploadPageLabelBadgeColor = LabelBadgeColor.Orange
           viewPageLabelBadgeColor = LabelBadgeColor.Orange
       }
   }
    private fun setDependencies(component: SingleSelectComponent) {
        component.availableIf= DependsOnComponentCustomNumericValue(component,"test", null)

    }
     */
}
