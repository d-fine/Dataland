export const gdvDataModel = [    {
        name: "general",
        label: "General",
        color: " ", 
        showIf: ():boolean => true,
        subcategories: [    {
            name: "masterData",
            label: "Master Data",
            fields: [
                 {
                name: "berichts-pflicht",
                label: "Berichts-Pflicht",
                description: "Ist das Unternehmen berichtspflichtig?",
                    unit: "",
                uploadComponentName: "YesNoFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "(gültigkeits)Datum",
                label: "(Gültigkeits) Datum",
                description: "Datum bis wann die Information gültig ist",
                    unit: "",
                uploadComponentName: "DateFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "masterData",
            label: "Master Data",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "berichts-pflicht",
                label: "Berichts-Pflicht",
                fields: [
    
                ],
                },
            {
                name: "(gültigkeits)Datum",
                label: "(Gültigkeits) Datum",
                fields: [
    
                ],
                },
            ],
            },
        ],
        },
    {
        name: "allgemein",
        label: "Allgemein",
        color: " ", 
        showIf: ():boolean => true,
        subcategories: [    {
            name: "esg-ziele",
            label: "ESG-Ziele",
            fields: [
    
            ],
            },
        {
            name: "ziele",
            label: "Ziele",
            fields: [
    
            ],
            },
        {
            name: "investitionen",
            label: "Investitionen",
            fields: [
    
            ],
            },
        {
            name: "sektorMitHohenKlimaauswirkungen",
            label: "Sektor mit hohen Klimaauswirkungen",
            fields: [
    
            ],
            },
        {
            name: "sektor",
            label: "Sektor",
            fields: [
    
            ],
            },
        {
            name: "nachhaltigkeitsbericht",
            label: "Nachhaltigkeitsbericht",
            fields: [
    
            ],
            },
        {
            name: "frequenzDerBerichterstattung",
            label: "Frequenz der Berichterstattung",
            fields: [
    
            ],
            },
        {
            name: "mechanismenZurÜberwachungDerEinhaltungUNGlobalCompactPrinzipienUnd/oderOECDLeitsätze",
            label: "Mechanismen zur Überwachung der Einhaltung UN Global Compact Prinzipien und/oder OECD Leitsätze",
            fields: [
    
            ],
            },
        {
            name: "uncgPrinzipien",
            label: "UNCG Prinzipien",
            fields: [
    
            ],
            },
        {
            name: "erklärungUNGC",
            label: "Erklärung UNGC",
            fields: [
    
            ],
            },
        {
            name: "oecdLeitsätze",
            label: "OECD Leitsätze",
            fields: [
    
            ],
            },
        {
            name: "erklärungOECD",
            label: "Erklärung OECD",
            fields: [
    
            ],
            },
        {
            name: "ausrichtungAufDieUNSDGsUndAktivesVerfolgen",
            label: "Ausrichtung auf die UN SDGs und aktives Verfolgen",
            fields: [
    
            ],
            },
        {
            name: "ausschlusslistenAufBasisVonESGKriterien",
            label: "Ausschlusslisten auf Basis von ESG Kriterien",
            fields: [
    
            ],
            },
        {
            name: "ausschlusslisten",
            label: "Ausschlusslisten",
            fields: [
    
            ],
            },
        {
            name: "ökologische/sozialeFührungsstandardsOder-prinzipien",
            label: "Ökologische/soziale Führungsstandards oder -prinzipien",
            fields: [
    
            ],
            },
        {
            name: "anreizmechanismenFürDasManagement(Umwelt)",
            label: "Anreizmechanismen für das Management (Umwelt)",
            fields: [
    
            ],
            },
        {
            name: "anreizmechanismenFürDasManagement(Soziales)",
            label: "Anreizmechanismen für das Management (Soziales)",
            fields: [
    
            ],
            },
        {
            name: "esg-bezogeneRechtsstreitigkeiten",
            label: "ESG-bezogene Rechtsstreitigkeiten",
            fields: [
    
            ],
            },
        {
            name: "rechtsstreitigkeitenMitBezugZuE",
            label: "Rechtsstreitigkeiten mit Bezug zu E",
            fields: [
    
            ],
            },
        {
            name: "statusZuE",
            label: "Status zu E",
            fields: [
    
            ],
            },
        {
            name: "einzelheitenZuDenRechtsstreitigkeitenZuE",
            label: "Einzelheiten zu den Rechtsstreitigkeiten zu E",
            fields: [
    
            ],
            },
        {
            name: "rechtsstreitigkeitenMitBezugZuS",
            label: "Rechtsstreitigkeiten mit Bezug zu S",
            fields: [
    
            ],
            },
        {
            name: "statusZuS",
            label: "Status zu S",
            fields: [
    
            ],
            },
        {
            name: "einzelheitenZuDenRechtsstreitigkeitenZuS",
            label: "Einzelheiten zu den Rechtsstreitigkeiten zu S",
            fields: [
    
            ],
            },
        {
            name: "rechtsstreitigkeitenMitBezugZuG",
            label: "Rechtsstreitigkeiten mit Bezug zu G",
            fields: [
    
            ],
            },
        {
            name: "statusZuG",
            label: "Status zu G",
            fields: [
    
            ],
            },
        {
            name: "einzelheitenZuDenRechtsstreitigkeitenZuG",
            label: "Einzelheiten zu den Rechtsstreitigkeiten zu G",
            fields: [
    
            ],
            },
        {
            name: "esg-rating",
            label: "ESG-Rating",
            fields: [
    
            ],
            },
        {
            name: "agentur",
            label: "Agentur",
            fields: [
    
            ],
            },
        {
            name: "ergebnis",
            label: "Ergebnis",
            fields: [
    
            ],
            },
        {
            name: "kritischePunkte",
            label: "Kritische Punkte",
            fields: [
    
            ],
            },
        {
            name: "nachhaltigkeitsbezogenenAnleihen",
            label: "Nachhaltigkeitsbezogenen Anleihen",
            fields: [
    
            ],
            },
        {
            name: "wichtigsteE-,S-UndG-RisikenUndBewertung",
            label: "Wichtigste E-, S- und G-Risiken und Bewertung",
            fields: [
    
            ],
            },
        {
            name: "hindernisseBeimUmgangMitESG-Bedenken",
            label: "Hindernisse beim Umgang mit ESG-Bedenken",
            fields: [
    
            ],
            },
        ],
        },
    {
        name: "umwelt",
        label: "Umwelt",
        color: " ", 
        showIf: ():boolean => true,
        subcategories: [    {
            name: "treibhausgasemissionen",
            label: "Treibhausgasemissionen",
            fields: [
                 {
                name: "treibhausgas-emissionsintensitätDerUnternehmen,InDieInvestriertWird",
                label: "Treibhausgas-Emissionsintensität der Unternehmen, in die investriert wird",
                description: "THG-Emissionsintensität der Unternehmen, in die investiert wird. Scope 1 + Scope 2 Treibhausgasemissionen ./. Umsatz in Millionen EUR Scope 1 + Scope 2 Treibhausgasemissionen ./. Unternehmensgröße in Mio. EUR",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "strategieUndZieleZurReduzierungVonTreibhausgas-Emissionen",
                label: "Strategie und Ziele zur Reduzierung von Treibhausgas-Emissionen",
                description: "Welchen Entwicklungspfad bzgl. der (Reduktion von) Treibhausgasemissionen verfolgt das Unternehmen. Gibt es einen Zeitplan bzw. konkrete Ziele? Und wie plant das Unternehmen, diesen Kurs zu erreichen? Bitte erläutern Sie, in welchem Bezug dieser Entwicklungspfad zu dem auf dem Pariser Abkommen basierenden Kurs steht.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "treibhausgasemissionen",
            label: "Treibhausgasemissionen",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "treibhausgas-emissionsintensitätDerUnternehmen,InDieInvestriertWird",
                label: "Treibhausgas-Emissionsintensität der Unternehmen, in die investriert wird",
                fields: [
    
                ],
                },
            {
                name: "strategieUndZieleZurReduzierungVonTreibhausgas-Emissionen",
                label: "Strategie und Ziele zur Reduzierung von Treibhausgas-Emissionen",
                fields: [
    
                ],
                },
            ],
            },
        {
            name: "produkteZurVerringerungDerUmweltbelastung",
            label: "Produkte zur Verringerung der Umweltbelastung",
            fields: [
    
            ],
            },
        {
            name: "verringerungenDerUmweltbelastung",
            label: "Verringerungen der Umweltbelastung",
            fields: [
    
            ],
            },
        {
            name: "ökologischerMindest-StandardFürProduktionsprozesse",
            label: "Ökologischer Mindest-Standard für Produktionsprozesse",
            fields: [
    
            ],
            },
        {
            name: "energieverbrauch",
            label: "Energieverbrauch",
            fields: [
                 {
                name: "unternehmens/gruppenStrategieBzglEnergieverbrauch",
                label: "Unternehmens/Gruppen Strategie bzgl Energieverbrauch",
                description: "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "energieverbrauch",
            label: "Energieverbrauch",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "unternehmens/gruppenStrategieBzglEnergieverbrauch",
                label: "Unternehmens/Gruppen Strategie bzgl Energieverbrauch",
                fields: [
    
                ],
                },
            ],
            },
        {
            name: "energieeffizienzImmobilienanlagen",
            label: "Energieeffizienz Immobilienanlagen",
            fields: [
                 {
                name: "unternehmens/gruppenStrategieBzglEnergieeffizientenImmobilienanlagen",
                label: "Unternehmens/Gruppen Strategie bzgl energieeffizienten Immobilienanlagen",
                description: "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "energieeffizienzImmobilienanlagen",
            label: "Energieeffizienz Immobilienanlagen",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "unternehmens/gruppenStrategieBzglEnergieeffizientenImmobilienanlagen",
                label: "Unternehmens/Gruppen Strategie bzgl energieeffizienten Immobilienanlagen",
                fields: [
    
                ],
                },
            ],
            },
        {
            name: "wasserverbrauch",
            label: "Wasserverbrauch",
            fields: [
                 {
                name: "unternehmens/gruppenStrategieBzglWasserverbrauch",
                label: "Unternehmens/Gruppen Strategie bzgl Wasserverbrauch",
                description: "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "wasserverbrauch",
            label: "Wasserverbrauch",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "unternehmens/gruppenStrategieBzglWasserverbrauch",
                label: "Unternehmens/Gruppen Strategie bzgl Wasserverbrauch",
                fields: [
    
                ],
                },
            ],
            },
        {
            name: "abfallproduktion",
            label: "Abfallproduktion",
            fields: [
                 {
                name: "unternehmens/gruppenStrategieBzglAbfallproduktion",
                label: "Unternehmens/Gruppen Strategie bzgl Abfallproduktion",
                description: "Bitte erläutern Sie den von der Gruppe/Unternehmen definierte Entwicklungspfad (Zeitplan und Ziel - falls vorhanden) und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "abfallproduktion",
            label: "Abfallproduktion",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "unternehmens/gruppenStrategieBzglAbfallproduktion",
                label: "Unternehmens/Gruppen Strategie bzgl Abfallproduktion",
                fields: [
    
                ],
                },
            ],
            },
        {
            name: "gefährlicheAbfälle",
            label: "Gefährliche Abfälle",
            fields: [
                 {
                name: "gefährlicherAbfall",
                label: "Gefährlicher Abfall",
                description: "Wie wird in dem Unternehmen während der Produktion und Verarbeitung mit gefährlichen Abfällen (brennbar, reaktiv, giftig, radioaktiv) umgegangen?",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "gefährlicheAbfälle",
            label: "Gefährliche Abfälle",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "gefährlicherAbfall",
                label: "Gefährlicher Abfall",
                fields: [
    
                ],
                },
            ],
            },
        {
            name: "biodiversität",
            label: "Biodiversität",
            fields: [
                 {
                name: "negativeAktivitätenFürDieBiologischeVielfalt",
                label: "Negative Aktivitäten für die biologische Vielfalt",
                description: "Hat das Unternehmen Standorte / Betriebe in oder in der Nähe von biodiversitätssensiblen Gebieten, in denen sich die Aktivitäten des Unternehmens negativ auf diese Gebiete auswirken?",
                    unit: "",
                uploadComponentName: "YesNoFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "negativeMaßnahmenFürDieBiologischeVielfalt",
                label: "Negative Maßnahmen für die biologische Vielfalt",
                description: "Bitte erläutern Sie Aktivitäten, die sich negativ auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für den Umgang mit diesen Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "positiveAktivitätenFürDieBiologischeVielfalt",
                label: "Positive Aktivitäten für die biologische Vielfalt",
                description: "Hat das Unternehmen Standorte / Betriebe in oder in der Nähe von biodiversitätssensiblen Gebieten, in denen sich die Aktivitäten des Unternehmens positiv auf diese Gebiete auswirken?",
                    unit: "",
                uploadComponentName: "YesNoFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "positiveMaßnahmenFürDieBiologischeVielfalt",
                label: "Positive Maßnahmen für die biologische Vielfalt",
                description: "Bitte erläutern Sie Aktivitäten, die sich positiv auf die Biodiversität auswirken. Teilen Sie bitte auch den von der Gruppe/Unternehmen definierte Entwicklungspfad für die Weiterentwicklung dieser Maßnahmen (Zeitplan und Ziel - falls vorhanden) mit uns und wie das Unternehmen den geplanten Entwicklungspfad erreichen möchte.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "biodiversität",
            label: "Biodiversität",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "negativeAktivitätenFürDieBiologischeVielfalt",
                label: "Negative Aktivitäten für die biologische Vielfalt",
                fields: [
    
                ],
                },
            {
                name: "negativeMaßnahmenFürDieBiologischeVielfalt",
                label: "Negative Maßnahmen für die biologische Vielfalt",
                fields: [
    
                ],
                },
            {
                name: "positiveAktivitätenFürDieBiologischeVielfalt",
                label: "Positive Aktivitäten für die biologische Vielfalt",
                fields: [
    
                ],
                },
            {
                name: "positiveMaßnahmenFürDieBiologischeVielfalt",
                label: "Positive Maßnahmen für die biologische Vielfalt",
                fields: [
    
                ],
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
                description: "Erzielt das Unternehmen einen Teil seiner Einnahmen aus Aktivitäten im Bereich fossiler Brennstoffe und/oder besitzt das Unternehmen Immobilien, die an der Gewinnung, Lagerung, dem Transport oder der Herstellung fossiler Brennstoffe beteiligt sind?",
                    unit: "",
                uploadComponentName: "YesNoFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "fossileBrennstoffe",
            label: "Fossile Brennstoffe",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "einnahmenAusFossilenBrennstoffen",
                label: "Einnahmen aus fossilen Brennstoffen",
                fields: [
    
                ],
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
                            identifier: "Nfrd",
                            label: "Nfrd",
                        },
                        {
                            identifier: "Csrd",
                            label: "Csrd",
                        },
                    ],
                unit: "",
                uploadComponentName: "SingleSelectFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "euTaxonomieKompassAktivitäten",
                label: "EU Taxonomie Kompass Aktivitäten",
                description: "Welche Aktivitäten gem. dem EU Taxonomie-Kompass übt das Unternehmen aus?",
                options: [
                        {
                            identifier: "Eutaxonomyactivityoptions",
                            label: "Eutaxonomyactivityoptions",
                        },
                    ],
                unit: "",
                uploadComponentName: "MultiSelectFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "taxonomie",
            label: "Taxonomie",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "taxonomieBerichterstattung",
                label: "Taxonomie Berichterstattung",
                fields: [
    
                ],
                },
            {
                name: "euTaxonomieKompassAktivitäten",
                label: "EU Taxonomie Kompass Aktivitäten",
                fields: [
    
                ],
                },
            ],
            },
        ],
        },
    {
        name: "soziales",
        label: "Soziales",
        color: " ", 
        showIf: ():boolean => true,
        subcategories: [    {
            name: "änderungenUnternehmensstruktur",
            label: "Änderungen Unternehmensstruktur",
            fields: [
    
            ],
            },
        {
            name: "sicherheitsmaßnahmenFürMitarbeiter",
            label: "Sicherheitsmaßnahmen für Mitarbeiter",
            fields: [
    
            ],
            },
        {
            name: "einkommensgleichheit",
            label: "Einkommensgleichheit",
            fields: [
                 {
                name: "maßnahmenZurVerbesserungDerEinkommensungleichheit",
                label: "Maßnahmen zur Verbesserung der Einkommensungleichheit",
                description: "Wie überwacht das Unternehmen die Einkommens(un)gleichheit und welche Maßnahmen wurden ergriffen, um die Einkommensungleichheit abzustellen?",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "einkommensgleichheit",
            label: "Einkommensgleichheit",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "maßnahmenZurVerbesserungDerEinkommensungleichheit",
                label: "Maßnahmen zur Verbesserung der Einkommensungleichheit",
                fields: [
    
                ],
                },
            ],
            },
        {
            name: "geschlechterdiversität",
            label: "Geschlechterdiversität",
            fields: [
                 {
                name: "definitionTop-Management",
                label: "Definition Top-Management",
                description: "Bitte geben Sie Ihre Definition von \"Top-Management\".",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "einhaltungRechtlicherVorgaben",
                label: "Einhaltung rechtlicher Vorgaben",
                description: "Welche Maßnahmen wurden ergriffen, um das geltende Recht in Bezug auf die Geschlechterdiversität von Exekutivinstanzen einzuhalten?",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "geschlechterdiversität",
            label: "Geschlechterdiversität",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "definitionTop-Management",
                label: "Definition Top-Management",
                fields: [
    
                ],
                },
            {
                name: "einhaltungRechtlicherVorgaben",
                label: "Einhaltung rechtlicher Vorgaben",
                fields: [
    
                ],
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
                description: "Führt das Unternehmen interne oder externe Audits durch, um die Einhaltung der Arbeitsnormen durch das Unternehmen zu bewerten?",
                    unit: "",
                uploadComponentName: "YesNoFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "artDesAudits",
                label: "Art des Audits",
    
                options: [
                        {
                            identifier: "InterneAnhoerung",
                            label: "InterneAnhoerung",
                        },
                        {
                            identifier: "PruefungDurchDritte",
                            label: "PruefungDurchDritte",
                        },
                        {
                            identifier: "SowohlInternAlsAuchVonDrittanbietern",
                            label: "SowohlInternAlsAuchVonDrittanbietern",
                        },
                    ],
                unit: "",
                uploadComponentName: "SingleSelectFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
            {
                name: "auditErgebnisse",
                label: "Audit Ergebnisse",
                description: "Bitte geben Sie Informationen über das letzte Audit an.",
                    unit: "",
                uploadComponentName: "InputTextFormField",
                required: false,
                showIf: ():boolean => true, 
                validation: "",
                },
     
            ],
            },
        {
            name: "audit",
            label: "Audit",
            color: " ", 
            showIf: ():boolean => true,
            subcategories: [    {
                name: "auditsZurEinhaltungVonArbeitsstandards",
                label: "Audits zur Einhaltung von Arbeitsstandards",
                fields: [
    
                ],
                },
            {
                name: "artDesAudits",
                label: "Art des Audits",
                fields: [
    
                ],
                },
            {
                name: "auditErgebnisse",
                label: "Audit Ergebnisse",
                fields: [
    
                ],
                },
            ],
            },
        ],
        },
    {
        name: "unternehmensführung/Governance",
        label: "Unternehmensführung/ Governance",
        color: " ", 
        showIf: ():boolean => true,
        subcategories: [    {
            name: "wirtschaftsprüfer",
            label: "Wirtschaftsprüfer",
            fields: [
    
            ],
            },
        {
            name: "ceo/vorsitzender",
            label: "CEO/Vorsitzender",
            fields: [
    
            ],
            },
        {
            name: "amtszeit",
            label: "Amtszeit",
            fields: [
    
            ],
            },
        {
            name: "einbeziehungVonStakeholdern",
            label: "Einbeziehung von Stakeholdern",
            fields: [
    
            ],
            },
        {
            name: "prozessDerEinbeziehungVonStakeholdern",
            label: "Prozess der Einbeziehung von Stakeholdern",
            fields: [
    
            ],
            },
        {
            name: "mechanismenZurAusrichtungAufStakeholder",
            label: "Mechanismen zur Ausrichtung auf Stakeholder",
            fields: [
    
            ],
            },
        {
            name: "esg-kriterienUndÜberwachungDerLieferanten",
            label: "ESG-Kriterien und Überwachung der Lieferanten",
            fields: [
    
            ],
            },
        {
            name: "auswahlkriterien",
            label: "Auswahlkriterien",
            fields: [
    
            ],
            },
        ],
        },
    ];

