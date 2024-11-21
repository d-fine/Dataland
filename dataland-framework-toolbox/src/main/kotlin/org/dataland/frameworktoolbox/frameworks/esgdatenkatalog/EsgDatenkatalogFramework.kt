package org.dataland.frameworktoolbox.frameworks.esgdatenkatalog

import ComponentGenerationUtilsForGermanFrameworks
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Heimathafen framework
 */
@Component
class EsgDatenkatalogFramework :
    PavedRoadFramework(
        identifier = "esg-datenkatalog",
        label = "ESG Datenkatalog für Großunternehmen",
        explanation =
            "Der ESG Datenkatalog für Großunternehmen ist ein ESG-Fragebogen des " +
                "Gesamtverbands der Versicherer und des Bundesverbands Öffentlicher Banken",
        File("./dataland-framework-toolbox/inputs/esg-datenkatalog/esg-datenkatalog.xlsx"),
        order = 8,
        enabledFeatures =
            FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.QaModel, FrameworkGenerationFeatures.DataPointSpecifications),
    ) {
    private fun setGroupsThatAreExpandedOnPageLoad(framework: Framework) {
        framework.root.edit<ComponentGroup>("allgemein") {
            viewPageExpandOnPageLoad = true
        }
        framework.root.edit<ComponentGroup>("allgemein") {
            viewPageExpandOnPageLoad = true
            edit<ComponentGroup>("datum") {
                viewPageExpandOnPageLoad = true
            }
        }
    }

    private fun overwriteFakeFixtureGenerationForDataDate(framework: Framework) {
        framework.root.edit<ComponentGroup>("allgemein") {
            edit<ComponentGroup>("datum") {
                edit<DateComponent>("gueltigkeitsdatum") {
                    fixtureGeneratorGenerator = {
                        it.addAtomicExpression(
                            identifier,
                            "dataGenerator.dataDate",
                        )
                    }
                }
            }
        }
    }

    private fun editListOfStringBaseDatapointComponents(framework: Framework) {
        framework.root.edit<ComponentGroup>("allgemein") {
            val componentGroupAllgemein = this
            with(EsgDatenkatalogListOfStringBaseDataPointComponents) {
                dokumenteZurNachhaltigkeitsstrategie(componentGroupAllgemein)
                richtlinienZurEinhaltungDerUngcp(componentGroupAllgemein)
                richtlinienZurEinhaltungDerOecdLeitsaetze(componentGroupAllgemein)
            }
        }
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setGroupsThatAreExpandedOnPageLoad(framework)
        overwriteFakeFixtureGenerationForDataDate(framework)

        editListOfStringBaseDatapointComponents(framework)
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils = ComponentGenerationUtilsForGermanFrameworks()
}
