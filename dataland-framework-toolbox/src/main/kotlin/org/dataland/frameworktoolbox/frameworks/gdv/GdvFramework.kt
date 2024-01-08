package org.dataland.frameworktoolbox.frameworks.gdv

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
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Heimathafen framework
 */
@Component
class GdvFramework : PavedRoadFramework(
    identifier = "gdv",
    label = "GDV/VÖB",
    explanation = "Das GDV/VÖB Framework ist ein ESG-Fragebogen des Gesamtverbands der Versicherer und des " +
        "Bundesverbands Öffentlicher Banken",
    File("./dataland-framework-toolbox/inputs/gdv/dataDictionary-GDV-VOEB-GDV-VÖB ESG questionnaire.csv"),
) {

    override fun configureDiagnostics(diagnosticManager: DiagnosticManager) {
        diagnosticManager.suppress("IgnoredRow-4.2-d76402cd")
        diagnosticManager.suppress("IgnoredRow-9-0fb5f681")
        diagnosticManager.suppress("IgnoredRow-10.1.1-dcf255e8")
        diagnosticManager.suppress("IgnoredRow-10.2.1-7ac5f737")
        diagnosticManager.suppress("IgnoredRow-19-a95add2a")
        diagnosticManager.suppress("IgnoredRow-24-333a7d76")
        diagnosticManager.suppress("IgnoredRow-26-c26c04ec")
        diagnosticManager.suppress("IgnoredRow-28-6bc8e802")
        diagnosticManager.suppress("IgnoredRow-30-a4b12703")
        diagnosticManager.suppress("IgnoredRow-32-2b48572d")
        diagnosticManager.suppress("IgnoredRow-36.1-8c0cd201")
        diagnosticManager.suppress("IgnoredRow-38-0c3c46b4")
        diagnosticManager.suppress("IgnoredRow-39-84e1ebe8")
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
            with(GdvUmweltRollingWindowComponents) {
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
            with(GdvSozialesRollingWindowComponents) {
                auswirkungenAufAnteilBefristerVertraegeUndFluktuation(sozialesGroup)
                budgetFuerSchulungAusbildung(sozialesGroup, showIfBerichtsPflicht)
                unfallrate(sozialesGroup, showIfBerichtsPflicht)
                massnahmenZurVerbesserungDerEinkommensungleichheit(sozialesGroup, showIfBerichtsPflicht)
            }
        }
    }

    private fun createListOfBaseDatapointComponents(framework: Framework, showIfBerichtsPflicht: FrameworkConditional) {
        framework.root.edit<ComponentGroup>("allgemein") {
            val sozialesGroup = this
            with(GdvListOfBaseDataPointComponents) {
                aktuelleBerichte(sozialesGroup)
                weitereAkkreditierungen(sozialesGroup, showIfBerichtsPflicht)
                richtlinienZurEinhaltungDerUngcp(sozialesGroup)
                richtlinienZurEinhaltungDerOecdLeitsaetze(sozialesGroup)
            }
        }
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setGroupsThatAreExpandedOnPageLoad(framework)
        overwriteFakeFixtureGenerationForDataDate(framework)
        val berichtsPflicht = framework.root
            .getOrNull<ComponentGroup>("general")
            ?.getOrNull<ComponentGroup>("masterData")
            ?.getOrNull<YesNoComponent>("berichtspflichtUndEinwilligungZurVeroeffentlichung")
        requireNotNull(berichtsPflicht) {
            "The field \"berichtspflichtUndEinwilligungZurVeroeffentlichung\" must exist in the gdv framework."
        }
        val showIfBerichtsPflicht = DependsOnComponentValue(berichtsPflicht, "Yes")
        createRollingWindowComponentsInCategoryUmwelt(framework, showIfBerichtsPflicht)
        createRollingWindowComponentsInCategorySoziales(framework, showIfBerichtsPflicht)
        createListOfBaseDatapointComponents(framework, showIfBerichtsPflicht)
        framework.root.edit<ComponentGroup>("general") {
            edit<ComponentGroup>("masterData") {
                edit<YesNoComponent>("berichtspflichtUndEinwilligungZurVeroeffentlichung") {
                    customizeBerichtsPflicht(this)
                }
            }
        }
        framework.root.getOrNull<ComponentGroup>("umwelt")?.getOrNull<ComponentGroup>("taxonomie")
            ?.create<MultiSelectComponent>(
                "euTaxonomieKompassAktivitaeten",
                "umsatzInvestitionsaufwandFuerNachhaltigeAktivitaeten",
            ) {
                setEuTaxonomieKompassAktivitaeten(this)
                availableIf = DependsOnComponentValue(berichtsPflicht, "Yes")
            }
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return GdvComponentGenerationUtils()
    }

    private fun setEuTaxonomieKompassAktivitaeten(component: MultiSelectComponent) {
        component.label = "EU Taxonomie Kompass Aktivitäten"
        component.explanation = "Welche Aktivitäten gem. dem EU Taxonomie-Kompass übt das Unternehmen aus?"
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
                    "import { Activity } from \"@clients/backend\";",
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
                        "import { activityApiNameToHumanizedName } from " +
                            "\"@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames\";",
                        "import { formatListOfStringsForDatatable } from " +
                            "\"@/components/resources/dataTable/conversion/" +
                            "MultiSelectValueGetterFactory\";",
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
            )
        }
    }

    private fun customizeBerichtsPflicht(component: YesNoComponent) {
        component.uploadConfigGenerator = { sectionUploadConfigBuilder ->
            sectionUploadConfigBuilder.addStandardUploadConfigCell(
                frameworkUploadOptions = null,
                component = component,
                uploadComponentName = "YesNoFormField",
                validation = FrameworkUploadOptions(
                    body = "\"required|is:Yes\"",
                    imports = null,
                ),
            )
        }
    }
}
