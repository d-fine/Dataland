package org.dataland.frameworktoolbox.frameworks.gdv

import org.apache.commons.text.StringEscapeUtils.escapeEcmaScript
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvListOfBaseDataPointComponent
import org.dataland.frameworktoolbox.frameworks.gdv.custom.GdvYearlyDecimalTimeseriesDataComponent
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.MultiSelectComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
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

    @Suppress("LongMethod") // t0d0: fix detekt error later!
    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        val berichtsPflicht = framework.root
            .getOrNull<ComponentGroup>("general")
            ?.getOrNull<ComponentGroup>("masterData")
            ?.getOrNull<YesNoComponent>("berichtsPflicht")
        require(berichtsPflicht != null) {
            "The field with the label \"berichtsPflicht\" must exist in the gdv framework."
        }

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

        framework.root.edit<ComponentGroup>("allgemein") {
            viewPageExpandOnPageLoad = true
        }

        esgBerichte.create<GdvListOfBaseDataPointComponent>("aktuelleBerichte") {
            label = "Aktuelle Berichte"
            explanation = "Aktuelle Nachhaltigkeits- oder ESG-Berichte"
            descriptionColumnHeader = "Beschreibung des Berichts"
            documentColumnHeader = "Bericht"
            availableIf = DependsOnComponentValue(
                nachhaltigkeitsberichte,
                "Yes",
            )
            // availableIfUpload =   ...   TODO Emanuel: Cannot be implemented yet.
        }

        framework.root
            .getOrNull<ComponentGroup>("allgemein")
            ?.getOrNull<ComponentGroup>("akkreditierungen")
            ?.create<GdvListOfBaseDataPointComponent>(
                "weitereAkkreditierungen",
            ) {
                label = "Weitere Akkreditierungen"
                explanation = "Weitere Akkreditierungen, die noch nicht aufgeführt wurden"
                descriptionColumnHeader = "Beschreibung der Akkreditierung"
                documentColumnHeader = "Akkreditierung"
                availableIf = DependsOnComponentValue(berichtsPflicht, "Yes")
                // availableIfUpload =   ...   TODO Emanuel: Cannot be implemented yet.
            }

        unGlobalConceptPrinzipien.create<GdvListOfBaseDataPointComponent>(
            "richtlinienZurEinhaltungDerUngcp",
            "erklaerungDerEinhaltungDerUngcp",
        ) {
            label = "Richtlinien zur Einhaltung der UNGCP"
            explanation = "Bitte teilen Sie die Richtlinien mit uns die beschreiben oder Informationen darüber " +
                "liefern, wie das Unternehmen die Einhaltung der UN Global Compact Prinzipien überwacht."
            descriptionColumnHeader = "Beschreibung der Richtlinie"
            documentColumnHeader = "Richtlinie"
            availableIf = DependsOnComponentValue(mechanismenZurUeberwachungDerEinhaltungDerUngcp, "Yes")
            // availableIfUpload =   ...   TODO Emanuel: Cannot be implemented yet.
        }

        oecdLeitsaetze.create<GdvListOfBaseDataPointComponent>(
            "richtlinienZurEinhaltungDerOecdLeitsaetze",
            "erklaerungDerEinhaltungDerOecdLeitsaetze",
        ) {
            label = "Richtlinien zur Einhaltung der OECD-Leitsätze"
            explanation = "Bitte teilen Sie die Richtlinien mit uns die beschreiben oder Informationen darüber " +
                "liefern, wie das Unternehmen die Einhaltung der OECD-Leitsätze überwacht."
            descriptionColumnHeader = "Beschreibung der Richtlinie"
            documentColumnHeader = "Richtlinie"
            availableIf = DependsOnComponentValue(mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze, "Yes")
            // availableIfUpload =   ...   TODO Emanuel: Cannot be implemented yet.
        }

        framework.root
            .getOrNull<ComponentGroup>("umwelt")
            ?.getOrNull<ComponentGroup>("taxonomie")
            ?.edit<MultiSelectComponent>("euTaxonomieKompassAktivitaeten") {
                customizeEuTaxonomieKompassAktivitaetenComponent(this)
            }

