package org.dataland.frameworktoolbox.frameworks.lksg

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.MultiSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.springframework.stereotype.Component
import java.io.File

/**
 * The Lksg Framework
 */
@Component
class LksgFramework : InDevelopmentPavedRoadFramework(
    identifier = "lksg",
    label = "LkSG",
    explanation = "Lieferkettensorgfaltspflichtengesetz",
    File("./dataland-framework-toolbox/inputs/lksg/lksg.xlsx"),
    order = 3,
) {

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setSectionColorsAndExpansion(framework.root)
        framework.root.getOrNull<ComponentGroup>("general")
            ?.getOrNull<ComponentGroup>("masterData")?.let { parent ->
                editShareOfTemporaryWorkersOptions(parent)
            }
        val governanceComponent = framework.root.getOrNull<ComponentGroup>("governance")
        governanceComponent?.getOrNull<ComponentGroup>("riskManagementOwnOperations")?.let { parent ->
            writeLksgRiskPositions(parent, "identifiedRisks")
        }
        governanceComponent?.getOrNull<ComponentGroup>("grievanceMechanismOwnOperations")?.let { parent ->
            writeLksgRiskPositions(parent, "complaintsRiskPosition")
        }
        governanceComponent?.getOrNull<ComponentGroup>("generalViolations")?.let { parent ->
            writeLksgRiskPositions(parent, "humanRightsOrEnvironmentalViolationsDefinition")
        }
    }

    private fun writeLksgRiskPositions(parentComponent: ComponentGroup, fieldName: String) {
        val optionsSet = mutableSetOf(
            SelectionOption("ChildLabor", "Child labor"),
            SelectionOption("ForcedLabor", "Forced Labor"),
            SelectionOption("Slavery", "Slavery"),
            SelectionOption(
                "DisregardForOccupationalHealthOrSafety",
                "Disregard for occupational" +
                    " health/safety",
            ),
            SelectionOption(
                "DisregardForFreedomOfAssociation",
                "Disregard for freedom of " +
                    "association",
            ),
            SelectionOption("UnequalTreatmentOfEmployment", "Unequal treatment of employment"),
            SelectionOption("WithholdingAdequateWages", "Withholding adequate wages"),
            SelectionOption(
                "ContaminationOfSoilWaterAirOrNoiseEmissionsOrExcessiveWaterConsumption",
                "Contamination of soil/water/air, noise emissions, excessive water consumption",
            ),
            SelectionOption(
                "UnlawfulEvictionOrDeprivationOfLandOrForestAndWater",
                "Unlawful eviction/deprivation of land, forest and water",
            ),
        )

        parentComponent.edit<MultiSelectComponent>(fieldName) {
            options = addFurtherSelectionOptions(optionsSet)
        }
    }

    private fun addFurtherSelectionOptions(optionsSet: MutableSet<SelectionOption>): MutableSet<SelectionOption> {
        optionsSet.add(
            SelectionOption(
                "UseOfPrivatePublicSecurityForcesWithDisregardForHumanRights",
                "Use of private/public security forces with disregard for human rights",
            ),
        )
        optionsSet.add(
            SelectionOption(
                "UseOfMercuryOrMercuryWaste",
                "Use of mercury, mercury waste (Minamata Convention)",
            ),
        )
        optionsSet.add(
            SelectionOption(
                "ProductionAndUseOfPersistentOrganicPollutants",
                "Production and use of persistent organic pollutants (POPs Convention)",
            ),
        )
        optionsSet.add(
            SelectionOption(
                "ExportImportOfHazardousWaste",
                "Export/import of hazardous waste (Basel Convention)",
            ),
        )
        return optionsSet
    }

    private fun editShareOfTemporaryWorkersOptions(parent: ComponentGroup) {
        parent.edit<SingleSelectComponent>("shareOfTemporaryWorkers") {
            options = mutableSetOf(
                SelectionOption("Smaller10", "<10%"),
                SelectionOption("Between10And25", "10-25%"),
                SelectionOption("Between25And50", "25-50%"),
                SelectionOption("Greater50", ">50%"),
            )
        }
    }

    private fun setSectionColorsAndExpansion(root: ComponentGroupApi) {
        root.edit<ComponentGroup>("general") {
            viewPageLabelBadgeColor = LabelBadgeColor.Orange
            viewPageExpandOnPageLoad = true

            uploadPageLabelBadgeColor = LabelBadgeColor.Orange
            edit<ComponentGroup>("masterData") {
                viewPageExpandOnPageLoad = true
            }
        }
    }
}
