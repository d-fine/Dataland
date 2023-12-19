package org.dataland.frameworktoolbox.frameworks.gdv

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvListOfBaseDataPointComponent
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.*
import org.dataland.frameworktoolbox.intermediate.group.*
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Heimathafen framework
 */
@Component
class GdvFramework : InDevelopmentPavedRoadFramework(
    identifier = "gdv",
    label = "GDV/VÖB",
    explanation = "Das GDV/VÖB Framework",
    File("./dataland-framework-toolbox/inputs/gdv/dataDictionary-GDV-VOEB-GDV-VÖB ESG questionnaire.csv"),
) {

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

    private fun createRollingWindowComponentsInCategoryUmwelt(framework: Framework, showIfBerichtsPflicht: FrameworkConditional) {
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

    private fun createRollingWindowComponentsInCategorySoziales(framework: Framework, showIfBerichtsPflicht: FrameworkConditional) {
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

    @Suppress("LongMethod") // t0d0: fix detekt error later!
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        setGroupsThatAreExpandedOnPageLoad(framework)
        overwriteFakeFixtureGenerationForDataDate(framework)

        val berichtsPflicht = framework.root
            .getOrNull<ComponentGroup>("general")
            ?.getOrNull<ComponentGroup>("masterData")
            ?.getOrNull<YesNoComponent>("berichtsPflicht")
        requireNotNull(berichtsPflicht) {
            "The field with the label \"berichtsPflicht\" must exist in the gdv framework."
        }

        val showIfBerichtsPflicht = DependsOnComponentValue(
            berichtsPflicht,
            "Yes",
        )

        createRollingWindowComponentsInCategoryUmwelt(framework, showIfBerichtsPflicht)
        createRollingWindowComponentsInCategorySoziales(framework, showIfBerichtsPflicht)
        createListOfBaseDatapointComponents(framework, showIfBerichtsPflicht)

        val esgBerichte = framework.root
            .getOrNull<ComponentGroup>("allgemein")
            ?.getOrNull<ComponentGroup>("esgBerichte")
        require(esgBerichte != null) {
            "The component group with the label \"esgBerichte\" must exist in the gdv framework."
        }

        val nachhaltigkeitsberichte = esgBerichte.getOrNull<YesNoComponent>("nachhaltigkeitsberichte")
        require(nachhaltigkeitsberichte != null) {
            "The field with the label \"nachhaltigkeitsberichte\" must exist in the gdv framework."
        }

        val unGlobalConceptPrinzipien = framework.root
            .getOrNull<ComponentGroup>("allgemein")
            ?.getOrNull<ComponentGroup>("unGlobalConceptPrinzipien")
        require(unGlobalConceptPrinzipien != null) {
            "The section with the label \"unGlobalConceptPrinzipien\" must exist in the gdv framework."
        }

        val mechanismenZurUeberwachungDerEinhaltungDerUngcp =
            unGlobalConceptPrinzipien.getOrNull<YesNoComponent>("mechanismenZurUeberwachungDerEinhaltungDerUngcp")
        require(mechanismenZurUeberwachungDerEinhaltungDerUngcp != null) {
            "The field with the label \"mechanismenZurUeberwachungDerEinhaltungDerUngcp\" " +
                "must exist in the gdv framework."
        }

        val oecdLeitsaetze = framework.root
            .getOrNull<ComponentGroup>("allgemein")
            ?.getOrNull<ComponentGroup>("oecdLeitsaetze")
        require(oecdLeitsaetze != null) {
            "The section with the label \"oecdLeitsaetze\" must exist in the gdv framework."
        }

        val mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze =
            oecdLeitsaetze.getOrNull<YesNoComponent>("mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze")
        require(mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze != null) {
            "The field with the label \"mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze\" " +
                "must exist in the gdv framework."
        }

        framework.root
            .getOrNull<ComponentGroup>("umwelt")
            ?.getOrNull<ComponentGroup>("taxonomie")
            ?.edit<MultiSelectComponent>("euTaxonomieKompassAktivitaeten") {
                customizeEuTaxonomieKompassAktivitaetenComponent(this)
            }

        val einkommensgleichheit = framework.root.getOrNull<ComponentGroup>("soziales")
            ?.getOrNull<ComponentGroup>("einkommensgleichheit")
        require(einkommensgleichheit != null) {
            "The component group with the label \"einkommensgleichheit\" must exist in the gdv framework."
        }
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return GdvComponentGenerationUtils()
    }

    private fun customizeEuTaxonomieKompassAktivitaetenComponent(component: MultiSelectComponent) {
        if (component.options.size == 1 && component.options.single().label == "EuTaxonomyActivityOptions") {
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
                            "import {activityApiNameToHumanizedName} from " +
                                "\"@/components/resources/frameworkDataSearch/euTaxonomy/ActivityName\";",
                            "import { formatListOfStringsForDatatable } from " +
                                "\"@/components/resources/dataTable/conversion/" +
                                "MultiSelectValueGetterFactory\";",
                        ),
                    ),
                )
            }
            component.uploadConfigGenerator = { sectionUploadConfigBuilder ->
                sectionUploadConfigBuilder.addStandardUploadConfigCell(
                    component,
                    uploadComponentName = "MultiSelectFormField",
                    // TODO Problem:  We cannot make it use the ActivityName.ts file! Limitation!
                )
            }
        }
    }
}
