import { type Category } from "@/utils/GenericFrameworkTypes";
import { type GdvData } from "@clients/backend";
//import {DropdownOption} from "@/utils/PremadeDropdownDatasets";
import { getActivityNamesAsDropdownOptions } from "@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames";

export const gdvDataModel = [
  {
    name: "general",
    label: "General",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "masterData",
        label: "Master Data",
        fields: [
          {
            name: "berichtsPflicht",
            label: "Berichts-Pflicht",
            description: "Ist das Unternehmen berichtspflichtig?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: true,
            showIf: (): boolean => true,
            validation: "is:Yes",
          },
          {
            name: "gueltigkeitsDatum",
            label: "(Gültigkeits) Datum",
            description: "Datum bis wann die Information gültig ist",
            options: "",
            unit: "",
            component: "DateFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
    ],
  },
  {
    name: "allgemein",
    label: "Allgemein",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "esgZiele",
        label: "ESG Ziele",
        fields: [
          {
            name: "existenzVonEsgZielen",
            label: "Existenz von ESG-Zielen",
            description:
              "Hat das Unternehmen spezifische ESG-Ziele/Engagements? Werden bspw. spezifische Ziele / Maßnahmen ergriffen, um das 1,5 Grad Ziel zu erreichen?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "beschreibungDerEsgZiele",
            label: "Beschreibung der ESG-Ziele",
            description: "Bitte geben Sie eine genaue Beschreibung der ESG-Ziele.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.esgZiele?.existenzVonEsgZielen == "Yes",
            validation: "",
          },
          {
            name: "investitionenInZielerreichung",
            label: "Investitionen in Zielerreichung",
            description:
              "Bitte geben Sie an wieviele Budgets/Vollzeitäquivalente für das Erreichen der ESG-Ziele zugewiesen wurden.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.esgZiele?.existenzVonEsgZielen == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "sektoren",
        label: "Sektoren",
        fields: [
          {
            name: "sektorenMitHohenKlimaauswirkungen",
            label: "Sektoren mit hohen Klimaauswirkungen",
            description:
              "Kann das Unternehmen einem oder mehreren Sektoren mit hohen Klimaauswirkungen zugeordnet werden?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "auflistungDerSektoren",
            label: "Auflistung der Sektoren",
            description:
              "Bitte geben Sie an, zu welchen Sektoren (mit hohen Klimaauswirkungen) das Unternehmen zugeordnet werden kann.",
            options: [
              {
                label: "A",
                value: "A",
              },
              {
                label: "B",
                value: "B",
              },
              {
                label: "C",
                value: "C",
              },
              {
                label: "D",
                value: "D",
              },
              {
                label: "E",
                value: "E",
              },
              {
                label: "F",
                value: "F",
              },
              {
                label: "G",
                value: "G",
              },
              {
                label: "H",
                value: "H",
              },
              {
                label: "L",
                value: "L",
              },
            ],
            unit: "",
            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.sektoren?.sektorenMitHohenKlimaauswirkungen == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "esgBerichte",
        label: "ESG Berichte",
        fields: [
          {
            name: "nachhaltigkeitsberichte",
            label: "Nachhaltigkeitsberichte",
            description: "Erstellt das Unternehmen Nachhaltigkeits- oder ESG-Berichte?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "frequenzDerBerichterstattung",
            label: "Frequenz der Berichterstattung",
            description: "In welchen Zeitabständen werden die Berichte erstellt?",
            options: [
              {
                label: "jährlich",
                value: "Jaehrlich",
              },
              {
                label: "halbjährlich",
                value: "Halbjaehrlich",
              },
              {
                label: "vierteljährlich",
                value: "Vierteljaehrlich",
              },
              {
                label: "monatlich",
                value: "Monatlich",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.esgBerichte?.nachhaltigkeitsberichte == "Yes",
            validation: "",
          },
          {
            name: "aktuelleBerichte",
            label: "Aktuelle Berichte",
            description: "Aktuelle Nachhaltigkeits- oder ESG-Berichte",
            options: "",
            unit: "",
            component: "ListOfBaseDataPointsFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.esgBerichte?.nachhaltigkeitsberichte == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "akkreditierungen",
        label: "Akkreditierungen",
        fields: [
          {
            name: "iso14001",
            label: "ISO 14001",
            description:
              "Haben Sie eine ISO 14001 Akkreditierung? Bitte teilen Sie das entsprechende Zertifikat mit uns.",
            options: "",
            unit: "",
            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "iso45001",
            label: "ISO 45001",
            description:
              "Haben Sie eine ISO 45001 Akkreditierung? Bitte teilen Sie das entsprechende Zertifikat mit uns.",
            options: "",
            unit: "",
            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "iso27001",
            label: "ISO 27001",
            description:
              "Haben Sie eine ISO 27001 Akkreditierung? Bitte teilen Sie das entsprechende Zertifikat mit uns.",
            options: "",
            unit: "",
            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "iso50001",
            label: "ISO 50001",
            description:
              "Haben Sie eine ISO 50001 Akkreditierung? Bitte teilen Sie das entsprechende Zertifikat mit uns.",
            options: "",
            unit: "",
            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "weitereAkkreditierungen",
            label: "Weitere Akkreditierungen",
            description: "Weitere Akkreditierungen, die noch nicht aufgeführt wurden",
            options: "",
            unit: "",
            component: "ListOfBaseDataPointsFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "unGlobalConceptPrinzipien",
        label: "UN Global Concept Prinzipien",
        fields: [
          {
            name: "mechanismenZurUeberwachungDerEinhaltungDerUngcp",
            label: "Mechanismen zur Überwachung der Einhaltung der UNGCP",
            description:
              "Verfügt das Unternehmen über Prozesse und Compliance-Mechanismen, um die Einhaltung der Prinzipien des UN Global Compact zu überwachen?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "richtlinienZurEinhaltungDerUngcp",
            label: "Richtlinien zur Einhaltung der UNGCP",
            description:
              "Bitte teilen Sie die Richtlinien mit uns die beschreiben oder Informationen darüber liefern, wie das Unternehmen die Einhaltung der UN Global Compact Prinzipien überwacht.",
            options: "",
            unit: "",
            component: "ListOfBaseDataPointsFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.unGlobalConceptPrinzipien?.mechanismenZurUeberwachungDerEinhaltungDerUngcp == "Yes",
            validation: "",
          },
          {
            name: "erklaerungDerEinhaltungDerUngcp",
            label: "Erklärung der Einhaltung der UNGCP",
            description: "Bitte geben Sie eine Erklärung ab, dass keine Verstöße gegen diese Grundsätze vorliegen.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.unGlobalConceptPrinzipien?.mechanismenZurUeberwachungDerEinhaltungDerUngcp == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "oecdLeitsaetze",
        label: "OECD Leitsätze",
        fields: [
          {
            name: "mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze",
            label: "Mechanismen zur Überwachung der Einhaltung der OECD-Leitsätze",
            description:
              "Verfügt das Unternehmen über Prozesse und Compliance-Mechanismen, um die Einhaltung der OECD-Leitsätze für multinationale Unternehmen (OECD MNE Guidelines) zu überwachen?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "richtlinienZurEinhaltungDerOecdLeitsaetze",
            label: "Richtlinien zur Einhaltung der OECD-Leitsätze",
            description:
              "Bitte teilen Sie die Richtlinien mit uns die beschreiben oder Informationen darüber liefern, wie das Unternehmen die Einhaltung der OECD-Leitsätze überwacht.",
            options: "",
            unit: "",
            component: "ListOfBaseDataPointsFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.oecdLeitsaetze?.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze == "Yes",
            validation: "",
          },
          {
            name: "erklaerungDerEinhaltungDerOecdLeitsaetze",
            label: "Erklärung der Einhaltung der OECD-Leitsätze",
            description: "Bitte geben Sie eine Erklärung ab, dass keine Verstöße gegen diese Grundsätze vorliegen.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.oecdLeitsaetze?.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "sonstige",
        label: "Sonstige",
        fields: [
          {
            name: "ausrichtungAufDieUnSdgsUndAktivesVerfolgen",
            label: "Ausrichtung auf die UN SDGs und aktives Verfolgen",
            description:
              "Wie steht das Unternehmen in Einklang mit den 17 UN-Zielen für nachhaltige Entwicklung? Welche dieser Ziele verfolgt das Unternehmen aktiv, entweder durch ihre Geschäftstätigkeit oder durch die Unternehmensführung?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "ausschlusslistenAufBasisVonEsgKriterien",
            label: "Ausschlusslisten auf Basis von ESG Kriterien",
            description:
              "Führt das Unternehmen Ausschlusslisten? Von besonderem Interesse sind Listen die Ausschlusskriterien, die einen Bezug zu den Bereichen E, S oder G haben.",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "ausschlusslisten",
            label: "Ausschlusslisten",
            description: "Bitte nennen Sie die Ausschlusslisten auf Basis von ESG Kriterien.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.sonstige?.ausschlusslistenAufBasisVonEsgKriterien == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "fuehrungsstandards",
        label: "Führungsstandards",
        fields: [
          {
            name: "oekologischeSozialeFuehrungsstandardsOderPrinzipien",
            label: "Ökologische/soziale Führungsstandards oder -prinzipien",
            description:
              "Hat sich das Unternehmen zu ökologischen/sozialen Führungsstandards oder Prinzipien verpflichtet?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anreizmechanismenFuerDasManagementUmwelt",
            label: "Anreizmechanismen für das Management (Umwelt)",
            description:
              "Wie spiegeln sich die Anreizmechanismen für den Bereich Umwelt in der jährlichen Zielsetzung für das Management wieder? Bitte geben Sie die aktuellen Verpflichtungen an.",
            options: [
              {
                label: "Nein",
                value: "Nein",
              },
              {
                label: "Ja, Aufsichtsrat",
                value: "JaAufsichtsrat",
              },
              {
                label: "Ja, Geschäftsleitung",
                value: "JaGeschaeftsleitung",
              },
              {
                label: "Ja, Aufsichtsrat und Geschäftsleitung",
                value: "JaAufsichtsratUndGeschaeftsleitung",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.fuehrungsstandards?.oekologischeSozialeFuehrungsstandardsOderPrinzipien == "Yes",
            validation: "",
          },
          {
            name: "anreizmechanismenFuerDasManagementSoziales",
            label: "Anreizmechanismen für das Management (Soziales)",
            description:
              "Wie spiegeln sich die Anreizmechanismen für den Bereich Soziales in der jährlichen Zielsetzung für das Management wieder? Bitte geben Sie die aktuellen Verpflichtungen an.",
            options: [
              {
                label: "Nein",
                value: "Nein",
              },
              {
                label: "Ja, Aufsichtsrat",
                value: "JaAufsichtsrat",
              },
              {
                label: "Ja, Geschäftsleitung",
                value: "JaGeschaeftsleitung",
              },
              {
                label: "Ja, Aufsichtsrat und Geschäftsleitung",
                value: "JaAufsichtsratUndGeschaeftsleitung",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.fuehrungsstandards?.oekologischeSozialeFuehrungsstandardsOderPrinzipien == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "rechtsstreitigkeiten",
        label: "Rechtsstreitigkeiten",
        fields: [
          {
            name: "esgBezogeneRechtsstreitigkeiten",
            label: "ESG-bezogene Rechtsstreitigkeiten",
            description:
              "Ist das Unternehmen in laufende bzw. war das Unternehmen in den letzten 3 Jahren in abgeschlossenen Rechtsstreitigkeiten im Zusammenhang mit ESG involviert?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "rechtsstreitigkeitenMitBezugZuE",
            label: "Rechtsstreitigkeiten mit Bezug zu E",
            description: 'Haben bzw. hatten die Rechtsstreitigkeiten Bezug zu "E"',
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.esgBezogeneRechtsstreitigkeiten == "Yes",
            validation: "",
          },
          {
            name: "statusZuE",
            label: "Status zu E",
            description: 'Sind die Rechtsstreitigkeiten mit Bezug zu "E" noch offen oder bereits geklärt?',
            options: [
              {
                label: "offen",
                value: "Offen",
              },
              {
                label: "geklärt",
                value: "Geklaert",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuE == "Yes",
            validation: "",
          },
          {
            name: "einzelheitenZuDenRechtsstreitigkeitenZuE",
            label: "Einzelheiten zu den Rechtsstreitigkeiten zu E",
            description: "Bitte erläutern Sie Einzelheiten zu den Rechtsstreitigkeiten.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuE == "Yes",
            validation: "",
          },
          {
            name: "rechtsstreitigkeitenMitBezugZuS",
            label: "Rechtsstreitigkeiten mit Bezug zu S",
            description: 'Haben bzw. hatten die Rechtsstreitigkeiten Bezug zu "S"',
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.esgBezogeneRechtsstreitigkeiten == "Yes",
            validation: "",
          },
          {
            name: "statusZuS",
            label: "Status zu S",
            description: 'Sind die Rechtsstreitigkeiten mit Bezug zu "S" noch offen oder bereits geklärt?',
            options: [
              {
                label: "offen",
                value: "Offen",
              },
              {
                label: "geklärt",
                value: "Geklaert",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuS == "Yes",
            validation: "",
          },
          {
            name: "einzelheitenZuDenRechtsstreitigkeitenZuS",
            label: "Einzelheiten zu den Rechtsstreitigkeiten zu S",
            description: "Bitte erläutern Sie Einzelheiten zu den Rechtsstreitigkeiten.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuS == "Yes",
            validation: "",
          },
          {
            name: "rechtsstreitigkeitenMitBezugZuG",
            label: "Rechtsstreitigkeiten mit Bezug zu G",
            description: 'Haben bzw. hatten die Rechtsstreitigkeiten Bezug zu "G"',
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.esgBezogeneRechtsstreitigkeiten == "Yes",
            validation: "",
          },
          {
            name: "statusZuG",
            label: "Status zu G",
            description: 'Sind die Rechtsstreitigkeiten mit Bezug zu "G" noch offen oder bereits geklärt?',
            options: [
              {
                label: "offen",
                value: "Offen",
              },
              {
                label: "geklärt",
                value: "Geklaert",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuG == "Yes",
            validation: "",
          },
          {
            name: "einzelheitenZuDenRechtsstreitigkeitenZuG",
            label: "Einzelheiten zu den Rechtsstreitigkeiten zu G",
            description: "Bitte erläutern Sie Einzelheiten zu den Rechtsstreitigkeiten.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuG == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "rating",
        label: "Rating",
        fields: [
          {
            name: "esgRating",
            label: "ESG-Rating",
            description: "Hat das Unternehmen bereits ein ESG-Rating einer anerkannten Ratingagentur?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "agentur",
            label: "Agentur",
            description: "Welche Rating Agentur hat das Rating durchgeführt?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.rating?.esgRating == "Yes",
            validation: "",
          },
          {
            name: "ergebnis",
            label: "Ergebnis",
            description: "Wie lautet das Rating?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.rating?.esgRating == "Yes",
            validation: "",
          },
          {
            name: "ratingbericht",
            label: "Ratingbericht",
            description: "Liegt ein Ratingbericht vor?",
            options: "",
            unit: "",
            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.rating?.esgRating == "Yes",
            validation: "",
          },
          {
            name: "kritischePunkte",
            label: "Kritische Punkte",
            description: "Was waren die kritischen Punkte beim ESG-Rating?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.rating?.esgRating == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "anleihen",
        label: "Anleihen",
        fields: [
          {
            name: "grueneSozialeUndOderNachhaltigeEmissionen",
            label: "Grüne, soziale und/oder nachhaltige Emissionen",
            description: "Hat das Unternehmen „grüne“, „soziale“ und/oder „nachhaltige“ Schuldtitel begeben?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "ausstehendeGrueneSozialeUndOderNachhaltigeEmissionen",
            label: "Ausstehende grüne, soziale und/oder nachhaltige Emissionen",
            description:
              "Bitte geben Sie Details zu den ausstehenden Emissionen für das letzte Jahr der Berichterstattung an (in Mio €).",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.allgemein?.anleihen?.grueneSozialeUndOderNachhaltigeEmissionen == "Yes",
            validation: "",
          },
          {
            name: "sustainibilityLinkedDebt",
            label: "Sustainibility Linked Debt",
            description: "Hat das Unternehmen Sustainability Linked Debt („SLD“) emittiert?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "ausstehendeSustainibilityLinkedDebt",
            label: "Ausstehende Sustainibility Linked Debt",
            description:
              "Bitte geben Sie Details zu den ausstehenden Emissionen für das letzte Jahr der Berichterstattung an (in Mio €).",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.allgemein?.anleihen?.sustainibilityLinkedDebt == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "risiken",
        label: "Risiken",
        fields: [
          {
            name: "wichtigsteESUndGRisikenUndBewertung",
            label: "Wichtigste E-, S- und G-Risiken und Bewertung",
            description:
              "Welches sind die wichtigsten von der Gruppe identifizierten E-, S- und G-Risiken? Bitte geben Sie die Details / Bewertung der identifizierten Risiken an.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "hindernisseBeimUmgangMitEsgBedenken",
            label: "Hindernisse beim Umgang mit ESG-Bedenken",
            description:
              "Welche grundsätzlichen Hindernisse bestehen für das Unternehmen bei der Berücksichtigung von ESG-Belangen?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
    ],
  },
  {
    name: "umwelt",
    label: "Umwelt",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "treibhausgasemissionen",
        label: "Treibhausgasemissionen",
        fields: [
          {
            name: "treibhausgasBerichterstattungUndPrognosen",
            label: "Treibhausgas-Berichterstattung und Prognosen",
            description:
              "Welche Treibhausgasinformationen werden derzeit auf Unternehmens-/Konzernebene berichtet und prognostiziert? Bitte geben Sie die Scope1, Scope 2 und Scope 3 Emissionen# für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an (in tCO2-Äquiv.).",
            options: [
              {
                label: "Scope 1 (in tCO2-Äquiv.)",
                value: "scope1",
              },
              {
                label: "Scope 2 (in tCO2-Äquiv.)",
                value: "scope2",
              },
              {
                label: "Scope 3 (in tCO2-Äquiv.)",
                value: "scope3",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "treibhausgasEmissionsintensitaetDerUnternehmenInDieInvestriertWird",
            label: "Treibhausgas-Emissionsintensität der Unternehmen, in die investriert wird",
            description:
              "THG-Emissionsintensität der Unternehmen, in die investiert wird. Scope 1 + Scope 2 Treibhausgasemissionen ./. Umsatz in Millionen EUR Scope 1 + Scope 2 Treibhausgasemissionen ./. Unternehmensgröße in Mio. EUR",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "strategieUndZieleZurReduzierungVonTreibhausgasEmissionen",
            label: "Strategie und Ziele zur Reduzierung von Treibhausgas-Emissionen",
            description:
              "Welchen Entwicklungspfad bzgl. der (Reduktion von) Treibhausgasemissionen verfolgt das Unternehmen. Gibt es einen Zeitplan bzw. konkrete Ziele? Und wie plant das Unternehmen, diesen Kurs zu erreichen? Bitte erläutern Sie, in welchem Bezug dieser Entwicklungspfad zu dem auf dem Pariser Abkommen basierenden Kurs steht.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "produktion",
        label: "Produktion",
        fields: [
          {
            name: "produkteZurVerringerungDerUmweltbelastung",
            label: "Produkte zur Verringerung der Umweltbelastung",
            description:
              "Entwickelt, produziert oder vertreibt das Unternehmen Produkte, die die Umweltbelastung verringern?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "verringerungenDerUmweltbelastung",
            label: "Verringerungen der Umweltbelastung",
            description: "Bitte beschreiben Sie möglichst genau, wie die Produkte die Umweltbelastung reduzieren.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.umwelt?.produktion?.produkteZurVerringerungDerUmweltbelastung == "Yes",
            validation: "",
          },
          {
            name: "oekologischerMindestStandardFuerProduktionsprozesse",
            label: "Ökologischer Mindest-Standard für Produktionsprozesse",
            description:
              "Verfügt das Unternehmen über interne Richtlinien, die einen Mindestumweltstandard im Produktionsprozess sicherstellen?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "energieverbrauch",
        label: "Energieverbrauch",
        fields: [
          {
            name: "berichterstattungEnergieverbrauch",
            label: "Berichterstattung Energieverbrauch",
            description:
              "Bitte geben Sie den Energieverbrauch (in GWh), sowie den Verbrauch erneuerbaren Energien (%) und, falls zutreffend, die Erzeugung erneuerbaren Energien (%) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Energieverbrauch (in GWh)",
                value: "energieverbrauch",
              },
              {
                label: "% des Verbrauchs erneuerbarer Energien (in %)",
                value: "prozentDesVerbrauchsErneuerbarerEnergien",
              },
              {
                label: "Gegebenenfalls % der erneuerbaren Energieerzeugung (in %)",
                value: "ggfProzentDerErneuerbarenEnergieerzeugung",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "unternehmensGruppenStrategieBzglEnergieverbrauch",
            label: "Unternehmens/Gruppen Strategie bzgl Energieverbrauch",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "energieeffizienzImmobilienanlagen",
        label: "Energieeffizienz Immobilienanlagen",
        fields: [
          {
            name: "berichterstattungEnergieverbrauchVonImmobilienvermoegen",
            label: "Berichterstattung Energieverbrauch von Immobilienvermoegen",
            description:
              "Bitte geben Sie den Anteil an energieeffizienten Immobilienanlagen (%) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Engagement/Anteil in energieineffizienten Immobilienanlagen",
                value: "engagementAnteilInEnergieineffizientenImmobilienanlagen",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "unternehmensGruppenStrategieBzglEnergieeffizientenImmobilienanlagen",
            label: "Unternehmens/Gruppen Strategie bzgl energieeffizienten Immobilienanlagen",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "wasserverbrauch",
        label: "Wasserverbrauch",
        fields: [
          {
            name: "berichterstattungWasserverbrauch",
            label: "Berichterstattung Wasserverbrauch",
            description:
              "Bitte geben Sie den Wasserverbrauch (in l), sowie die Emissionen in Wasser (in Tonnen) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Wasserverbrauch (in l)",
                value: "wasserverbrauch",
              },
              {
                label: "Emissionen in Wasser (in t)",
                value: "emissionenInWasser",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "unternehmensGruppenStrategieBzglWasserverbrauch",
            label: "Unternehmens/Gruppen Strategie bzgl Wasserverbrauch",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "abfallproduktion",
        label: "Abfallproduktion",
        fields: [
          {
            name: "berichterstattungAbfallproduktion",
            label: "Berichterstattung Abfallproduktion",
            description:
              "Bitte geben Sie die gesamte Abfallmenge (in Tonnen), sowie den Anteil (%) der gesamten Abfallmenge, der recyclet wird, sowie den Anteil (%) gefährlicher Abfall der gesamten Abfallmenge für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Gesamte Abfallmenge (in t)",
                value: "gesamteAbfallmenge",
              },
              {
                label: "% Abfall recycelt (in %)",
                value: "prozentAbfallRecyclet",
              },
              {
                label: "% Gefährlicher Abfall (in %)",
                value: "prozentGefaehrlicherAbfall",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "unternehmensGruppenStrategieBzglAbfallproduktion",
            label: "Unternehmens/Gruppen Strategie bzgl Abfallproduktion",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "recyclingImProduktionsprozess",
            label: "Recycling im Produktionsprozess",
            description:
              "Bitte geben Sie an, wie hoch der Anteil an Recyclaten (bereitsrecyceltes wiederverwertetes Material) im Produktionsprozess für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre.",
            options: [
              {
                label: "% Recycelte Werkstoffe im Produktionsprozess (in %)",
                value: "prozentRecycelteWerkstoffeImProduktionsprozess",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "gefaehrlicherAbfall",
            label: "Gefährlicher Abfall",
            description:
              "Wie wird in dem Unternehmen während der Produktion und Verarbeitung mit gefährlichen Abfällen (brennbar, reaktiv, giftig, radioaktiv) umgegangen?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "biodiversitaet",
        label: "Biodiversität",
        fields: [
          {
            name: "negativeAktivitaetenFuerDieBiologischeVielfalt",
            label: "Negative Aktivitäten für die biologische Vielfalt",
            description:
              "Hat das Unternehmen Standorte / Betriebe in oder in der Nähe von biodiversitätssensiblen Gebieten, in denen sich die Aktivitäten des Unternehmens negativ auf diese Gebiete auswirken?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "negativeMassnahmenFuerDieBiologischeVielfalt",
            label: "Negative Maßnahmen für die biologische Vielfalt",
            description:
              "Bitte erläutern Sie Aktivitäten, die sich negativ auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für den Umgang mit diesen Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.umwelt?.biodiversitaet?.negativeAktivitaetenFuerDieBiologischeVielfalt == "Yes",
            validation: "",
          },
          {
            name: "positiveAktivitaetenFuerDieBiologischeVielfalt",
            label: "Positive Aktivitäten für die biologische Vielfalt",
            description:
              "Hat das Unternehmen Standorte / Betriebe in oder in der Nähe von biodiversitätssensiblen Gebieten, in denen sich die Aktivitäten des Unternehmens positiv auf diese Gebiete auswirken?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "positiveMassnahmenFuerDieBiologischeVielfalt",
            label: "Positive Maßnahmen für die biologische Vielfalt",
            description:
              "Bitte erläutern Sie Aktivitäten, die sich positiv auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für die Weiterentwicklung dieser Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.umwelt?.biodiversitaet?.positiveAktivitaetenFuerDieBiologischeVielfalt == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "fossileBrennstoffe",
        label: "Fossile Brennstoffe",
        fields: [
          {
            name: "einnahmenAusFossilenBrennstoffen",
            label: "Einnahmen aus fossilen Brennstoffen",
            description:
              "Erzielt das Unternehmen einen Teil seiner Einnahmen aus Aktivitäten im Bereich fossiler Brennstoffe und/oder besitzt das Unternehmen Immobilien, die an der Gewinnung, Lagerung, dem Transport oder der Herstellung fossiler Brennstoffe beteiligt sind?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "berichterstattungEinnahmenAusFossilenBrennstoffen",
            label: "Berichterstattung Einnahmen aus fossilen Brennstoffen",
            description:
              "Bitte geben Sie den Anteil (%) der Einnahmen aus fossilen Brennstoffen aus den gesamten Einnahmen für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "% der Einnahmen aus fossilen Brennstoffen (in %)",
                value: "prozentDerEinnahmenAusFossilenBrennstoffen",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.umwelt?.fossileBrennstoffe?.einnahmenAusFossilenBrennstoffen == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "taxonomie",
        label: "Taxonomie",
        fields: [
          {
            name: "taxonomieBerichterstattung",
            label: "Taxonomie Berichterstattung",
            description: "Wird der EU-Taxonomie Bericht auf Basis NFRD oder auf Basis CSRD erstellt?",
            options: [
              {
                label: "NFRD",
                value: "Nfrd",
              },
              {
                label: "CSRD",
                value: "Csrd",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "euTaxonomieKompassAktivitaeten",
            label: "EU Taxonomie Kompass Aktivitäten",
            description: "Welche Aktivitäten gem. dem EU Taxonomie-Kompass übt das Unternehmen aus?",
            options: getActivityNamesAsDropdownOptions(),
            unit: "",
            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "umsatzInvestitionsaufwandFuerNachhaltigeAktivitaeten",
            label: "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten",
            description:
              "Wie hoch ist der Umsatz/Investitionsaufwand des Unternehmens aus nachhaltigen Aktivitäten (Mio. €) gemäß einer Definition der EU-Taxonomie? Bitte machen Sie Angaben zu den betrachteten Sektoren und gegebenenfalls zu den Annahmen bzgl. Taxonomie-konformen (aligned) Aktivitäten für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Taxonomie geeignet (eligible) nach % Umsatz (in %)",
                value: "taxonomieGeeignetNachProzentUmsatz",
              },
              {
                label: "Taxonomie geeignet (eligible) nach % Capex (in %)",
                value: "taxonomieGeeignetNachProzentCapex",
              },
              {
                label: "Taxonomie konform (aligned) nach % Umsatz (in %)",
                value: "taxonomieKonformNachProzentUmsatz",
              },
              {
                label: "Taxonomie konform (aligned) nach % Capex (in %)",
                value: "taxonomieKonformNachProzentCapex",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
    ],
  },
  {
    name: "soziales",
    label: "Soziales",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "unternehmensstrukturaenderungen",
        label: "Unternehmensstrukturänderungen",
        fields: [
          {
            name: "vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur",
            label: "Vorhandensein kürzlicher Änderungen der Unternehmensstruktur",
            description:
              "Gab es kürzlich eine Veränderung im Unternehmen / in der Gruppe (Umstrukturierung, Verkauf oder Übernahme)?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlUnbefristeterVertraegeInDeutschland",
            label: "Anzahl unbefristeter Verträge in Deutschland",
            description: "Bitte teilen Sie mit uns wieviele unbefristete Verträge es insgesamt in Deutschland gibt.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.unternehmensstrukturaenderungen
                ?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur == "Yes",
            validation: "",
          },
          {
            name: "anzahlDerVonEinemVerkaufBetroffenenUnbefristetenVertraegeInDeutschland",
            label: "Anzahl der von einem Verkauf betroffenen unbefristeten Verträge in Deutschland ",
            description:
              "Bitte teilen Sie mit uns wieviele unbefristete Verträge in Deutschland von einem etwaigen Verkauf betroffen waren.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.unternehmensstrukturaenderungen
                ?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur == "Yes",
            validation: "",
          },
          {
            name: "anzahlDerVonEinerAkquisitionBetroffenenUnbefristetenVertraegeInDeutschland",
            label: "Anzahl der von einer Akquisition betroffenen unbefristeten Verträge in Deutschland ",
            description:
              "Bitte teilen Sie mit uns wieviele unbefristete Verträge in Deutschland von einer etwaigen Akquisition betroffen waren.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.unternehmensstrukturaenderungen
                ?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur == "Yes",
            validation: "",
          },
          {
            name: "anzahlUnbefristeterVertraegeInDerGesamtgruppe",
            label: "Anzahl unbefristeter Verträge in der Gesamtgruppe",
            description:
              "Bitte teilen Sie mit uns wieviele unbefristete Verträge es insgesamt in der Gesamtgruppe gibt",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.unternehmensstrukturaenderungen
                ?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur == "Yes",
            validation: "",
          },
          {
            name: "anzahlDerVonEinemVerkaufBetroffenenUnbefristetenVertraegeInDerGesamtgruppe",
            label: "Anzahl der von einem Verkauf betroffenen unbefristeten Verträge in der Gesamtgruppe",
            description:
              "Bitte teilen Sie mit uns wieviele unbefristete Verträge in der Gesamtgruppe von einem etwaigen Verkauf betroffen waren.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.unternehmensstrukturaenderungen
                ?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur == "Yes",
            validation: "",
          },
          {
            name: "anzahlDerVonEinerAkquisitionBetroffenenUnbefristetenVertraegeInDerGesamtgruppe",
            label: "Anzahl der von einer Akquisition betroffenen unbefristeten Verträge in der Gesamtgruppe",
            description:
              "Bitte teilen Sie mit uns wieviele unbefristete Verträge in der Gesamtgruppe von einer etwaigen Akquisition betroffen waren.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.unternehmensstrukturaenderungen
                ?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur == "Yes",
            validation: "",
          },
          {
            name: "auswirkungenAufAnteilBefristerVertraegeUndFluktuation",
            label: "Auswirkungen auf Anteil befrister Verträge und Fluktuation",
            description:
              "Bitte geben Sie die Anzahl der befristeten Verträge sowie die Fluktuation (%) für die letzten drei Jahre an.",
            options: [
              {
                label: "# der befristeten Verträge",
                value: "anzahlDerBefristetenVertraege",
              },
              {
                label: "Fluktuation (in %)",
                value: "fluktuation",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearPastDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.unternehmensstrukturaenderungen
                ?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "sicherheitUndWeiterbildung",
        label: "Sicherheit und Weiterbildung",
        fields: [
          {
            name: "sicherheitsmassnahmenFuerMitarbeiter",
            label: "Sicherheitsmaßnahmen für Mitarbeiter",
            description:
              "Welche Maßnahmen werden ergriffen, um die Gesundheit und Sicherheit der Mitarbeiter des Unternehmens zu verbessern?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "unfallrate",
            label: "Unfallrate",
            description:
              "Wie hoch war die Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust für die letzten drei Jahre?",
            options: [
              {
                label: "Häufigkeitsrate von Arbeitsunfällen mit Zeitverlust",
                value: "haeufigkeitsrateVonArbeitsunfaellenMitZeitverlust",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearPastDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "budgetFuerSchulungAusbildung",
            label: "Budget für Schulung/Ausbildung",
            description:
              "Bitte geben Sie an wie hoch das Budget ist, das pro Mitarbeiter und Jahr für Schulungen/Fortbildungen in den letzten drei Jahren ausgegeben wurde.",
            options: [
              {
                label: "Budget pro Mitarbeiter (in €)",
                value: "budgetProMitarbeiter",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearPastDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "einkommensgleichheit",
        label: "Einkommensgleichheit",
        fields: [
          {
            name: "ueberwachungDerEinkommensungleichheit",
            label: "Überwachung der Einkommensungleichheit",
            description:
              "Bitte geben Sie das unbereinigte geschlechtsspezifische Lohngefälle, das Einkommensungleichheitsverhältnis, sowie das CEO-Einkommensungleichheitsverhältnis für die letzten drei Jahre an.",
            options: [
              {
                label: "Unbereinigtes geschlechtsspezifisches Lohngefälle (in %)",
                value: "unbereinigtesGeschlechtsspezifischesLohngefaelle",
              },
              {
                label: "Einkommensungleichheitsverhältnis (in %)",
                value: "einkommensungleichheitsverhaeltnis",
              },
              {
                label: "CEO-Einkommensungleichheitsverhältnis (in %)",
                value: "ceoEinkommensungleichheitsverhaeltnis",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesThreeYearPastDataFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "massnahmenZurVerbesserungDerEinkommensungleichheit",
            label: "Maßnahmen zur Verbesserung der Einkommensungleichheit",
            description:
              "Wie überwacht das Unternehmen die Einkommens(un)gleichheit und welche Maßnahmen wurden ergriffen, um die Einkommensungleichheit abzustellen?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "geschlechterdiversitaet",
        label: "Geschlechterdiversität",
        fields: [
          {
            name: "mitarbeiterAufTopManagementEbene",
            label: "Mitarbeiter auf Top-Management Ebene",
            description: "Geben Sie bitte an wieviele Personen eine Top-Management Position innehaben.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "frauenAufTopManagementEbene",
            label: "Frauen auf Top-Management-Ebene",
            description: "Geben Sie bitte an wieviele Frauen eine Top-Management Position innehaben.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "mitgliederGeschaeftsfuehrung",
            label: "Mitglieder Geschäftsführung",
            description: "Geben Sie bitte an wieviele Mitglieder die Geschäftsführung hat.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "frauenInDerGeschaeftsfuehrung",
            label: "Frauen in der Geschäftsführung",
            description: "Geben Sie bitte an wieviele Frauen in der Geschäftsführung sind.",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "definitionTopManagement",
            label: "Definition Top-Management",
            description: 'Bitte geben Sie Ihre Definition von "Top-Management".',
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "einhaltungRechtlicherVorgaben",
            label: "Einhaltung rechtlicher Vorgaben",
            description:
              "Welche Maßnahmen wurden ergriffen, um das geltende Recht in Bezug auf die Geschlechterdiversität von Exekutivinstanzen einzuhalten?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "audit",
        label: "Audit",
        fields: [
          {
            name: "auditsZurEinhaltungVonArbeitsstandards",
            label: "Audits zur Einhaltung von Arbeitsstandards",
            description:
              "Führt das Unternehmen interne oder externe Audits durch, um die Einhaltung der Arbeitsnormen durch das Unternehmen zu bewerten?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "artDesAudits",
            label: "Art des Audits",
            description: "Wie werden die Audits zur Einhaltung von Arbeitsstandards durchgeführt?",
            options: [
              {
                label: "Interne Anhörung",
                value: "InterneAnhoerung",
              },
              {
                label: "Prüfung durch Dritte",
                value: "PruefungDurchDritte",
              },
              {
                label: "Sowohl intern als auch von Drittanbietern",
                value: "SowohlInternAlsAuchVonDrittanbietern",
              },
            ],
            unit: "",
            component: "SingleSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.audit?.auditsZurEinhaltungVonArbeitsstandards == "Yes",
            validation: "",
          },
          {
            name: "auditErgebnisse",
            label: "Audit Ergebnisse",
            description: "Bitte geben Sie Informationen über das letzte Audit an.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.soziales?.audit?.auditsZurEinhaltungVonArbeitsstandards == "Yes",
            validation: "",
          },
        ],
      },
    ],
  },
  {
    name: "unternehmensfuehrungGovernance",
    label: "Unternehmensführung/ Governance",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "aufsichtsrat",
        label: "Aufsichtsrat",
        fields: [
          {
            name: "anzahlDerMitgliederImAufsichtsrat",
            label: "Anzahl der Mitglieder im Aufsichtsrat",
            description: "Wieviele Mitglieder hat der Aufsichtsrat?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlUnabhaengigerMitgliederImAufsichtsrat",
            label: "Anzahl unabhängiger Mitglieder im Aufsichtsrat",
            description: "Wieviele unabhängige Mitglieder hat der Aufsichtsrat?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlVonFrauenImAufsichtsrat",
            label: "Anzahl von Frauen im Aufsichtsrat",
            description: "Wieviele Frauen sind im Aufsichtsrat?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "verguetungsausschuss",
        label: "Vergütungsausschuss",
        fields: [
          {
            name: "anzahlDerMitgliederImVerguetungsausschuss",
            label: "Anzahl der Mitglieder im Vergütungsausschuss",
            description: "Wieviele Mitglieder hat der Vergütungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlUnabhaengigerMitgliederImVerguetungsausschuss",
            label: "Anzahl unabhängiger Mitglieder im Vergütungsausschuss",
            description: "Wieviele unabhängige Mitglieder hat der Vergütungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlVonFrauenImVerguetungsausschuss",
            label: "Anzahl von Frauen im Vergütungsausschuss",
            description: "Wieviele Frauen sind im Vergütungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "nominierungsausschuss",
        label: "Nominierungsausschuss",
        fields: [
          {
            name: "anzahlDerMitgliederImNominierungsausschuss",
            label: "Anzahl der Mitglieder im Nominierungsausschuss",
            description: "Wieviele Mitglieder hat der Nominierungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlUnabhaengigerMitgliederImNominierungsausschuss",
            label: "Anzahl unabhängiger Mitglieder im Nominierungsausschuss",
            description: "Wieviele unabhängige Mitglieder hat der Nominierungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlVonFrauenImVerguetungsausschuss",
            label: "Anzahl von Frauen im Vergütungsausschuss",
            description: "Wieviele Frauen sind im Nominierungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "pruefungsausschuss",
        label: "Prüfungsausschuss",
        fields: [
          {
            name: "anzahlDerMitgliederImPruefungsausschuss",
            label: "Anzahl der Mitglieder im Prüfungsausschuss",
            description: "Wieviele Mitglieder hat der Prüfungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlUnabhaengigerMitgliederImPruefungsausschuss",
            label: "Anzahl unabhängiger Mitglieder im Prüfungsausschuss",
            description: "Wieviele unabhängige Mitglieder hat der Prüfungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlVonFrauenImPruefungsausschuss",
            label: "Anzahl von Frauen im Prüfungsausschuss",
            description: "Wieviele Frauen sind im Prüfungsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "nachhaltigkeitsausschuss",
        label: "Nachhaltigkeitsausschuss",
        fields: [
          {
            name: "anzahlDerMitgliederImNachhaltigkeitsausschuss",
            label: "Anzahl der Mitglieder im Nachhaltigkeitsausschuss",
            description: "Wieviele Mitglieder hat der Nachhaltigkeitsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlUnabhaengigerMitgliederImNachhaltigkeitsausschuss",
            label: "Anzahl unabhängiger Mitglieder im Nachhaltigkeitsausschuss",
            description: "Wieviele unabhängige Mitglieder hat der Nachhaltigkeitsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "anzahlVonFrauenImNachhaltigkeitsausschuss",
            label: "Anzahl von Frauen im Nachhaltigkeitsausschuss",
            description: "Wieviele Frauen sind im Nachhaltigkeitsausschuss?",
            options: "",
            unit: "",
            component: "NumberFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "sonstige",
        label: "Sonstige",
        fields: [
          {
            name: "wirtschaftspruefer",
            label: "Wirtschaftsprüfer",

            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "trennungVonCeoOderVorsitzenden",
            label: "Trennung von CEO oder Vorsitzenden",
            description:
              "Hat sich das Unternehmen im aktuellen Jahr der Berichterstattung von CEO/Vorsitzenden getrennt?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "amtszeitBisZurTrennung",
            label: "Amtszeit bis zur Trennung",
            description: "Wieviele Jahre war der/die CEO/Vorsitzende(r) im Amt?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.unternehmensfuehrungGovernance?.sonstige?.trennungVonCeoOderVorsitzenden == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "stakeholder",
        label: "Stakeholder",
        fields: [
          {
            name: "einbeziehungVonStakeholdern",
            label: "Einbeziehung von Stakeholdern",
            description:
              "Gibt es einen kontinuierlichen Prozess des Dialogs mit den Stakeholdern des Unternehmens? Bitte geben Sie Einzelheiten zu einem solchen Prozess an, z.B. eine Umfrage zur Bewertung der Mitarbeiter- oder Kundenzufriedenheit. Falls zutreffend, teilen Sie uns bitte die wichtigsten Schlussfolgerungen mit.",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "prozessDerEinbeziehungVonStakeholdern",
            label: "Prozess der Einbeziehung von Stakeholdern",
            description:
              "Bitte geben Sie Einzelheiten zu einem solchen Prozess an, z.B. eine Umfrage zur Bewertung der Mitarbeiter- oder Kundenzufriedenheit. Falls zutreffend, teilen Sie uns bitte die wichtigsten Schlussfolgerungen mit.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.unternehmensfuehrungGovernance?.stakeholder?.einbeziehungVonStakeholdern == "Yes",
            validation: "",
          },
          {
            name: "mechanismenZurAusrichtungAufStakeholder",
            label: "Mechanismen zur Ausrichtung auf Stakeholder",
            description:
              "Welche Mechanismen gibt es derzeit, um sicherzustellen, dass die Stakeholder im besten Interesse des Unternehmens handeln? Bitte erläutern Sie (falls zutreffend) die Beteiligungsmechanismen, verschiedene Anreizsysteme usw.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.unternehmensfuehrungGovernance?.stakeholder?.einbeziehungVonStakeholdern == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "unternehmensrichtlinien",
        label: "Unternehmensrichtlinien",
        fields: [
          {
            name: "veroeffentlichteUnternehmensrichtlinien",
            label: "Veröffentlichte Unternehmensrichtlinien",
            description: "Welche Richtlinien sind im Unternehmen veröffentlicht?",
            options: [
              {
                label: "Anti-Korruption",
                value: "AntiKorruption",
              },
              {
                label: "Verhaltenskodex",
                value: "Verhaltenskodex",
              },
              {
                label: "Interessenkonflikte",
                value: "Interessenkonflikte",
              },
              {
                label: "Datenschutz",
                value: "Datenschutz",
              },
              {
                label: "Diversität & Inklusion",
                value: "DiversitaetAndInklusion",
              },
              {
                label: "Faire Behandlung von Kunden",
                value: "FaireBehandlungVonKunden",
              },
              {
                label: "Zwangsarbeit",
                value: "Zwangsarbeit",
              },
              {
                label: "Gesundheit und Sicherheit",
                value: "GesundheitUndSicherheit",
              },
              {
                label: "Mgt von Umweltgefahren",
                value: "MgtVonUmweltgefahren",
              },
              {
                label: "Verantwortungsvolles Marketing",
                value: "VerantwortungsvollesMarketing",
              },
              {
                label: "Whistleblowing",
                value: "Whistleblowing",
              },
              {
                label: "other",
                value: "Other",
              },
            ],
            unit: "",
            component: "MultiSelectFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "weitereVeroeffentlicheUnternehmensrichtlinien",
            label: "Weitere veröffentliche Unternehmensrichtlinien",
            description: "Bitte nennen Sie weitere wichtige Richtlinien, falls diese nicht angegeben sind.",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
        ],
      },
      {
        name: "lieferantenauswahl",
        label: "Lieferantenauswahl",
        fields: [
          {
            name: "esgKriterienUndUeberwachungDerLieferanten",
            label: "ESG-Kriterien und Überwachung der Lieferanten",
            description:
              "Wendet das Unternehmen ESG-Kriterien bei der Auswahl seiner Lieferanten an, einschließlich einer Bestandsaufnahme der Lieferkette?",
            options: "",
            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (dataset: GdvData): boolean => dataset.general?.masterData?.berichtsPflicht == "Yes",
            validation: "",
          },
          {
            name: "auswahlkriterien",
            label: "Auswahlkriterien",
            description:
              "Bitte nennen Sie die Auswahlkriterien und erläutern Sie, wie diese Kriterien im Laufe der Zeit überwacht/geprüft werden. Bezieht das Unternehmen beispielsweise Rohstoffe aus Gebieten, in denen umstrittene Abholzungsaktivitäten stattfinden (z.B. Soja, Palmöl, Tropenholz, Holz oder industrielle Viehzucht)?",
            options: "",
            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (dataset: GdvData): boolean =>
              dataset.unternehmensfuehrungGovernance?.lieferantenauswahl?.esgKriterienUndUeberwachungDerLieferanten ==
              "Yes",
            validation: "",
          },
        ],
      },
    ],
  },
] as Category[];
