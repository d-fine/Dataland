package org.dataland.frameworktoolbox.frameworks.esgquestionnaire

import ComponentGenerationUtilsForGermanFrameworks
import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.components.MultiSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.get
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
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
    File("./dataland-framework-toolbox/inputs/esg-questionnaire/dataDictionary-GDV-VOEB-GDV-VÖB ESG questionnaire.csv"),
    order = 6,
) {

    override fun configureDiagnostics(diagnosticManager: DiagnosticManager) {
        diagnosticManager.suppress("IgnoredRow-19-4a93a683")
        diagnosticManager.suppress("IgnoredRow-24-0ed74c4b")
        diagnosticManager.suppress("IgnoredRow-26-b760adfc")
        diagnosticManager.suppress("IgnoredRow-28-939ecd18")
        diagnosticManager.suppress("IgnoredRow-30-299f8248")
        diagnosticManager.suppress("IgnoredRow-32-1056ee6e")
        diagnosticManager.suppress("IgnoredRow-36.1-311efcd3")
        diagnosticManager.suppress("IgnoredRow-38-0c3c46b4")
        diagnosticManager.suppress("IgnoredRow-39-982fa447")
        diagnosticManager.suppress("IgnoredRow-40.3-c921451e")
        diagnosticManager.suppress("IgnoredRow-42-059b81e2")
        diagnosticManager.suppress("IgnoredRow-43-b7b83dcb")
        diagnosticManager.suppress("IgnoredRow-44-107a06aa")
    }

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

    private fun createRollingWindowComponentsInCategoryUmwelt(
        framework: Framework,
        showIfBerichtsPflicht: FrameworkConditional,
    ) {
        framework.root.edit<ComponentGroup>("umwelt") {
            val umweltGroup = this
            with(EsgQuestionnaireUmweltRollingWindowComponents) {
                treibhausgasBerichterstattungUndPrognosen(umweltGroup, showIfBerichtsPflicht)
                berichterstattungEnergieverbrauch(umweltGroup, showIfBerichtsPflicht)
                energieeffizienzImmobilienanlagen(umweltGroup, showIfBerichtsPflicht)
                berichterstattungWasserverbrauch(umweltGroup, showIfBerichtsPflicht)
                unternehmensGruppenStrategieBzglAbfallproduktion(umweltGroup, showIfBerichtsPflicht)
                recyclingImProduktionsprozess(umweltGroup, showIfBerichtsPflicht)
                berichterstattungEinnahmenAusFossilenBrennstoffen(umweltGroup)
                umsatzInvestitionsaufwandFuerNachhaltige(umweltGroup, showIfBerichtsPflicht)
            }
        }
    }

    private fun createRollingWindowComponentsInCategorySoziales(
        framework: Framework,
        showIfBerichtsPflicht: FrameworkConditional,
    ) {
        framework.root.edit<ComponentGroup>("soziales") {
            val sozialesGroup = this
            with(EsgQuestionnaireSozialesRollingWindowComponents) {
                auswirkungenAufAnteilBefristerVertraegeUndFluktuation(sozialesGroup)
                budgetFuerSchulungAusbildung(sozialesGroup, showIfBerichtsPflicht)
                unfallrate(sozialesGroup, showIfBerichtsPflicht)
                massnahmenZurVerbesserungDerEinkommensungleichheit(sozialesGroup, showIfBerichtsPflicht)
            }
        }
    }

    private fun editListOfStringBaseDatapointComponents(framework: Framework) {
        framework.root.edit<ComponentGroup>("allgemein") {
            val componentGroupAllgemein = this
            with(EsgQuestionnaireListOfStringBaseDataPointComponents) {
                aktuelleBerichte(componentGroupAllgemein)
                weitereAkkreditierungen(componentGroupAllgemein)
                richtlinienZurEinhaltungDerUngcp(componentGroupAllgemein)
                richtlinienZurEinhaltungDerOecdLeitsaetze(componentGroupAllgemein)
            }
        }
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setGroupsThatAreExpandedOnPageLoad(framework)
        overwriteFakeFixtureGenerationForDataDate(framework)
        val berichtsPflicht = framework.root
            .get<ComponentGroup>("general")
            .get<ComponentGroup>("masterData")
            .get<YesNoComponent>("berichtspflichtUndEinwilligungZurVeroeffentlichung")

        val showIfBerichtsPflicht = DependsOnComponentValue(berichtsPflicht, "Yes")
        createRollingWindowComponentsInCategoryUmwelt(framework, showIfBerichtsPflicht)
        createRollingWindowComponentsInCategorySoziales(framework, showIfBerichtsPflicht)
        editListOfStringBaseDatapointComponents(framework)
        framework.root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("masterData") {
                edit<YesNoComponent>("berichtspflichtUndEinwilligungZurVeroeffentlichung") {
                    customizeBerichtsPflicht(this)
                }
            }
        }
        framework.root.get<ComponentGroup>("umwelt").get<ComponentGroup>("taxonomie")
            .create<MultiSelectComponent>(
                "euTaxonomieKompassAktivitaeten",
                "umsatzInvestitionsaufwandFuerNachhaltigeAktivitaeten",
            ) {
                setEuTaxonomieKompassAktivitaeten(this)
                availableIf = DependsOnComponentValue(berichtsPflicht, "Yes")
            }
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return ComponentGenerationUtilsForGermanFrameworks()
    }

    private fun setEuTaxonomieKompassAktivitaeten(component: MultiSelectComponent) {
        component.label = "EU Taxonomie Kompass Aktivitäten"
        component.uploadPageExplanation = "Welche Aktivitäten gem. dem EU Taxonomie-Kompass übt das Unternehmen aus?"
        setEuTaxonomieKompassAktivitaetenFixtureGenerator(component)
        setEuTaxonomieKompassAktivitaetenViewConfigGenerator(component)
        setEuTaxonomieKompassAktivitaetenUploadGenerator(component)
        setEuTaxonomieKompassAktivitaetenDataModelGenerator(component)
    }

    private fun setEuTaxonomieKompassAktivitaetenFixtureGenerator(component: MultiSelectComponent) {
        component.fixtureGeneratorGenerator = { sectionConfigBuilder: FixtureSectionBuilder ->
            sectionConfigBuilder.addAtomicExpression(
                component.identifier,
                component.documentSupport.getFixtureExpression(
                    fixtureExpression = "pickSubsetOfElements(Object.values(Activity))",
                    nullableFixtureExpression =
                    "dataGenerator.valueOrNull(pickSubsetOfElements(Object.values(Activity)))",
                    nullable = component.isNullable,
                ),
                imports = setOf(
                    TypeScriptImport("activity", "@clients/backend"),
                ),
            )
        }
    }

    private fun setEuTaxonomieKompassAktivitaetenViewConfigGenerator(component: MultiSelectComponent) {
        component.viewConfigGenerator = { sectionConfigBuilder ->
            sectionConfigBuilder.addStandardCellWithValueGetterFactory(
                component,
                FrameworkDisplayValueLambda(
                    "formatListOfStringsForDatatable(" +
                        "${component.getTypescriptFieldAccessor()}?.map(it => {\n" +
                        "                  return activityApiNameToHumanizedName(it)}), " +
                        "'${escapeEcmaScript(component.label)}'" +
                        ")",
                    setOf(
                        TypeScriptImport(
                            "activityApiNameToHumanizedName",
                            "@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames",
                        ),
                        TypeScriptImport(
                            "formatListOfStringsForDatatable",
                            "@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory",
                        ),
                    ),
                ),
            )
        }
    }

    private fun setEuTaxonomieKompassAktivitaetenUploadGenerator(component: MultiSelectComponent) {
        component.uploadConfigGenerator = { sectionUploadConfigBuilder ->
            sectionUploadConfigBuilder.addStandardUploadConfigCell(
                frameworkUploadOptions = FrameworkUploadOptions(
                    body = "getActivityNamesAsDropdownOptions()",
                    imports = setOf(
                        "import { getActivityNamesAsDropdownOptions } from " +
                            "\"@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames\"",
                    ),
                ),
                component = component,
                uploadComponentName = "MultiSelectFormField",
            )
        }
    }

    private fun setEuTaxonomieKompassAktivitaetenDataModelGenerator(component: MultiSelectComponent) {
        component.dataModelGenerator = { dataClassBuilder ->
            dataClassBuilder.addProperty(
                component.identifier,
                component.documentSupport.getJvmTypeReference(
                    TypeReference(
                        "java.util.EnumSet",
                        true,
                        listOf(
                            TypeReference(
                                "org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity",
                                false,
                            ),
                        ),
                    ),
                    true,
                ),
                component.documentSupport.getJvmAnnotations(),
            )
        }
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
