package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

import ComponentGenerationUtilsForGermanFrameworks
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Heimathafen framework
 */
@Component
class EsgQuestionnaireFramework : PavedRoadFramework(
    identifier = "esg-questionnaire",
    label = "ESG Questionnaire für Corporate Schuldscheindarlehen",
    explanation = "Der ESG Questionnaire für Corporate Schuldscheindarlehen ist ein ESG-Fragebogen des " +
        "Gesamtverbands der Versicherer und des Bundesverbands Öffentlicher Banken",
    File("./dataland-framework-toolbox/inputs/esg-questionnaire/esg-questionnaire.xlsx"),
    order = 7,
    enabledFeatures =
    FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.QaModel),
) {

    /*override fun configureDiagnostics(diagnosticManager: DiagnosticManager) {
        diagnosticManager.suppress("IgnoredRow-38-3118f246")
    } TODO suppression is probably not needed anymore. remove at the every end*/

    private fun setGroupsThatAreExpandedOnPageLoad(framework: Framework) {
        framework.root.edit<ComponentGroup>("allgemein") {
            viewPageExpandOnPageLoad = true
        }
        framework.root.edit<ComponentGroup>("general") {
            viewPageExpandOnPageLoad = true
            edit<ComponentGroup>("masterData") {
                viewPageExpandOnPageLoad = true
            }
        }
    }

    private fun overwriteFakeFixtureGenerationForDataDate(framework: Framework) {
        framework.root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("masterData") {
                edit<DateComponent>("gueltigkeitsDatum") {
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
    private fun createRollingWindowComponentsInCategorySoziales(
        framework: Framework,
    ) {
        framework.root.edit<ComponentGroup>("soziales") {
            // val sozialesGroup = this TODO
            with(EsgQuestionnaireSozialesRollingWindowComponents) {
                //  auswirkungenAufAnteilBefristerVertraegeUndFluktuation(sozialesGroup) TODO
                // budgetFuerSchulungAusbildung(sozialesGroup) TODO
                // unfallrate(sozialesGroup) TODO
                // massnahmenZurVerbesserungDerEinkommensungleichheit(sozialesGroup) TODO
            }
        }
    }

    private fun editListOfStringBaseDatapointComponents(framework: Framework) {
        framework.root.edit<ComponentGroup>("allgemein") {
            val componentGroupAllgemein = this
            println(componentGroupAllgemein)
            with(EsgQuestionnaireListOfStringBaseDataPointComponents) {
                dokumenteZurNachhaltigkeitsstrategie(componentGroupAllgemein)
                richtlinienZurEinhaltungDerUngcp(componentGroupAllgemein)
                richtlinienZurEinhaltungDerOecdLeitsaetze(componentGroupAllgemein)
            }
        }
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setGroupsThatAreExpandedOnPageLoad(framework)
        overwriteFakeFixtureGenerationForDataDate(framework)

        createRollingWindowComponentsInCategorySoziales(framework)
        editListOfStringBaseDatapointComponents(framework)
        framework.root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("masterData") {
                edit<YesNoComponent>("berichtspflichtUndEinwilligungZurVeroeffentlichung") {
                    customizeBerichtsPflicht(this)
                }
            }
        }
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return ComponentGenerationUtilsForGermanFrameworks()
    }

    private fun customizeBerichtsPflicht(component: YesNoComponent) {
        component.uploadConfigGenerator = { sectionUploadConfigBuilder ->
            sectionUploadConfigBuilder.addStandardUploadConfigCell(
                frameworkUploadOptions = null,
                component = component,
                uploadComponentName = "YesNoFormField",
                validation = "is:Yes",
            )
        }
    }
}