// TODO what about the placements of all the rolling window fields?
        val componentGroupUmwelt: ComponentGroup? = framework.root.getOrNull<ComponentGroup>("umwelt")
        componentGroupUmwelt?.edit<ComponentGroup>("treibhausgasemissionen") {
            create<GdvYearlyDecimalTimeseriesDataComponent>("treibhausgasBerichterstattungUndPrognosen") {
                label = "Treibhausgas-Berichterstattung und Prognosen"
                explanation = "Welche Treibhausgasinformationen werden derzeit auf Unternehmens-/Konzernebene " +
                    "berichtet und prognostiziert? Bitte geben Sie die Scope1, Scope 2 und Scope 3 Emissionen" +
                    "# für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die " +
                    "kommenden drei Jahre an (in tCO2-Äquiv.)."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "scope1", "Scope 1",
                        "tCO2-Äquiv.",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "scope2", "Scope 2",
                        "tCO2-Äquiv.",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "scope3", "Scope 3",
                        "tCO2-Äquiv.",
                    ), // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                )
            }
        }

        componentGroupUmwelt?.edit<ComponentGroup>("energieverbrauch") {
            create<GdvYearlyDecimalTimeseriesDataComponent>("berichterstattungEnergieverbrauch") {
                label = "Berichterstattung Energieverbrauch"
                explanation = "Bitte geben Sie den Energieverbrauch (in GWh), sowie den Verbrauch erneuerbaren " +
                    "Energien (%) und, falls zutreffend, die Erzeugung erneuerbaren Energien (%) für das aktuelle " +
                    "Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "energieverbrauch",
                        "Energieverbrauch", "GWh",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "verbrauchErneuerbareEnergien",
                        "Verbrauch erneuerbare Energien", "%",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "erzeugungErneuerbareEnergien",
                        "Erzeugung erneuerbare Energien", "%",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                )
                // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
                // TODO Emanuel: Die labels in den Reihen weichen iwie ab von denen im Fragebogen. Wieso?
            }
        }
        componentGroupUmwelt?.edit<ComponentGroup>("energieeffizienzImmobilienanlagen") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEnergieverbrauchVonImmobi" +
                    "lienvermoegen",
            ) {
                label = "Berichterstattung Energieverbrauch von Immobilienvermoegen"
                explanation = "Bitte geben Sie den Anteil an energieeffizienten Immobilienanlagen (%) " +
                    "für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die " +
                    "kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "energieeffizienteImmobilienanlagen",
                        "energieeffiziente Immobilienanlagen", "%",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                )
                // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
                // TODO Emanuel: Das label in der einen Reihe weicht iwie ab von dem im Fragebogen. Wieso?
            }
        }

        componentGroupUmwelt?.edit<ComponentGroup>("wasserverbrauch") {
            create<GdvYearlyDecimalTimeseriesDataComponent>("berichterstattungWasserverbrauch") {
                label = "Berichterstattung Wasserverbrauch"
                explanation = "Bitte geben Sie den Wasserverbrauch (in l), sowie die Emissionen in Wasser " +
                    "(in Tonnen) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen " +
                    "für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "wasserverbrauch",
                        "Wasserverbrauch", "l",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "emissionenInWasser",
                        "Emissionen in Wasser", "t",
                    ),

                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                    // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
                )
            }
        }

        componentGroupUmwelt?.edit<ComponentGroup>("abfallproduktion") {
            create<GdvYearlyDecimalTimeseriesDataComponent>("berichterstattungAbfallproduktion") {
                label = "Berichterstattung Abfallproduktion"
                explanation = "Bitte geben Sie die gesamte Abfallmenge (in Tonnen), sowie den Anteil (%) " +
                    "der gesamten Abfallmenge, der recyclet wird, sowie den Anteil (%) gefährlicher Abfall der " +
                    "gesamten Abfallmenge für das aktuelle Kalenderjahr, die letzten drei Jahren sowie " +
                    "die Prognosen für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "abfallmenge", "Abfallmenge",
                        "t",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "anteilRecycelterAbfallmenge",
                        "Anteil der recycelten Abfallmenge", "%",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "anteilGefaehrlicherAbfallmenge",
                        "Anteil gefährlicher Abfall an Gesamtmenge", "%",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                )
                // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
                // TODO Emanuel: Das label in der einen Reihe weicht iwie ab von dem im Fragebogen. Wieso?
            }
        }

        componentGroupUmwelt?.edit<ComponentGroup>("abfallproduktion") {
            create<GdvYearlyDecimalTimeseriesDataComponent>("recyclingImProduktionsprozess") {
                label = "Recycling im Produktionsprozess"
                explanation = "Bitte geben Sie an, wie hoch der Anteil an Recyclaten (bereits" +
                    "recyceltes wiederverwertetes Material) im Produktionsprozess für das aktuelle Kalenderjahr, " +
                    "die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "anteilAnRecyclaten",
                        "Anteil " +
                            "an Recyclaten",
                        "%",
                    ), // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                )
            }
        }
        val einnahmenAusFossilenBrennstoffen = componentGroupUmwelt
            ?.getOrNull<ComponentGroup>("fossileBrennstoffe")
            ?.getOrNull<YesNoComponent>("einnahmenAusFossilenBrennstoffen")
        require(einnahmenAusFossilenBrennstoffen != null) {
            "The field with the label \"einnahmenAusFossilenBrennstoffen\" must exist in the " +
                "gdv framework."
        }

        componentGroupUmwelt.edit<ComponentGroup>("fossileBrennstoffe") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "berichterstattungEinnahmenAusFossilenBrennstoffen",
            ) {
                label = "Berichterstattung Einnahmen aus fossilen Brennstoffen"
                explanation = "Bitte geben Sie den Anteil (%) der Einnahmen aus fossilen Brennstoffen aus den " +
                    "gesamten Einnahmen für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die " +
                    "Prognosen für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "anteilEinnahmenAusFossilen" +
                            "Brennstoffen",
                        "Anteil der Einnahmen aus fossilen Brennstoffen", "%",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    einnahmenAusFossilenBrennstoffen,
                    "Yes",
                )
            }
            // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
            // TODO Emanuel: Die fixtures von dem Ding sind keine vernünftigen Prozentzahlen
        }

        componentGroupUmwelt.edit<ComponentGroup>("taxonomie") {
            create<GdvYearlyDecimalTimeseriesDataComponent>(
                "umsatzInvestitionsaufwandFuerNachhaltige" +
                    "Aktivitaeten",
            ) {
                label = "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten"
                explanation = "Wie hoch ist der Umsatz/Investitionsaufwand des Unternehmens aus nachhaltigen " +
                    "Aktivitäten (Mio. €) gemäß einer Definition der EU-Taxonomie? Bitte machen Sie Angaben " +
                    "zu den betrachteten Sektoren und gegebenenfalls zu den Annahmen bzgl. Taxonomie-konformen" +
                    " (aligned) Aktivitäten für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die " +
                    "Prognosen für die kommenden drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "umsatzInvestitionsaufwandAus" +
                            "NachhaltigenAktivitaeten",
                        "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten",
                        "Mio. €",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                )
            }
            // TODO Emanuel: Das Ding sollte drei Jahre in die Vergangenheit/Zukunft gehen anstatt zwei.
            // TODO Emanuel: Irgendwie sieht die Tabelle hier auf der viewpage ganz anders aus als im Fragebogen
        }

        val componentGroupSoziales: ComponentGroup? = framework.root.getOrNull<ComponentGroup>("soziales")
        componentGroupSoziales?.create<GdvYearlyDecimalTimeseriesDataComponent>(
            "anzahlDerBetroffenen" +
                "Mitarbeiter",
        ) {
            label = "Anzahl der betroffenen Mitarbeiter"
            explanation = "Bitte teilen Sie mit uns wieviele unbefristete Verträge es insgesamt in Deutschland und " +
                "in der Gesamtgruppe gibt und wieviele unbefristete Verträge von der Änderung betroffen sind " +
                "(Verkauf oder Akquisition)."
            decimalRows = mutableListOf(
                GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                    "anzahlUnbefristeteVertraege",
                    "Anzahl der unbefristeten Verträge", "",
                ),
                GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                    "anzahlvonAenderungBetroffeneVertraege",
                    "Anzahl der von Änderung betroffenen Verträge", "",
                ),
            )
        }
        val vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur = componentGroupSoziales
            ?.getOrNull<ComponentGroup>("unternehmensstrukturaenderungen")
            ?.getOrNull<YesNoComponent>("vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur")
        require(vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur != null) {
            "The field with the label \"vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur\" must exist in " +
                "the gdv framework."
        }
        componentGroupSoziales.create<GdvYearlyDecimalTimeseriesDataComponent>(
            "auswirkungenAufAnteil" +
                "BefristerVertraegeUndFluktuation",
        ) {
            label = "Auswirkungen auf Anteil befrister Verträge und Fluktuation"
            explanation = "Bitte geben Sie die Anzahl der befristeten Verträge sowie die Fluktuation (%) für die" +
                " letzten drei Jahre an."
            decimalRows = mutableListOf(
                GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                    "anzahlbefristeteVertraege",
                    "Anzahl der befristeten Verträge", "",
                ),
                GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                    "fluktuation", "Fluktuation",
                    "%",
                ),
            )
            availableIf = DependsOnComponentValue(
                vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur,
                "Yes",
            )
        }

        framework.root.create<GdvYearlyDecimalTimeseriesDataComponent>("unfallrate") {
            label = "Unfallrate"
            explanation = "Wie hoch war die Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust für die letzten " +
                "drei Jahre?"
            decimalRows = mutableListOf(
                GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                    "haeufigkeitsrateVonArbeitsunfaellen",
                    "Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust", "%",
                ),
            )
            availableIf = DependsOnComponentValue(
                berichtsPflicht,
                "Yes",
            )
        }

        componentGroupSoziales.edit<ComponentGroup>("einkommensgleichheit") {
            create<GdvYearlyDecimalTimeseriesDataComponent>("ueberwachungDerEinkommensungleichheit") {
                label = "Überwachung der Einkommensungleichheit"
                explanation = "Bitte geben Sie das unbereinigte geschlechtsspezifische Lohngefälle, das " +
                    "Einkommensungleichheitsverhältnis, sowie das CEO-Einkommensungleichheitsverhältnis für" +
                    " die letzten drei Jahre an."
                decimalRows = mutableListOf(
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "geschlechtsspezifischesLohngefaelle",
                        "Geschlechtsspezifisches Lohngefälle", "%",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "einkommensungleichheitsverhaeltnis",
                        "Einkommensungleichheitsverhältnis", "%",
                    ),
                    GdvYearlyDecimalTimeseriesDataComponent.TimeseriesRow(
                        "ceoEinkommenungleichheit" +
                            "sverhaeltnis",
                        "CEO-Einkommensungleichheitsverhältnis", "%",
                    ),
                )
                availableIf = DependsOnComponentValue(
                    berichtsPflicht,
                    "Yes",
                )
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
        }
    }
}
