package org.dataland.frameworktoolbox.frameworks.gdv

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.components.MultiSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.intermediate.logic.FrameworkConditional
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Heimathafen framework
 */
@Component
class GdvFramework : InDevelopmentPavedRoadFramework( // TODO in the end it should implement "PavedRoadFramework" (?)
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

        framework.root.edit<ComponentGroup>("umwelt") {
            edit<ComponentGroup>("taxonomie") {
                edit<MultiSelectComponent>("euTaxonomieKompassAktivitaeten") {
                    customizeEuTaxonomieKompassAktivitaetenComponent(this)
                }
            }
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
                    frameworkUploadOptions = FrameworkUploadOptions(
                        body = "Object.values(Activity),",
                        imports = setOf("import {Activity} from \"@clients/backend\" "),
                    ),
                    component = component,
                    uploadComponentName = "MultiSelectFormField",
                    // TODO Problem:  We cannot make it use the ActivityName.ts file! Limitation!
                )
            }
        }
    }
}
