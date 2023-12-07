package org.dataland.frameworktoolbox.frameworks.gdv

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.MultiSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
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

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("allgemein") {
            viewPageExpandOnPageLoad = true
        }

        framework.root
            .getOrNull<ComponentGroup>("umwelt")
            ?.getOrNull<ComponentGroup>("taxonomie")
            ?.edit<MultiSelectComponent>("euTaxonomieKompassAktivitaeten") {
                if (options.size == 1 && options.single().label == "EuTaxonomyActivityOptions") {
                    this.fixtureGeneratorGenerator = { sectionConfigBuilder: FixtureSectionBuilder ->
                        sectionConfigBuilder.addAtomicExpression(
                            identifier,
                            documentSupport.getFixtureExpression(
                                fixtureExpression = "pickSubsetOfElements(Object.values(Activity))",
                                nullableFixtureExpression =
                                "dataGenerator.valueOrNull(pickSubsetOfElements(Object.values(Activity)))",
                                nullable = isNullable,
                            ),
                            imports = setOf(
                                "import { Activity } from \"@clients/backend\";",
                            ),
                        )
                    }
                    this.viewConfigGenerator = { sectionConfigBuilder ->
                        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
                            this,
                            FrameworkDisplayValueLambda(
                                "formatListOfStringsForDatatable(" +
                                    "${getTypescriptFieldAccessor()}?.map(it => {\n" +
                                    "                  return activityApiNameToHumanizedName(it)}), " +
                                    "'${escapeEcmaScript(label)}'" +
                                    ")",
                                setOf(
                                    "import {activityApiNameToHumanizedName} from " +
                                        "\"@/components/resources/frameworkDataSearch/euTaxonomy/ActivityName\";",
                                    "import { formatListOfStringsForDatatable } from " +
                                        "\"@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory\";",
                                ),
                            ),
                        )
                    }
                }
            }

        // TODO: Remove this. this is just a POC for showing how to create a GdvYearlyDecimalTimeseriesDataComponent.
//        framework.root.edit<ComponentGroup>("allgemein") {
//            create<GdvYearlyDecimalTimeseriesDataComponent>("testingData") {
//                label = "Data, for Testing!"
//                decimalRows = mutableListOf(
//                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow("scope1", "Scope 1", "tCO2-Äquiv."),
//                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow("scope2", "Scope 2", "tCO2-Äquiv."),
//                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow("scope3", "Scope 3", "tCO2-Äquiv."),
//                )
//            }
//        }
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return GdvComponentGenerationUtils()
    }
}
