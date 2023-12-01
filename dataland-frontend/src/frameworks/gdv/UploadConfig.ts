export const gdvDataModel = [
  {
    name: "general",
    label: "General",
    color: " ",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "masterData",
        label: "Master Data",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "berichts-pflicht",
            label: "Berichts-Pflicht",
            description: "Ist das Unternehmen berichtspflichtig?",

            component: "",
            required: "",
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
        name: "esg-ziele",
        label: "ESG-Ziele",
        description:
          "Hat das Unternehmen spezifische ESG-Ziele/Engagements? Werden bspw. spezifische Ziele / Maßnahmen ergriffen, um das 1,5 Grad Ziel zu erreichen?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ziele",
        label: "Ziele",
        description: "Bitte geben Sie eine genaue Beschreibung der ESG-Ziele.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "investitionen",
        label: "Investitionen",
        description:
          "Bitte geben Sie an wieviele Budgets/Vollzeitäquivalente für das Erreichen der ESG-Ziele zugewiesen wurden.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "sektorMitHohenKlimaauswirkungen",
        label: "Sektor mit hohen Klimaauswirkungen",
        description: "Kann das Unternehmen einem oder mehreren Sektoren mit hohen Klimaauswirkungen zugeordnet werden?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "nachhaltigkeitsbericht",
        label: "Nachhaltigkeitsbericht",
        description: "Erstellt das Unternehmen Nachhaltigkeits- oder ESG-Berichte?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "mechanismenZurÜberwachungDerEinhaltungUNGlobalCompactPrinzipienUnd/oderOECDLeitsätze",
        label: "Mechanismen zur Überwachung der Einhaltung UN Global Compact Prinzipien und/oder OECD Leitsätze",
        description:
          "Verfügt das Unternehmen über Prozesse und Compliance-Mechanismen, um die Einhaltung der Prinzipien des UN Global Compact und/oder der OECD-Leitsätze für multinationale Unternehmen (OECD MNE-Leitsätze) zu überwachen?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "uncgPrinzipien",
        label: "UNCG Prinzipien",
        description: "Hat das Unternehmen Überwachungsmechanismen für die UNGC Prinzipien eingerichtet ?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "erklärungUNGC",
        label: "Erklärung UNGC",
        description: "Bitte geben Sie eine Erklärung ab, dass keine Verstöße gegen diese Grundsätze vorliegen.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "oecdLeitsätze",
        label: "OECD Leitsätze",
        description: "Hat das Unternehmen Überwachungsmechanismen für die OECD Leitsätze eingerichtet ?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "erklärungOECD",
        label: "Erklärung OECD",
        description: "Bitte geben Sie eine Erklärung ab, dass keine Verstöße gegen diese Grundsätze vorliegen.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ausrichtungAufDieUNSDGsUndAktivesVerfolgen",
        label: "Ausrichtung auf die UN SDGs und aktives Verfolgen",
        description:
          "Wie steht das Unternehmen in Einklang mit den 17 UN-Zielen für nachhaltige Entwicklung? Welche dieser Ziele verfolgt das Unternehmen aktiv, entweder durch ihre Geschäftstätigkeit oder durch die Unternehmensführung?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ausschlusslistenAufBasisVonESGKriterien",
        label: "Ausschlusslisten auf Basis von ESG Kriterien",
        description:
          "Führt das Unternehmen Ausschlusslisten? Von besonderem Interesse sind Listen die Ausschlusskriterien, die einen Bezug zu den Bereichen E, S oder G haben.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ausschlusslisten",
        label: "Ausschlusslisten",
        description: "Bitte nennen Sie die Ausschlusslisten auf Basis von ESG Kriterien.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ökologische/sozialeFührungsstandardsOder-prinzipien",
        label: "Ökologische/soziale Führungsstandards oder -prinzipien",
        description:
          "Hat sich das Unternehmen zu ökologischen/sozialen Führungsstandards oder Prinzipien verpflichtet?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "esg-bezogeneRechtsstreitigkeiten",
        label: "ESG-bezogene Rechtsstreitigkeiten",
        description:
          "Ist das Unternehmen in laufende bzw. war das Unternehmen in den letzten 3 Jahren in abgeschlossenen Rechtsstreitigkeiten im Zusammenhang mit ESG involviert?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "rechtsstreitigkeitenMitBezugZuE",
        label: "Rechtsstreitigkeiten mit Bezug zu E",
        description: 'Haben bzw. hatten die Rechtsstreitigkeiten Bezug zu "E"',

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "einzelheitenZuDenRechtsstreitigkeitenZuE",
        label: "Einzelheiten zu den Rechtsstreitigkeiten zu E",
        description: "Bitte erläutern Sie Einzelheiten zu den Rechtsstreitigkeiten.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "rechtsstreitigkeitenMitBezugZuS",
        label: "Rechtsstreitigkeiten mit Bezug zu S",
        description: 'Haben bzw. hatten die Rechtsstreitigkeiten Bezug zu "S"',

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "einzelheitenZuDenRechtsstreitigkeitenZuS",
        label: "Einzelheiten zu den Rechtsstreitigkeiten zu S",
        description: "Bitte erläutern Sie Einzelheiten zu den Rechtsstreitigkeiten.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "rechtsstreitigkeitenMitBezugZuG",
        label: "Rechtsstreitigkeiten mit Bezug zu G",
        description: 'Haben bzw. hatten die Rechtsstreitigkeiten Bezug zu "G"',

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "einzelheitenZuDenRechtsstreitigkeitenZuG",
        label: "Einzelheiten zu den Rechtsstreitigkeiten zu G",
        description: "Bitte erläutern Sie Einzelheiten zu den Rechtsstreitigkeiten.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "esg-rating",
        label: "ESG-Rating",
        description: "Hat das Unternehmen bereits ein ESG-Rating einer anerkannten Ratingagentur?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "agentur",
        label: "Agentur",
        description: "Welche Rating Agentur hat das Rating durchgeführt?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ergebnis",
        label: "Ergebnis",
        description: "Wie lautet das Rating (Ratingbericht bitte anfügen)?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "kritischePunkte",
        label: "Kritische Punkte",
        description: "Was waren die kritischen Punkte beim ESG-Rating?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "nachhaltigkeitsbezogenenAnleihen",
        label: "Nachhaltigkeitsbezogenen Anleihen",
        description:
          "Hat das Unternehmen „grüne“, „soziale“ und/oder „nachhaltige“ Schuldtitel begeben oder Sustainability Linked Debt („SLD“) emittiert?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "wichtigsteE-,S-UndG-RisikenUndBewertung",
        label: "Wichtigste E-, S- und G-Risiken und Bewertung",
        description:
          "Welches sind die wichtigsten von der Gruppe identifizierten E-, S- und G-Risiken? Bitte geben Sie die Details / Bewertung der identifizierten Risiken an.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "hindernisseBeimUmgangMitESG-Bedenken",
        label: "Hindernisse beim Umgang mit ESG-Bedenken",
        description:
          "Welche grundsätzlichen Hindernisse bestehen für das Unternehmen bei der Berücksichtigung von ESG-Belangen?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
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
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "treibhausgas-emissionsintensitätDerUnternehmen,InDieInvestriertWird",
            label: "Treibhausgas-Emissionsintensität der Unternehmen, in die investriert wird",
            description:
              "THG-Emissionsintensität der Unternehmen, in die investiert wird. Scope 1 + Scope 2 Treibhausgasemissionen ./. Umsatz in Millionen EUR Scope 1 + Scope 2 Treibhausgasemissionen ./. Unternehmensgröße in Mio. EUR",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "strategieUndZieleZurReduzierungVonTreibhausgas-Emissionen",
            label: "Strategie und Ziele zur Reduzierung von Treibhausgas-Emissionen",
            description:
              "Welchen Entwicklungspfad bzgl. der (Reduktion von) Treibhausgasemissionen verfolgt das Unternehmen. Gibt es einen Zeitplan bzw. konkrete Ziele? Und wie plant das Unternehmen, diesen Kurs zu erreichen? Bitte erläutern Sie, in welchem Bezug dieser Entwicklungspfad zu dem auf dem Pariser Abkommen basierenden Kurs steht.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "produkteZurVerringerungDerUmweltbelastung",
        label: "Produkte zur Verringerung der Umweltbelastung",
        description:
          "Entwickelt, produziert oder vertreibt das Unternehmen Produkte, die die Umweltbelastung verringern?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "verringerungenDerUmweltbelastung",
        label: "Verringerungen der Umweltbelastung",
        description: "Bitte beschreiben Sie möglichst genau, wie die Produkte die Umweltbelastung reduzieren.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ökologischerMindest-StandardFürProduktionsprozesse",
        label: "Ökologischer Mindest-Standard für Produktionsprozesse",
        description:
          "Verfügt das Unternehmen über interne Richtlinien, die einen Mindestumweltstandard im Produktionsprozess sicherstellen?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "energieverbrauch",
        label: "Energieverbrauch",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "unternehmens/gruppenStrategieBzglEnergieverbrauch",
            label: "Unternehmens/Gruppen Strategie bzgl Energieverbrauch",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "energieeffizienzImmobilienanlagen",
        label: "Energieeffizienz Immobilienanlagen",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "unternehmens/gruppenStrategieBzglEnergieeffizientenImmobilienanlagen",
            label: "Unternehmens/Gruppen Strategie bzgl energieeffizienten Immobilienanlagen",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "wasserverbrauch",
        label: "Wasserverbrauch",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "unternehmens/gruppenStrategieBzglWasserverbrauch",
            label: "Unternehmens/Gruppen Strategie bzgl Wasserverbrauch",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "abfallproduktion",
        label: "Abfallproduktion",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "unternehmens/gruppenStrategieBzglAbfallproduktion",
            label: "Unternehmens/Gruppen Strategie bzgl Abfallproduktion",
            description:
              "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "gefährlicheAbfälle",
        label: "Gefährliche Abfälle",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "gefährlicherAbfall",
            label: "Gefährlicher Abfall",
            description:
              "Wie wird in dem Unternehmen während der Produktion und Verarbeitung mit gefährlichen Abfällen (brennbar, reaktiv, giftig, radioaktiv) umgegangen?",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "biodiversität",
        label: "Biodiversität",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "negativeAktivitätenFürDieBiologischeVielfalt",
            label: "Negative Aktivitäten für die biologische Vielfalt",
            description:
              "Hat das Unternehmen Standorte / Betriebe in oder in der Nähe von biodiversitätssensiblen Gebieten, in denen sich die Aktivitäten des Unternehmens negativ auf diese Gebiete auswirken?",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "negativeMaßnahmenFürDieBiologischeVielfalt",
            label: "Negative Maßnahmen für die biologische Vielfalt",
            description:
              "Bitte erläutern Sie Aktivitäten, die sich negativ auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für den Umgang mit diesen Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "positiveAktivitätenFürDieBiologischeVielfalt",
            label: "Positive Aktivitäten für die biologische Vielfalt",
            description:
              "Hat das Unternehmen Standorte / Betriebe in oder in der Nähe von biodiversitätssensiblen Gebieten, in denen sich die Aktivitäten des Unternehmens positiv auf diese Gebiete auswirken?",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "positiveMaßnahmenFürDieBiologischeVielfalt",
            label: "Positive Maßnahmen für die biologische Vielfalt",
            description:
              "Bitte erläutern Sie Aktivitäten, die sich positiv auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für die Weiterentwicklung dieser Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "fossileBrennstoffe",
        label: "Fossile Brennstoffe",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "einnahmenAusFossilenBrennstoffen",
            label: "Einnahmen aus fossilen Brennstoffen",
            description:
              "Erzielt das Unternehmen einen Teil seiner Einnahmen aus Aktivitäten im Bereich fossiler Brennstoffe und/oder besitzt das Unternehmen Immobilien, die an der Gewinnung, Lagerung, dem Transport oder der Herstellung fossiler Brennstoffe beteiligt sind?",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "taxonomie",
        label: "Taxonomie",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [],
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
        name: "änderungenUnternehmensstruktur",
        label: "Änderungen Unternehmensstruktur",
        description:
          "Gab es kürzlich eine Veränderung im Unternehmen / in der Gruppe (Umstrukturierung, Verkauf oder Übernahme)?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "sicherheitsmaßnahmenFürMitarbeiter",
        label: "Sicherheitsmaßnahmen für Mitarbeiter",
        description:
          "Welche Maßnahmen werden ergriffen, um die Gesundheit und Sicherheit der Mitarbeiter des Unternehmens zu verbessern?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "einkommensgleichheit",
        label: "Einkommensgleichheit",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "maßnahmenZurVerbesserungDerEinkommensungleichheit",
            label: "Maßnahmen zur Verbesserung der Einkommensungleichheit",
            description:
              "Wie überwacht das Unternehmen die Einkommens(un)gleichheit und welche Maßnahmen wurden ergriffen, um die Einkommensungleichheit abzustellen?",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "geschlechterdiversität",
        label: "Geschlechterdiversität",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "definitionTop-Management",
            label: "Definition Top-Management",
            description: 'Bitte geben Sie Ihre Definition von "Top-Management".',

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "einhaltungRechtlicherVorgaben",
            label: "Einhaltung rechtlicher Vorgaben",
            description:
              "Welche Maßnahmen wurden ergriffen, um das geltende Recht in Bezug auf die Geschlechterdiversität von Exekutivinstanzen einzuhalten?",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
      {
        name: "audit",
        label: "Audit",
        color: " ",
        showIf: (): boolean => true,
        subcategories: [
          {
            name: "auditsZurEinhaltungVonArbeitsstandards",
            label: "Audits zur Einhaltung von Arbeitsstandards",
            description:
              "Führt das Unternehmen interne oder externe Audits durch, um die Einhaltung der Arbeitsnormen durch das Unternehmen zu bewerten?",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
          {
            name: "auditErgebnisse",
            label: "Audit Ergebnisse",
            description: "Bitte geben Sie Informationen über das letzte Audit an.",

            component: "",
            required: "",
            showIf: (): boolean => true,
            validation: "",
          },
        ],
      },
    ],
  },
  {
    name: "unternehmensführung/Governance",
    label: "Unternehmensführung/ Governance",
    color: " ",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "wirtschaftsprüfer",
        label: "Wirtschaftsprüfer",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "ceo/vorsitzender",
        label: "CEO/Vorsitzender",
        description: "Hat sich das Unternehmen im aktuellen Jahr der Berichterstattung von CEO/Vorsitzenden getrennt?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "amtszeit",
        label: "Amtszeit",
        description: "Wieviele Jahre war der/die CEO/Vorsitzende(r) im Amt?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "einbeziehungVonStakeholdern",
        label: "Einbeziehung von Stakeholdern",
        description:
          "Gibt es einen kontinuierlichen Prozess des Dialogs mit den Stakeholdern des Unternehmens? Bitte geben Sie Einzelheiten zu einem solchen Prozess an, z.B. eine Umfrage zur Bewertung der Mitarbeiter- oder Kundenzufriedenheit. Falls zutreffend, teilen Sie uns bitte die wichtigsten Schlussfolgerungen mit.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "prozessDerEinbeziehungVonStakeholdern",
        label: "Prozess der Einbeziehung von Stakeholdern",
        description:
          "Bitte geben Sie Einzelheiten zu einem solchen Prozess an, z.B. eine Umfrage zur Bewertung der Mitarbeiter- oder Kundenzufriedenheit. Falls zutreffend, teilen Sie uns bitte die wichtigsten Schlussfolgerungen mit.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "mechanismenZurAusrichtungAufStakeholder",
        label: "Mechanismen zur Ausrichtung auf Stakeholder",
        description:
          "Welche Mechanismen gibt es derzeit, um sicherzustellen, dass die Stakeholder im besten Interesse des Unternehmens handeln? Bitte erläutern Sie (falls zutreffend) die Beteiligungsmechanismen, verschiedene Anreizsysteme usw.",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "esg-kriterienUndÜberwachungDerLieferanten",
        label: "ESG-Kriterien und Überwachung der Lieferanten",
        description:
          "Wendet das Unternehmen ESG-Kriterien bei der Auswahl seiner Lieferanten an, einschließlich einer Bestandsaufnahme der Lieferkette?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
      {
        name: "auswahlkriterien",
        label: "Auswahlkriterien",
        description:
          "Bitte nennen Sie die Auswahlkriterien und erläutern Sie, wie diese Kriterien im Laufe der Zeit überwacht/geprüft werden. Bezieht das Unternehmen beispielsweise Rohstoffe aus Gebieten, in denen umstrittene Abholzungsaktivitäten stattfinden (z.B. Soja, Palmöl, Tropenholz, Holz oder industrielle Viehzucht)?",

        component: "",
        required: "",
        showIf: (): boolean => true,
        validation: "",
      },
    ],
  },
];
