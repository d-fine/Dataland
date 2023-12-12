import { type Category } from "@/utils/GenericFrameworkTypes";

export const gdvDataModel: Category[] = [
  {
    name: "general",
    label: "General",
    color: " ",
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

            unit: "",
            component: "YesNoFormField",
            required: true,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "gueltigkeitsDatum",
            label: "(Gültigkeits) Datum",
            description: "Datum bis wann die Information gültig ist",

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
    color: " ",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "esgZiele",
        label: "ESG-Ziele",
        fields: [],
      },
      {
        name: "ziele",
        label: "Ziele",
        fields: [],
      },
      {
        name: "investitionen",
        label: "Investitionen",
        fields: [],
      },
      {
        name: "sektorMitHohenKlimaauswirkungen",
        label: "Sektor mit hohen Klimaauswirkungen",
        fields: [],
      },
      {
        name: "sektor",
        label: "Sektor",
        fields: [],
      },
      {
        name: "nachhaltigkeitsbericht",
        label: "Nachhaltigkeitsbericht",
        fields: [],
      },
      {
        name: "frequenzDerBerichterstattung",
        label: "Frequenz der Berichterstattung",
        fields: [],
      },
      {
        name: "allgemein",
        label: "Allgemein",
        fields: [
          {
            name: "aktuelleBerichte",
            label: "Aktuelle Berichte",
            description: "Bitte teilen Sie die letzten Berichte mit uns (vorzugsweise die letzten drei).",

            unit: "",
            component: "ListOfBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "iso14001",
        label: "ISO 14001",
        fields: [],
      },
      {
        name: "iso45001",
        label: "ISO 45001",
        fields: [],
      },
      {
        name: "iso27001",
        label: "ISO 27001",
        fields: [],
      },
      {
        name: "iso50001",
        label: "ISO 50001",
        fields: [],
      },
      {
        name: "mechanismenZurUeberwachungDerEinhaltungUnGlobalCompactPrinzipienUndOderOecdLeitsaetze",
        label: "Mechanismen zur Überwachung der Einhaltung UN Global Compact Prinzipien und/oder OECD Leitsätze",
        fields: [],
      },
      {
        name: "erklaerungUngc",
        label: "Erklärung UNGC",
        fields: [],
      },
      {
        name: "oecdLeitsaetze",
        label: "OECD Leitsätze",
        fields: [],
      },
      {
        name: "richtlinienEinhaltungOecd",
        label: "Richtlinien Einhaltung OECD",
        fields: [],
      },
      {
        name: "erklaerungOecd",
        label: "Erklärung OECD",
        fields: [],
      },
      {
        name: "ausrichtungAufDieUnSdgsUndAktivesVerfolgen",
        label: "Ausrichtung auf die UN SDGs und aktives Verfolgen",
        fields: [],
      },
      {
        name: "ausschlusslistenAufBasisVonEsgKriterien",
        label: "Ausschlusslisten auf Basis von ESG Kriterien",
        fields: [],
      },
      {
        name: "ausschlusslisten",
        label: "Ausschlusslisten",
        fields: [],
      },
      {
        name: "oekologischeSozialeFuehrungsstandardsOderPrinzipien",
        label: "Ökologische/soziale Führungsstandards oder -prinzipien",
        fields: [],
      },
      {
        name: "anreizmechanismenFuerDasManagementUmwelt",
        label: "Anreizmechanismen für das Management (Umwelt)",
        fields: [],
      },
      {
        name: "anreizmechanismenFuerDasManagementSoziales",
        label: "Anreizmechanismen für das Management (Soziales)",
        fields: [],
      },
      {
        name: "esgBezogeneRechtsstreitigkeiten",
        label: "ESG-bezogene Rechtsstreitigkeiten",
        fields: [],
      },
      {
        name: "rechtsstreitigkeitenMitBezugZuE",
        label: "Rechtsstreitigkeiten mit Bezug zu E",
        fields: [],
      },
      {
        name: "statusZuE",
        label: "Status zu E",
        fields: [],
      },
      {
        name: "einzelheitenZuDenRechtsstreitigkeitenZuE",
        label: "Einzelheiten zu den Rechtsstreitigkeiten zu E",
        fields: [],
      },
      {
        name: "rechtsstreitigkeitenMitBezugZuS",
        label: "Rechtsstreitigkeiten mit Bezug zu S",
        fields: [],
      },
      {
        name: "statusZuS",
        label: "Status zu S",
        fields: [],
      },
      {
        name: "einzelheitenZuDenRechtsstreitigkeitenZuS",
        label: "Einzelheiten zu den Rechtsstreitigkeiten zu S",
        fields: [],
      },
      {
        name: "rechtsstreitigkeitenMitBezugZuG",
        label: "Rechtsstreitigkeiten mit Bezug zu G",
        fields: [],
      },
      {
        name: "statusZuG",
        label: "Status zu G",
        fields: [],
      },
      {
        name: "einzelheitenZuDenRechtsstreitigkeitenZuG",
        label: "Einzelheiten zu den Rechtsstreitigkeiten zu G",
        fields: [],
      },
      {
        name: "esgRating",
        label: "ESG-Rating",
        fields: [],
      },
      {
        name: "agentur",
        label: "Agentur",
        fields: [],
      },
      {
        name: "ergebnis",
        label: "Ergebnis",
        fields: [],
      },
      {
        name: "kritischePunkte",
        label: "Kritische Punkte",
        fields: [],
      },
      {
        name: "nachhaltigkeitsbezogenenAnleihen",
        label: "Nachhaltigkeitsbezogenen Anleihen",
        fields: [],
      },
      {
        name: "wichtigsteESUndGRisikenUndBewertung",
        label: "Wichtigste E-, S- und G-Risiken und Bewertung",
        fields: [],
      },
      {
        name: "hindernisseBeimUmgangMitEsgBedenken",
        label: "Hindernisse beim Umgang mit ESG-Bedenken",
        fields: [],
      },
    ],
  },
  {
    name: "umwelt",
    label: "Umwelt",
    color: " ",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "treibhausgasemissionen",
        label: "Treibhausgasemissionen",
        fields: [
          {
            name: "treibhausgasEmissionsintensitaetDerUnternehmenInDieInvestriertWird",
            label: "Treibhausgas-Emissionsintensität der Unternehmen, in die investriert wird",
            description:
              "THG-Emissionsintensität der Unternehmen, in die investiert wird. Scope 1 + Scope 2 Treibhausgasemissionen ./. Umsatz in Millionen EUR Scope 1 + Scope 2 Treibhausgasemissionen ./. Unternehmensgröße in Mio. EUR",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "strategieUndZieleZurReduzierungVonTreibhausgasEmissionen",
            label: "Strategie und Ziele zur Reduzierung von Treibhausgas-Emissionen",
            description:
              "Welchen Entwicklungspfad bzgl. der (Reduktion von) Treibhausgasemissionen verfolgt das Unternehmen. Gibt es einen Zeitplan bzw. konkrete Ziele? Und wie plant das Unternehmen, diesen Kurs zu erreichen? Bitte erläutern Sie, in welchem Bezug dieser Entwicklungspfad zu dem auf dem Pariser Abkommen basierenden Kurs steht.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "treibhausgasBerichterstattungUndPrognosen",
            label: "Treibhausgas-Berichterstattung und Prognosen",
            description:
              "Welche Treibhausgasinformationen werden derzeit auf Unternehmens-/Konzernebene berichtet und prognostiziert? Bitte geben Sie die Scope1, Scope 2 und Scope 3 Emissionen# für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an (in tCO2-Äquiv.).",
            options: [
              {
                label: "Scope 1",
                value: "scope1",
              },
              {
                label: "Scope 2",
                value: "scope2",
              },
              {
                label: "Scope 3",
                value: "scope3",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "produkteZurVerringerungDerUmweltbelastung",
        label: "Produkte zur Verringerung der Umweltbelastung",
        fields: [],
      },
      {
        name: "verringerungenDerUmweltbelastung",
        label: "Verringerungen der Umweltbelastung",
        fields: [],
      },
      {
        name: "oekologischerMindestStandardFuerProduktionsprozesse",
        label: "Ökologischer Mindest-Standard für Produktionsprozesse",
        fields: [],
      },
      {
        name: "energieverbrauch",
        label: "Energieverbrauch",
        fields: [
          {
            name: "unternehmensGruppenStrategieBzglEnergieverbrauch",
            label: "Unternehmens/Gruppen Strategie bzgl Energieverbrauch",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "berichterstattungEnergieverbrauch",
            label: "Berichterstattung Energieverbrauch",
            description:
              "Bitte geben Sie den Energieverbrauch (in GWh), sowie den Verbrauch erneuerbaren Energien (%) und, falls zutreffend, die Erzeugung erneuerbaren Energien (%) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Energieverbrauch",
                value: "energieverbrauch",
              },
              {
                label: "Verbrauch erneuerbare Energien",
                value: "verbrauchErneuerbareEnergien",
              },
              {
                label: "Erzeugung erneuerbare Energien",
                value: "erzeugungErneuerbareEnergien",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "energieeffizienzImmobilienanlagen",
        label: "Energieeffizienz Immobilienanlagen",
        fields: [
          {
            name: "unternehmensGruppenStrategieBzglEnergieeffizientenImmobilienanlagen",
            label: "Unternehmens/Gruppen Strategie bzgl energieeffizienten Immobilienanlagen",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "berichterstattungEnergieverbrauchVonImmobilienvermoegen",
            label: "Berichterstattung Energieverbrauch von Immobilienvermoegen",
            description:
              "Bitte geben Sie den Anteil an energieeffizienten Immobilienanlagen (%) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "energieeffiziente Immobilienanlagen",
                value: "energieeffizienteImmobilienanlagen",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "wasserverbrauch",
        label: "Wasserverbrauch",
        fields: [
          {
            name: "unternehmensGruppenStrategieBzglWasserverbrauch",
            label: "Unternehmens/Gruppen Strategie bzgl Wasserverbrauch",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "berichterstattungWasserverbrauch",
            label: "Berichterstattung Wasserverbrauch",
            description:
              "Bitte geben Sie den Wasserverbrauch (in l), sowie die Emissionen in Wasser (in Tonnen) für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Wasserverbrauch",
                value: "wasserverbrauch",
              },
              {
                label: "Emissionen in Wasser",
                value: "emissionenInWasser",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "abfallproduktion",
        label: "Abfallproduktion",
        fields: [
          {
            name: "unternehmensGruppenStrategieBzglAbfallproduktion",
            label: "Unternehmens/Gruppen Strategie bzgl Abfallproduktion",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "berichterstattungAbfallproduktion",
            label: "Berichterstattung Abfallproduktion",
            description:
              "Bitte geben Sie die gesamte Abfallmenge (in Tonnen), sowie den Anteil (%) der gesamten Abfallmenge, der recyclet wird, sowie den Anteil (%) gefährlicher Abfall der gesamten Abfallmenge für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Abfallmenge",
                value: "abfallmenge",
              },
              {
                label: "Anteil der recycelten Abfallmenge",
                value: "anteilRecycelterAbfallmenge",
              },
              {
                label: "Anteil gefährlicher Abfall an Gesamtmenge",
                value: "anteilGefaehrlicherAbfallmenge",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "recyclingImProduktionsprozess",
            label: "Recycling im Produktionsprozess",
            description:
              "Bitte geben Sie an, wie hoch der Anteil an Recyclaten (bereitsrecyceltes wiederverwertetes Material) im Produktionsprozess für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre.",
            options: [
              {
                label: "Anteil an Recyclaten",
                value: "anteilAnRecyclaten",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "gefaehrlicheAbfaelle",
        label: "Gefährliche Abfälle",
        fields: [
          {
            name: "gefaehrlicherAbfall",
            label: "Gefährlicher Abfall",
            description:
              "Wie wird in dem Unternehmen während der Produktion und Verarbeitung mit gefährlichen Abfällen (brennbar, reaktiv, giftig, radioaktiv) umgegangen?",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
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

            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "negativeMassnahmenFuerDieBiologischeVielfalt",
            label: "Negative Maßnahmen für die biologische Vielfalt",
            description:
              "Bitte erläutern Sie Aktivitäten, die sich negativ auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für den Umgang mit diesen Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "positiveAktivitaetenFuerDieBiologischeVielfalt",
            label: "Positive Aktivitäten für die biologische Vielfalt",
            description:
              "Hat das Unternehmen Standorte / Betriebe in oder in der Nähe von biodiversitätssensiblen Gebieten, in denen sich die Aktivitäten des Unternehmens positiv auf diese Gebiete auswirken?",

            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "positiveMassnahmenFuerDieBiologischeVielfalt",
            label: "Positive Maßnahmen für die biologische Vielfalt",
            description:
              "Bitte erläutern Sie Aktivitäten, die sich positiv auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für die Weiterentwicklung dieser Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
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

            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "berichterstattungEinnahmenAusFossilenBrennstoffen",
            label: "Berichterstattung Einnahmen aus fossilen Brennstoffen",
            description:
              "Bitte geben Sie den Anteil (%) der Einnahmen aus fossilen Brennstoffen aus den gesamten Einnahmen für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Anteil der Einnahmen aus fossilen Brennstoffen",
                value: "anteilEinnahmenAusFossilenBrennstoffen",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
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
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "euTaxonomieKompassAktivitaeten",
            label: "EU Taxonomie Kompass Aktivitäten",
            description: "Welche Aktivitäten gem. dem EU Taxonomie-Kompass übt das Unternehmen aus?",
            options: [
              {
                label: "EuTaxonomyActivityOptions",
                value: "Eutaxonomyactivityoptions",
              },
            ],
            unit: "",
            component: "MultiSelectFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "umsatzInvestitionsaufwandFuerNachhaltigeAktivitaeten",
            label: "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten",
            description:
              "Wie hoch ist der Umsatz/Investitionsaufwand des Unternehmens aus nachhaltigen Aktivitäten (Mio. €) gemäß einer Definition der EU-Taxonomie? Bitte machen Sie Angaben zu den betrachteten Sektoren und gegebenenfalls zu den Annahmen bzgl. Taxonomie-konformen (aligned) Aktivitäten für das aktuelle Kalenderjahr, die letzten drei Jahren sowie die Prognosen für die kommenden drei Jahre an.",
            options: [
              {
                label: "Umsatz/Investitionsaufwand für nachhaltige Aktivitäten",
                value: "umsatzInvestitionsaufwandAusNachhaltigenAktivitaeten",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
    ],
  },
  {
    name: "soziales",
    label: "Soziales",
    color: " ",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "aenderungenUnternehmensstruktur",
        label: "Änderungen Unternehmensstruktur",
        fields: [],
      },
      {
        name: "sicherheitsmassnahmenFuerMitarbeiter",
        label: "Sicherheitsmaßnahmen für Mitarbeiter",
        fields: [],
      },
      {
        name: "einkommensgleichheit",
        label: "Einkommensgleichheit",
        fields: [
          {
            name: "massnahmenZurVerbesserungDerEinkommensungleichheit",
            label: "Maßnahmen zur Verbesserung der Einkommensungleichheit",
            description:
              "Wie überwacht das Unternehmen die Einkommens(un)gleichheit und welche Maßnahmen wurden ergriffen, um die Einkommensungleichheit abzustellen?",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "ueberwachungDerEinkommensungleichheit",
            label: "Überwachung der Einkommensungleichheit",
            description:
              "Bitte geben Sie das unbereinigte geschlechtsspezifische Lohngefälle, das Einkommensungleichheitsverhältnis, sowie das CEO-Einkommensungleichheitsverhältnis für die letzten drei Jahre an.",
            options: [
              {
                label: "Geschlechtsspezifisches Lohngefälle",
                value: "geschlechtsspezifischesLohngefaelle",
              },
              {
                label: "Einkommensungleichheitsverhältnis",
                value: "einkommensungleichheitsverhaeltnis",
              },
              {
                label: "CEO-Einkommensungleichheitsverhältnis",
                value: "ceoEinkommenungleichheitsverhaeltnis",
              },
            ],
            unit: "",
            component: "GdvYearlyDecimalTimeseriesDataFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "geschlechterdiversitaet",
        label: "Geschlechterdiversität",
        fields: [
          {
            name: "definitionTopManagement",
            label: "Definition Top-Management",
            description: 'Bitte geben Sie Ihre Definition von "Top-Management".',

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "einhaltungRechtlicherVorgaben",
            label: "Einhaltung rechtlicher Vorgaben",
            description:
              "Welche Maßnahmen wurden ergriffen, um das geltende Recht in Bezug auf die Geschlechterdiversität von Exekutivinstanzen einzuhalten?",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
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

            unit: "",
            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "artDesAudits",
            label: "Art des Audits",
            description: "Art des Audits",
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
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "auditErgebnisse",
            label: "Audit Ergebnisse",
            description: "Bitte geben Sie Informationen über das letzte Audit an.",

            unit: "",
            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "anzahlDerBetroffenenMitarbeiter",
        label: "Anzahl der betroffenen Mitarbeiter",
        fields: [],
      },
      {
        name: "auswirkungenAufAnteilBefristerVertraegeUndFluktuation",
        label: "Auswirkungen auf Anteil befrister Verträge und Fluktuation",
        fields: [],
      },
      {
        name: "budgetFuerSchulungAusbildung",
        label: "Budget für Schulung/Ausbildung",
        fields: [],
      },
    ],
  },
  {
    name: "unternehmensfuehrungGovernance",
    label: "Unternehmensführung/ Governance",
    color: " ",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "wirtschaftspruefer",
        label: "Wirtschaftsprüfer",
        fields: [],
      },
      {
        name: "ceoVorsitzender",
        label: "CEO/Vorsitzender",
        fields: [],
      },
      {
        name: "amtszeit",
        label: "Amtszeit",
        fields: [],
      },
      {
        name: "einbeziehungVonStakeholdern",
        label: "Einbeziehung von Stakeholdern",
        fields: [],
      },
      {
        name: "prozessDerEinbeziehungVonStakeholdern",
        label: "Prozess der Einbeziehung von Stakeholdern",
        fields: [],
      },
      {
        name: "mechanismenZurAusrichtungAufStakeholder",
        label: "Mechanismen zur Ausrichtung auf Stakeholder",
        fields: [],
      },
      {
        name: "veroeffentlichteUnternehmensrichtlinien",
        label: "Veröffentlichte Unternehmensrichtlinien",
        fields: [],
      },
      {
        name: "esgKriterienUndUeberwachungDerLieferanten",
        label: "ESG-Kriterien und Überwachung der Lieferanten",
        fields: [],
      },
      {
        name: "auswahlkriterien",
        label: "Auswahlkriterien",
        fields: [],
      },
    ],
  },
];
