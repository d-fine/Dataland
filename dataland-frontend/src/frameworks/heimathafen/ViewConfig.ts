import { type HeimathafenData } from "@clients/backend";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { type AvailableMLDTDisplayObjectTypes } from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import { formatStringForDatatable } from "@/components/resources/dataTable/conversion/PlainStringValueGetterFactory";
import { formatYesNoValueForDatatable } from "@/components/resources/dataTable/conversion/YesNoValueGetterFactory";
import { formatNumberForDatatable } from "@/components/resources/dataTable/conversion/NumberValueGetterFactory";
import { wrapDisplayValueWithDatapointInformation } from "@/components/resources/dataTable/conversion/DataPoints";
export const HeimathafenViewConfiguration: MLDTConfig<HeimathafenData> = [
  {
    type: "section",
    label: "General",
    expandOnPageLoad: true,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "section",
        label: "Datenanbieter",
        expandOnPageLoad: true,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Unternehmenseigentum und Eigentümerstruktur",
            explanation:
              "Bitte geben Sie eine kurze Auskunft über die Besitzverhältnisse und Eigentümerstruktur des Unternehmens. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.datenanbieter?.unternehmenseigentumUndEigentuemerstruktur),
          },
          {
            type: "cell",
            label: "Kernkompetenzen und Geschäftsbereiche",
            explanation: "Bitte beschreiben Sie kurz Ihre Kernkompetenzen und Geschäftsfelder",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.datenanbieter?.kernkompetenzenUndGeschaeftsbereiche),
          },
          {
            type: "cell",
            label: "Anzahl der für ESG zuständigen Mitarbeiter",
            explanation:
              "Wie viele Mitarbeiter in Ihrem Unternehmen sind für den ESG-Bereich in Ihrem Unternehmen verantwortlich",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatNumberForDatatable(dataset.general?.datenanbieter?.anzahlDerFuerEsgZustaendigenMitarbeiter, ""),
          },
        ],
      },
      {
        type: "section",
        label: "Methodik",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Verständnis von Nachhaltigkeit als Teil der Bewertung",
            explanation:
              "Bitte führen Sie Ihr Verständnis von Nachhaltigkeit im Rahmen der Bewertung aus. \nBitte machen Sie Angaben zu den Komponenten, die Sie bei der Bewertung des Grades der Nachhaltigkeit von Unternehmen berücksichtigen. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.verstaendnisVonNachhaltigkeitAlsTeilDerBewertung),
          },
          {
            type: "cell",
            label: "Kriterien für Ihre Nachhaltigkeitsratings",
            explanation: "Welche Kriterien legen Sie für Ihre Nachhaltigkeitsratings zugrunde?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.kriterienFuerIhreNachhaltigkeitsratings),
          },
          {
            type: "cell",
            label: "Verfahren zur Vorbereitung der Analyse oder Methodik",
            explanation:
              "Bitte beschreiben Sie uns das Vorgehen zur Erstellung Ihrer Analysen, bzw. die zugrunde liegende Methodik.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.verfahrenZurVorbereitungDerAnalyseOderMethodik),
          },
          {
            type: "cell",
            label: "Wie ist Ihre Bewertungsskala definiert?",
            explanation: "Wie ist Ihre Ratingskala definiert?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.wieIstIhreBewertungsskalaDefiniert),
          },
          {
            type: "cell",
            label: "Bewertung aktuell",
            explanation:
              "Wie stellen Sie die Aktualität ihrer Ratings sicher? Wie häufig/in welchen Zeitabständen werden Updates zur Verfügung gestellt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.bewertungAktuell),
          },
          {
            type: "cell",
            label: "Sind Ihre Bewertungen unabhängig?",
            explanation:
              "Erfolgen Ihre Ratings unabhängig (von Kunden, Kooperationspartnern, Unternehmen, etc.)\nWelche Parteien können aktiv Einfluss auf die Gestaltung des Ratings nehmen?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.sindIhreBewertungenUnabhaengig),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nHier sollten Angaben zur Vorgehensweise bei der Datenerhebung gemacht werden, z.B. mithilfe eines Fragebogens, Interviews, etc. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.datenerfassung),
          },
          {
            type: "cell",
            label: "Die Methodik umfasst Umwelt, Soziales und Governance",
            explanation:
              "Deckt die Methodik die Bereiche Umwelt, Soziales und Governance ab?\nHier sollte darauf eingangen werden, ob die Methodik alle drei Bereiche abdeckt oder ein Fokus auf bestimmte Themenbereiche vorliegt.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.methodik?.dieMethodikUmfasstUmweltSozialesUndGovernance),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden für die Datenerhebung verwendet?\nAngabe von Quellen für die Datenerhebung, zum Beispiel Nachhaltigkeitsberichte von Unternehmen, Daten von NGOs etc. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.datenquelle),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie werden die erhobenen Daten plausibilisiert?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten (z.B. numerische Daten werden verlangt und Text wurde eingetragen)",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.datenPlausibilitaetspruefung),
          },
          {
            type: "cell",
            label: "Intervalle für die Datenaktualisierung",
            explanation:
              "In welchen Zeiträumen erfolgt eine Aktualisierung der Daten?\nAngabe des Zeitraums in dem das Rating aktualisiert wird (z.B. quartalsweise, monatlich), sowie wie z.B. mit Adhoc Meldungen umgegangen wird.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.intervalleFuerDieDatenaktualisierung),
          },
          {
            type: "cell",
            label: "Zuverlässigkeit der Methodik sicherstellen",
            explanation:
              "Wie wird die Reliabilität der Methodik sichergestellt?\nBei einer Methodik muss sichergestellt werden, dass mehrere Anwender zum selben Ergebnis kommen. Angaben dazu, wie das gewährleistet wird. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.zuverlaessigkeitDerMethodikSicherstellen),
          },
          {
            type: "cell",
            label: "Minimieren oder verhindern Sie subjektive Faktoren",
            explanation:
              "Wie werden subjektive Einflussfaktoren minimiert bzw. verhindert?\nSubjektive Einschätzungen spielen im Rating Markt eine große Rolle, Angaben dazu, wie Subjektivität reduziert wird. (z.B. durch Vier-Augen Prinzip, automatische Prozesse)",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.minimierenOderVerhindernSieSubjektiveFaktoren),
          },
          {
            type: "cell",
            label: "Liste potenzieller Interessenkonflikte",
            explanation:
              "Bitte führen Sie mögliche Interessenskonflikte auf. \nKurze Beschreibung möglicher entstehender Interessenskonflikte bei der Bewertung eines Unternehmens.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.listePotenziellerInteressenkonflikte),
          },
          {
            type: "cell",
            label: "Interessenkonflikten entgegenwirken",
            explanation:
              "Wie wird Interessenskonflikten entgegengewirkt?\nWenn der Erheber der Daten zugleich der Nutzer der Daten ist, kann es zu Interessenskonflikten kommen. Beschreibung der Prozesse, um dem entgegenzuwirken (z.B. Maßnahmen zur Erhöhung der Transparenz, Erfüllung bestimmter Vorgaben, Vier-Augen Prinzip)",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.interessenkonfliktenEntgegenwirken),
          },
          {
            type: "cell",
            label: "Dokumentation der Datenerfassung und Sicherstellung des Prozesses",
            explanation:
              "Wie wird die Dokumentation der erhobenen Daten und der Prozesse sichergestellt?\nAngabe des Dokumentationsortes von Daten und Prozessen und Ausführung der Art und Weise der Dokumentation. Angabe von Maßnahmen zur Unveränderlichkeit von Informationen.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.methodik?.dokumentationDerDatenerfassungUndSicherstellungDesProzesses,
              ),
          },
          {
            type: "cell",
            label: "Bewertung von Qualitätsstandards",
            explanation: "Welche Qualitätsstandards liegen Ihrem Rating zugrunde?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.bewertungVonQualitaetsstandards),
          },
          {
            type: "cell",
            label: "Rating-Transparenzstandards",
            explanation: "Welche Transparenzstandards liegen Ihrem Rating zugrunde?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.ratingTransparenzstandards),
          },
          {
            type: "cell",
            label: "Qualitätssicherungsprozess",
            explanation: "Gibt es einen Qualitätssicherungsprozess?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.methodik?.qualitaetssicherungsprozess),
          },
          {
            type: "cell",
            label: "Falls nein, geben Sie bitte die Gründe an",
            explanation: "Wenn Nein, bitte begründen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.qualitaetssicherungsprozess == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.fallsNeinGebenSieBitteDieGruendeAn),
          },
          {
            type: "cell",
            label: "Struktur des Qualitätssicherungsprozesses",
            explanation:
              "Wie ist der Qualitätssicherungsprozess aufgebaut?\nBeschreibung des Prozesses, wie z.B. sichergestellt wird, dass keine falschen Daten oder  unvollständige Daten erhoben werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.qualitaetssicherungsprozess == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.strukturDesQualitaetssicherungsprozesses),
          },
          {
            type: "cell",
            label: " Die Aktualität der Methodik",
            explanation:
              "Wie wird die Aktualität der Methodik sichergestellt?\nAngaben dazu, wie Adhoc/kurzfristige Meldungen bei Emittenten und kurzfristige regulatorische Veränderungen überwacht und in die Methodik integriert werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.qualitaetssicherungsprozess == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.dieAktualitaetDerMethodik),
          },
          {
            type: "cell",
            label: "PAIs in die Analyse einbezogen",
            explanation: "Werden PAIs in der Analyse berücksichtigt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.methodik?.paisInDieAnalyseEinbezogen),
          },
          {
            type: "cell",
            label: "Liste der eingeschlossenen PAIs",
            explanation: "Welche PAIs werden in der Analyse berücksichtigt?",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.paisInDieAnalyseEinbezogen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.listeDerEingeschlossenenPais),
          },
          {
            type: "cell",
            label: "Quelle der PAI-Sammlung",
            explanation: "Welche Quellen werden für die Erhebung der PAIs verwendet?",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.paisInDieAnalyseEinbezogen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.quelleDerPaiSammlung),
          },
          {
            type: "cell",
            label: "Umgang mit Ausreißern",
            explanation: "Wie erfolgt der Umgang mit Ausreißern?",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.paisInDieAnalyseEinbezogen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.umgangMitAusreissern),
          },
          {
            type: "cell",
            label: "Identifizierung von kontroversen Geschäften",
            explanation: "Wie werden kontroverse Geschäftsfelder identifiziert?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.identifizierungVonKontroversenGeschaeften),
          },
          {
            type: "cell",
            label: "Aktuelle Kontroversen",
            explanation: "Wie wird die Aktualität der Kontroversen gewährleistet?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.aktuelleKontroversen),
          },
          {
            type: "cell",
            label: "Kontroversen um die Quellenerfassung",
            explanation: "Welche Quellen werden zur Erfassung von Kontroversen genutzt? ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.methodik?.kontroversenUmDieQuellenerfassung),
          },
        ],
      },
      {
        type: "section",
        label: "Impactmerkmale",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "SDG - Keine Armut",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "keine Armut" gemessen werden?',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgKeineArmut),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeineArmut == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.wennSdgKeineArmutNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeineArmut == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgKeineArmut),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeineArmut == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgKeineArmut),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeineArmut == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgKeineArmut),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeineArmut == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgKeineArmut),
          },
          {
            type: "cell",
            label: "SDG - Kein Hunger",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "kein Hunger" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgKeinHunger),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeinHunger == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.wennSdgKeinHungerNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeinHunger == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgKeinHunger),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeinHunger == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgKeinHunger),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeinHunger == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgKeinHunger),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgKeinHunger == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgKeinHunger),
          },
          {
            type: "cell",
            label: "SDG - Gesundheit und Wohlergehen",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Gesundheit und Wohlergehen" gemessen werden?    ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgGesundheitUndWohlergehen),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGesundheitUndWohlergehen == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgGesundheitUndWohlergehenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGesundheitUndWohlergehen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgGesundheitUndWohlergehen,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten).",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGesundheitUndWohlergehen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgGesundheitUndWohlergehen),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGesundheitUndWohlergehen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgGesundheitUndWohlergehen,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Daten von NGOs etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGesundheitUndWohlergehen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgGesundheitUndWohlergehen),
          },
          {
            type: "cell",
            label: "SDG - Hochwertige Bildung",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Hochwertige Bildung" gemessen werden?',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgHochwertigeBildung),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgHochwertigeBildung == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.wennSdgHochwertigeBildungNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgHochwertigeBildung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgHochwertigeBildung,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgHochwertigeBildung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgHochwertigeBildung),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgHochwertigeBildung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgHochwertigeBildung,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgHochwertigeBildung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgHochwertigeBildung),
          },
          {
            type: "cell",
            label: "SDG - Geschlechtergleichheit",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Geschlechtergleichheit" gemessen werden?',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgGeschlechtergleichheit),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGeschlechtergleichheit == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgGeschlechtergleichheitNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGeschlechtergleichheit == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgGeschlechtergleichheit,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGeschlechtergleichheit == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgGeschlechtergleichheit),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGeschlechtergleichheit == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgGeschlechtergleichheit,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgGeschlechtergleichheit == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgGeschlechtergleichheit),
          },
          {
            type: "cell",
            label: "SDG - Sauberes Wasser und sanitäre Einrichtungen",
            explanation:
              'Kann mit der Methodik ein Beitrag zum SDG "Sauberes Wasser und Sanitäreinrichtungen" gemessen werden?',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgSauberesWasserUndSanitaereEinrichtungen),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgSauberesWasserUndSanitaereEinrichtungen == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgSauberesWasserUndSanitaereEinrichtungenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale
                  ?.verwendeteSchluesselzahlenFuerSdgSauberesWasserUndSanitaereEinrichtungen,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgSauberesWasserUndSanitaereEinrichtungen,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale
                  ?.datenPlausibilitaetspruefungFuerSdgSauberesWasserUndSanitaereEinrichtungen,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenquelleFuerSdgSauberesWasserUndSanitaereEinrichtungen,
              ),
          },
          {
            type: "cell",
            label: "SDG - Bezahlbare und saubere Energie",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Bezahlbare und saubere Energie" gemessen werden?',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgBezahlbareUndSaubereEnergie),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgBezahlbareUndSaubereEnergie == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgBezahlbareUndSaubereEnergieNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgBezahlbareUndSaubereEnergie == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgBezahlbareUndSaubereEnergie,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgBezahlbareUndSaubereEnergie == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgBezahlbareUndSaubereEnergie,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgBezahlbareUndSaubereEnergie == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgBezahlbareUndSaubereEnergie,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgBezahlbareUndSaubereEnergie == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgBezahlbareUndSaubereEnergie),
          },
          {
            type: "cell",
            label: "SDG - Menschenwürdige Arbeit und Wirtschaftswachstum",
            explanation:
              'Kann mit der Methodik ein Beitrag zum SDG "Menschenwürdige Arbeit und Wirtschaftswachstum" gemessen werden? ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.general?.impactmerkmale?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgMenschenwuerdigeArbeitUndWirtschaftswachstumNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale
                  ?.verwendeteSchluesselzahlenFuerSdgMenschenwuerdigeArbeitUndWirtschaftswachstum,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgMenschenwuerdigeArbeitUndWirtschaftswachstum,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale
                  ?.datenPlausibilitaetspruefungFuerSdgMenschenwuerdigeArbeitUndWirtschaftswachstum,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenquelleFuerSdgMenschenwuerdigeArbeitUndWirtschaftswachstum,
              ),
          },
          {
            type: "cell",
            label: "SDG - Industrie, Innovation und Infrastruktur",
            explanation:
              'Kann mit der Methodik ein Beitrag zum SDG "Industrie, Innovation und Infrastruktur" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgIndustrieInnovationUndInfrastruktur),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgIndustrieInnovationUndInfrastruktur == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgIndustrieInnovationUndInfrastrukturNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgIndustrieInnovationUndInfrastruktur,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgIndustrieInnovationUndInfrastruktur,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgIndustrieInnovationUndInfrastruktur,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenquelleFuerSdgIndustrieInnovationUndInfrastruktur,
              ),
          },
          {
            type: "cell",
            label: "SDG - Weniger Ungleichheiten",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Weniger Ungleichheiten" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgWenigerUngleichheiten),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgWenigerUngleichheiten == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgWenigerUngleichheitenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgWenigerUngleichheiten == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgWenigerUngleichheiten,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgWenigerUngleichheiten == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgWenigerUngleichheiten),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgWenigerUngleichheiten == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgWenigerUngleichheiten,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgWenigerUngleichheiten == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgWenigerUngleichheiten),
          },
          {
            type: "cell",
            label: "SDG - Nachhaltige Städte und Gemeinden",
            explanation:
              'Kann mit der Methodik ein Beitrag zum SDG "Nachhaltige Städte und Gemeinden" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgNachhaltigeStaedteUndGemeinden),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaltigeStaedteUndGemeinden == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgNachhaltigeStaedteUndGemeindenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgNachhaltigeStaedteUndGemeinden,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgNachhaltigeStaedteUndGemeinden,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgNachhaltigeStaedteUndGemeinden,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenquelleFuerSdgNachhaltigeStaedteUndGemeinden,
              ),
          },
          {
            type: "cell",
            label: "SDG - Nachhaliger Konsum und Produktion",
            explanation:
              'Kann mit der Methodik ein Beitrag zum SDG "Nachhaliger Konsum und Produktion" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgNachhaligerKonsumUndProduktion),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaligerKonsumUndProduktion == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgNachhaligerKonsumUndProduktionNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaligerKonsumUndProduktion == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgNachhaligerKonsumUndProduktion,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaligerKonsumUndProduktion == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgNachhaligerKonsumUndProduktion,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaligerKonsumUndProduktion == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgNachhaligerKonsumUndProduktion,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgNachhaligerKonsumUndProduktion == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenquelleFuerSdgNachhaligerKonsumUndProduktion,
              ),
          },
          {
            type: "cell",
            label: "SDG - Maßnahmen zum Klimaschutz",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Maßnahmen zum Klimaschutz" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgMassnahmenZumKlimaschutz),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMassnahmenZumKlimaschutz == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgMassnahmenZumKlimaschutzNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMassnahmenZumKlimaschutz == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgMassnahmenZumKlimaschutz,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMassnahmenZumKlimaschutz == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgMassnahmenZumKlimaschutz),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMassnahmenZumKlimaschutz == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgMassnahmenZumKlimaschutz,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgMassnahmenZumKlimaschutz == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgMassnahmenZumKlimaschutz),
          },
          {
            type: "cell",
            label: "SDG - Leben unter Wasser",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Leben unter Wasser" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgLebenUnterWasser),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenUnterWasser == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.wennSdgLebenUnterWasserNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenUnterWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgLebenUnterWasser,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenUnterWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgLebenUnterWasser),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenUnterWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgLebenUnterWasser,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenUnterWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgLebenUnterWasser),
          },
          {
            type: "cell",
            label: "SDG - Leben an Land",
            explanation: 'Kann mit der Methodik ein Beitrag zum SDG "Leben an Land" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgLebenAnLand),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenAnLand == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.wennSdgLebenAnLandNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenAnLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgLebenAnLand),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenAnLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenerfassungFuerSdgLebenAnLand),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenAnLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenPlausibilitaetspruefungFuerSdgLebenAnLand),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgLebenAnLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.impactmerkmale?.datenquelleFuerSdgLebenAnLand),
          },
          {
            type: "cell",
            label: "SDG - Frieden, Gerechtigkeit und starke Institutionen",
            explanation:
              'Kann mit der Methodik ein Beitrag zum SDG "Frieden, Gerechtigkeit und starke Institutionen" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.general?.impactmerkmale?.sdgFriedenGerechtigkeitUndStarkeInstitutionen,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgFriedenGerechtigkeitUndStarkeInstitutionen == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgFriedenGerechtigkeitUndStarkeInstitutionenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgFriedenGerechtigkeitUndStarkeInstitutionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale
                  ?.verwendeteSchluesselzahlenFuerSdgFriedenGerechtigkeitUndStarkeInstitutionen,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgFriedenGerechtigkeitUndStarkeInstitutionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgFriedenGerechtigkeitUndStarkeInstitutionen,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgFriedenGerechtigkeitUndStarkeInstitutionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale
                  ?.datenPlausibilitaetspruefungFuerSdgFriedenGerechtigkeitUndStarkeInstitutionen,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgFriedenGerechtigkeitUndStarkeInstitutionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenquelleFuerSdgFriedenGerechtigkeitUndStarkeInstitutionen,
              ),
          },
          {
            type: "cell",
            label: "SDG - Partnerschaften zur Erreichung der Ziele",
            explanation:
              'Kann mit der Methodik ein Beitrag zum SDG "Partnerschaften zur Erreichung der Ziele" gemessen werden?  ',
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.impactmerkmale?.sdgPartnerschaftenZurErreichungDerZiele),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgPartnerschaftenZurErreichungDerZiele == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.wennSdgPartnerschaftenZurErreichungDerZieleNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.verwendeteSchluesselzahlenFuerSdgPartnerschaftenZurErreichungDerZiele,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenerfassungFuerSdgPartnerschaftenZurErreichungDerZiele,
              ),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale
                  ?.datenPlausibilitaetspruefungFuerSdgPartnerschaftenZurErreichungDerZiele,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.general?.impactmerkmale?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.impactmerkmale?.datenquelleFuerSdgPartnerschaftenZurErreichungDerZiele,
              ),
          },
        ],
      },
      {
        type: "section",
        label: "Implementierung",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Angebotene Sprachen",
            explanation: "In welchen Sprachen wird das Produkt/Tool/System angeboten?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.angeboteneSprachen),
          },
          {
            type: "cell",
            label: "Bereitgestellte Dokumentationsarten",
            explanation: "Welche Arten von Dokumentationen stellen Sie in welcher Form zur Verfügung?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.bereitgestellteDokumentationsarten),
          },
          {
            type: "cell",
            label: "Bereitgestellte Dokumentation auf Deutsch",
            explanation:
              "Wird eine detaillierte technische deutschsprachige Dokumentation des Systems zur Verfügung gestellt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.bereitgestellteDokumentationAufDeutsch),
          },
          {
            type: "cell",
            label: "Leistungstests",
            explanation: "Wurden Performancetests durchgeführt und können Sie diese Ergebnisse zur Verfügung stellen?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.leistungstests),
          },
          {
            type: "cell",
            label: "Sicherheitstests",
            explanation: "Wurden Sicherheitstests durchgeführt und können Sie diese Ergebnisse zur Verfügung stellen?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.sicherheitstests),
          },
          {
            type: "cell",
            label: "Beschreibung der Systemarchitektur",
            explanation:
              "Geben Sie bitte eine kurze Beschreibung Ihrer Systemarchitektur (Datenbank, CPU, Prozessoren, Schnittstellen, Server etc.) bei ASP Betrieb an.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.beschreibungDerSystemarchitektur),
          },
          {
            type: "cell",
            label: "Erforderliches Client- Betriebssystem",
            explanation: "Welches Client Betriebssystem wird benötigt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.erforderlichesClientBetriebssystem),
          },
          {
            type: "cell",
            label: "Angebot für fette/dünne/zitrische Kunden",
            explanation: "Wird der Client als Fat-Client, Thin-Client, Citrix-Client angeboten?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.angebotFuerFetteDuenneZitrischeKunden),
          },
          {
            type: "cell",
            label: "Server Backup",
            explanation: "Wie wird das Backup der Server durchgeführt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.serverBackup),
          },
          {
            type: "cell",
            label: "Standardisiertes Konzept zur Wiederherstellung im Katastrophenfall",
            explanation:
              "Haben Sie ein standardisiertes Disaster Recovery Konzept? Fügen Sie hierzu bitte ein Testat oder einen Auszug seitens eines Prüfers bei.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              wrapDisplayValueWithDatapointInformation(
                formatYesNoValueForDatatable(
                  dataset.general?.implementierung?.standardisiertesKonzeptZurWiederherstellungImKatastrophenfall
                    ?.value,
                ),
                "Standardisiertes Konzept zur Wiederherstellung im Katastrophenfall",
                dataset.general?.implementierung?.standardisiertesKonzeptZurWiederherstellungImKatastrophenfall,
              ),
          },
          {
            type: "cell",
            label: "Stamm- und Bewegungsdaten lesen",
            explanation: "Können Stamm- und Bewegungsdaten über eine Datawarehouse-Lösung eingelesen werden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.stammUndBewegungsdatenLesen),
          },
          {
            type: "cell",
            label: "Kompatibilität mit anderen Datenquellen",
            explanation: "Ist Ihre Lösung mit anderen Datenquellen nutzbar?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.kompatibilitaetMitAnderenDatenquellen),
          },
          {
            type: "cell",
            label: "Import der Ergebnisse in das Data Warehouse",
            explanation: "Können die Ergebnisse  einem Datawarehouse zum Einlesen zur Verfügung gestellt werden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.importDerErgebnisseInDasDataWarehouse),
          },
          {
            type: "cell",
            label: "Erforderliches Datenbanksystem",
            explanation: "Welches Datenbanksystem wird benötigt (Hersteller, Versionsnummer)?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.erforderlichesDatenbanksystem),
          },
          {
            type: "cell",
            label: "Beschreibung des Designs und der Struktur der Datenbank(en)",
            explanation: "Bitte beschreiben Sie den Aufbau und die Struktur der Datenbank(en).",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.implementierung?.beschreibungDesDesignsUndDerStrukturDerDatenbankEn,
              ),
          },
          {
            type: "cell",
            label: "Direkter Zugriff auf die Datenbank",
            explanation: "Ist ein direkter (lesender) Zugriff auf die Datenbank Tabellen möglich?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.direkterZugriffAufDieDatenbank),
          },
          {
            type: "cell",
            label: "Schreibender Zugriff auf die Datenbank",
            explanation: "Ist ein schreibender Zugriff direkt oder über Zwischentabellen auf die Datenbank möglich?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.schreibenderZugriffAufDieDatenbank),
          },
          {
            type: "cell",
            label: "Unterstützung der Echtzeitverarbeitung",
            explanation: "Unterstützt das System Real-time Processing?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.unterstuetzungDerEchtzeitverarbeitung),
          },
          {
            type: "cell",
            label: "Unterstützung für zeitnahe Verarbeitung",
            explanation: "Unterstützt das System Near-time Processing?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.unterstuetzungFuerZeitnaheVerarbeitung),
          },
          {
            type: "cell",
            label: "Unterstützung der Stapelverarbeitung",
            explanation: "Unterstützt das System Batch Processing?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.unterstuetzungDerStapelverarbeitung),
          },
          {
            type: "cell",
            label: "Unterstützte BI-Lösung",
            explanation: "Welche BI-Lösung unterstützt Ihr System?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.unterstuetzteBiLoesung),
          },
          {
            type: "cell",
            label: "Flexibilität beim Import/Export von Daten",
            explanation:
              "Besitzt das System die Flexibilität, um über Standard-Dateiformate (xls, xlsx, csv, FundsXML, txt) Daten importieren und exportieren zu können?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.flexibilitaetBeimImportExportVonDaten),
          },
          {
            type: "cell",
            label: "24/7 Verfügbarkeit",
            explanation: "Wird eine jederzeitige Verfügbarkeit (24hrs,7d) angeboten?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.general?.implementierung?.jederzeitVerfuegbar),
          },
          {
            type: "cell",
            label: "Übertragen von Datenhistorien",
            explanation: "Welche Funktionen stellt das System zur Übernahme der Datenhistorien zur Verfügung?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.uebertragenVonDatenhistorien),
          },
          {
            type: "cell",
            label: "Unterstützter Zeitraum der Datenhistorien",
            explanation: "Welchen Zeitraum an Datenhistorien unterstützt das System?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.general?.implementierung?.unterstuetzterZeitraumDerDatenhistorien),
          },
          {
            type: "cell",
            label: "Frühester Starttermin für ein Integrationsprojekt",
            explanation: "Ab wann könnte frühestens ein Integrationsprojekt starten?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.implementierung?.fruehesterStartterminFuerEinIntegrationsprojekt,
              ),
          },
          {
            type: "cell",
            label: "Geschätzter Zeitrahmen für die vollständige Integration des Projekts",
            explanation:
              "Welchen Zeitraum schätzen Sie für ein vollständig abgeschlossenes Integrationsprojekt des beschriebenen Umfangs?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.general?.implementierung?.geschaetzterZeitrahmenFuerDieVollstaendigeIntegrationDesProjekts,
              ),
          },
          {
            type: "cell",
            label: "Durchschnittliche Anzahl der benötigten Ressourcen",
            explanation:
              "Wieviel Ressourcen werden im Durchschnitt auf Kundenseite während der Implementierung benötigt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatNumberForDatatable(
                dataset.general?.implementierung?.durchschnittlicheAnzahlDerBenoetigtenRessourcen,
                "",
              ),
          },
          {
            type: "cell",
            label: "Anzahl der verfügbaren Ressourcen",
            explanation:
              "Wieviele Ressourcen stehen ab wann und mit welcher Kapazität zur Umsetzung des Projekts zur Verfügung (Support, Entwicklung, Beratung)?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatNumberForDatatable(dataset.general?.implementierung?.anzahlDerVerfuegbarenRessourcen, ""),
          },
          {
            type: "cell",
            label: "Kundenbetreuung",
            explanation:
              "Wie und mit wievielen Mitarbeitern können Sie einen Kunden in den ersten sechs Monaten nach Einführung unterstützen?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatNumberForDatatable(dataset.general?.implementierung?.kundenbetreuung, ""),
          },
        ],
      },
    ],
  },
  {
    type: "section",
    label: "Environmental",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "section",
        label: "Nachhaltigskeitsrisiken",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Methodik für ökologische Nachhaltigkeitsrisiken",
            explanation:
              "Werden Nachhaltigkeitsrisiken bezogen auf den Bereich Umwelt in der Methodik abgebildet?\nNachhaltigkeitsrisiken können einen wesentlichen negativen Einfluss auf die Performance eines Unternehmens haben. Angaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Umwelt in der Methodik abgebildet werden. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken
                  ?.wennMethodikFuerOekologischeNachhaltigkeitsrisikenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Kartierte Risiken für die ökologische Nachhaltigkeit",
            explanation:
              "Welche Nachhaltigkeitsrisiken im Bereich Umwelt werden abgebildet?\nAufführung der Nachhaltigkeitsrisiken, die abgebildet werden können. (z.B. Klimarisiken, Risiken bzgl. Biodiversität, Risiken bzgl. Wasser)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken?.kartierteRisikenFuerDieOekologischeNachhaltigkeit,
              ),
          },
          {
            type: "cell",
            label:
              "Identifizierung der wesentlichen Risiken für die ökologische Nachhaltigkeit und der Konstruktionsmethodik",
            explanation:
              "Wie werden wesentliche Nachhaltigkeitsrisiken eines Unternehmens im Bereich Umwelt identifiziert und in der Methodik berücksichtigt?\nAngaben zur Wesentlichkeitsanalyse bei der Einstufung der Wesentlichkeit eines Risikos bezogen auf ein Unternehmen. Sowie Angaben dazu, wie sich die unterschiedliche Risikoeinstufung in der Methodik widerspiegelt. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken
                  ?.identifizierungDerWesentlichenRisikenFuerDieOekologischeNachhaltigkeitUndDerKonstruktionsmethodik,
              ),
          },
          {
            type: "cell",
            label: "Umweltbewertung unter Berücksichtigung von Nachhaltigkeitsrisiken",
            explanation:
              "Wie werden Nachhaltigkeitsrisiken in der Bewertung bezogen auf den Bereich Umwelt berücksichtigt?\nAngaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Umwelt in die Erstellung des Ratings miteinbezogen werden und wenn ja wie. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken
                  ?.umweltbewertungUnterBeruecksichtigungVonNachhaltigkeitsrisiken,
              ),
          },
          {
            type: "cell",
            label: "Risiken für die ökologische Nachhaltigkeit absichern",
            explanation: "Wie wird die Überwachung von Nachhaltigkeitsrisiken im Bereich Umwelt sichergestellt?",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken?.risikenFuerDieOekologischeNachhaltigkeitAbsichern,
              ),
          },
          {
            type: "cell",
            label: "Quelle",
            explanation:
              "Welche Quellen werden für die Erfassung von Nachhaltigkeitsrisiken im Bereich Umwelt verwendet?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Daten von NGOs etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.nachhaltigskeitsrisiken?.quelle),
          },
          {
            type: "cell",
            label: "Vier-Augen-Prüfung",
            explanation: "Ist eine Vier-Augen-Verifizierung vorhanden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.environmental?.nachhaltigskeitsrisiken?.vierAugenPruefung),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.vierAugenPruefung == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken?.wennVierAugenPruefungNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Beschreibung der Vier-Augen- Prüfung",
            explanation: "Wie erfolgt die Vier-Augen-Verifizierung?",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.vierAugenPruefung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.nachhaltigskeitsrisiken?.beschreibungDerVierAugenPruefung,
              ),
          },
        ],
      },
      {
        type: "section",
        label: "PAIs",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Sechs PAIs - Treibhausgasemissionen",
            explanation:
              "Werden die sechs PAIs bezogen auf Treibhausgasemissionen abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.pais?.wennSechsPaisTreibhausgasemissionenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Wenn Ja, bitte die PAIs auflisten",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.wennJaBitteDiePaisAuflisten),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.pais?.verwendeteSchluesselzahlenFuerSechsPaisTreibhausgasemissionen,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten, Benchmarking)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenerfassungFuerSechsPaisTreibhausgasemissionen),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde erfasst.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.pais?.datenPlausibilitaetspruefungFuerSechsPaisTreibhausgasemissionen,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärungen etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenquelleFuerSechsPaisTreibhausgasemissionen),
          },
          {
            type: "cell",
            label: "PAIs - biologische Vielfalt",
            explanation: "Wird der PAI auf Biodiversität abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.environmental?.pais?.paisBiologischeVielfalt),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.wennPaisBiologischeVielfaltNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung des PAIs verwendet werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.pais?.verwendeteSchluesselzahlenFuerPaisBiologischeVielfalt,
              ),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenerfassungFuerPaisBiologischeVielfalt),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.pais?.datenPlausibilitaetspruefungFuerPaisBiologischeVielfalt,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärungen, Interviews etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenquelleFuerPaisBiologischeVielfalt),
          },
          {
            type: "cell",
            label: "PAI - Wasser",
            explanation: "Wird der PAI auf Wasser abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.environmental?.pais?.paiWasser),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.wennPaiWasserNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.verwendeteSchluesselzahlenFuerPaiWasser),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenerfassungFuerPaiWasser),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenPlausibilitaetspruefungFuerPaiWasser),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärungen, Interviews, Daten von NGOs etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenquelleFuerPaiWasser),
          },
          {
            type: "cell",
            label: "PAI - Abfall",
            explanation: "Wird der PAI auf Abfall abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.environmental?.pais?.paiAbfall),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.wennPaiAbfallNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung des PAIs verwendet werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.verwendeteSchluesselzahlenFuerPaiAbfall),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenerfassungFuerPaiAbfall),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenPlausibilitaetspruefungFuerPaiAbfall),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärung, Daten von NGOs etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenquelleFuerPaiAbfall),
          },
          {
            type: "cell",
            label: "PAI - Umwelt auf dem Land",
            explanation: "Wir der PAI auf Umwelt bei Staaten abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.environmental?.pais?.paiUmweltAufDemLand),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paiUmweltAufDemLand == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.wennPaiUmweltAufDemLandNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung des PAIs verwendet werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.verwendeteSchluesselzahlenFuerPaiUmweltAufDemLand),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenerfassungFuerPaiUmweltAufDemLand),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.pais?.datenPlausibilitaetspruefungFuerPaiUmweltAufDemLand,
              ),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Umweltbundesamt)",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.environmental?.pais?.datenquelleFuerPaiUmweltAufDemLand),
          },
        ],
      },
      {
        type: "section",
        label: "SFDR",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Methodik zur Messung eines signifikanten Beitrags zu einem Umweltziel",
            explanation:
              "Wie erfolgt die Abbildung eines wesentlichen Beitrages zu einem Umweltziel?\nAngaben darüber ob mit der Methodik ein wesentlicher Beitrag zu einem Umweltziel gemessen werden kann und wenn ja, zu welchem und wie.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.sfdr?.methodikZurMessungEinesSignifikantenBeitragsZuEinemUmweltziel,
              ),
          },
        ],
      },
      {
        type: "section",
        label: "Kontroverse Geschäftsfelder",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Ausschluss der Tabakerzeugung",
            explanation: "Kann mit der Methodik Umsatz aus der Tabakproduktion ausgeschlossen werden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugung,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugung == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder?.wennAusschlussDerTabakerzeugungNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Metrisch verwendet",
            explanation:
              "Welche Kennzahl wird für die Messung des Umsatzes aus Tabakproduktion herangezogen?\nAngaben zu der Zusammensetzung der Kennzahl zur Berechnung des Umsatzes aus der Tabakproduktion.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder?.metrischVerwendetFuerAusschlussDerTabakerzeugung,
              ),
          },
          {
            type: "cell",
            label: "Methodik der Berechnung",
            explanation:
              "Wie erfolgt die Berechnung?\nAngaben zur Methodik zur Berechnung des Umsatzes aus der Tabakproduktion.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder
                  ?.methodikDerBerechnungFuerAusschlussDerTabakerzeugung,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Quellen",
            explanation:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder?.verwendeteQuellenFuerAusschlussDerTabakerzeugung,
              ),
          },
          {
            type: "cell",
            label: "Ausschluss der Kohleförderung und -verteilung",
            explanation:
              "Kann mit der Methodik Umsatz aus der Herstellung und dem Vertrieb von Kohle ausgeschlossen werden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder
                  ?.wennAusschlussDerKohlefoerderungUndVerteilungNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Metrisch verwendet",
            explanation:
              "Welche Kennzahl wird für die Messung des Umsatzes aus Kohle herangezogen?\nAngaben zu der Zusammensetzung der Kennzahl zur Berechnung des Umsatzes aus Kohle.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder
                  ?.metrischVerwendetFuerAusschlussDerKohlefoerderungUndVerteilung,
              ),
          },
          {
            type: "cell",
            label: "Methodik der Berechnung",
            explanation: "Wie erfolgt die Berechnung?\nAngaben zur Methodik zur Berechnung des Umsatzes aus Kohle.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder
                  ?.methodikDerBerechnungFuerAusschlussDerKohlefoerderungUndVerteilung,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Quellen",
            explanation:
              "Welche Quellen werden verwendet?Angabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.environmental?.kontroverseGeschaeftsfelder
                  ?.verwendeteQuellenFuerAusschlussDerKohlefoerderungUndVerteilung,
              ),
          },
        ],
      },
    ],
  },
  {
    type: "section",
    label: "Social",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "section",
        label: "Nachhaltigskeitsrisiken",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Methodik Soziale Nachhaltigkeitsrisiken",
            explanation:
              "Werden Nachhaltigkeitsrisiken bezogen auf den Bereich Soziales in der Methodik abgebildet?\nNachhaltigkeitsrisiken können einen wesentlichen negativen Einfluss auf die Performance eines Unternehmens haben. Angaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Soziales in der Methodik abgebildet werden. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.social?.nachhaltigskeitsrisiken?.methodikSozialeNachhaltigkeitsrisiken,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.methodikSozialeNachhaltigkeitsrisiken == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.nachhaltigskeitsrisiken?.wennMethodikSozialeNachhaltigkeitsrisikenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Kartierte soziale Nachhaltigkeitsrisiken",
            explanation:
              "Welche Nachhaltigkeitsrisiken im Bereich Soziales werden abgebildet?\nAufführung der Nachhaltigkeitsrisiken, die abgebildet werden können (z.B. Risiken in Bezug auf Arbeitnehmerbelange, Demographie, Gesundheitsschutz).",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.methodikSozialeNachhaltigkeitsrisiken == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.nachhaltigskeitsrisiken?.kartierteSozialeNachhaltigkeitsrisiken),
          },
          {
            type: "cell",
            label: "Identifizierung wesentlicher sozialer Nachhaltigkeitsrisiken und Konstruktionsmethodik",
            explanation:
              "Wie werden wesentliche Nachhaltigkeitsrisiken eines Unternehmens im Bereich Soziales identifiziert und berücksichtigt?\nAngaben zur Wesentlichkeitsanalyse bei der Einstufung der Wesentlichkeit eines Risikos bezogen auf ein Unternehmen. Sowie Angaben dazu, wie sich die unterschiedliche Risikoeinstufung in der Methodik widerspiegelt. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.methodikSozialeNachhaltigkeitsrisiken == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.nachhaltigskeitsrisiken
                  ?.identifizierungWesentlicherSozialerNachhaltigkeitsrisikenUndKonstruktionsmethodik,
              ),
          },
          {
            type: "cell",
            label: "Soziale Bewertung unter Berücksichtigung von Nachhaltigkeitsrisiken",
            explanation:
              "Wie werden Nachhaltigkeitsrisiken in der Bewertung bezogen auf den Bereich Soziales berücksichtigt?\nAngaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Soziales in die Erstellung des Ratings miteinbezogen werden und wenn ja wie. ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.nachhaltigskeitsrisiken
                  ?.sozialeBewertungUnterBeruecksichtigungVonNachhaltigkeitsrisiken,
              ),
          },
          {
            type: "cell",
            label: "Soziale Nachhaltigkeitsrisiken absichern",
            explanation: "Wie wird die Überwachung von Nachhaltigkeitsrisiken im Bereich Soziales sichergestellt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.nachhaltigskeitsrisiken?.sozialeNachhaltigkeitsrisikenAbsichern),
          },
          {
            type: "cell",
            label: "Quelle",
            explanation:
              "Welche Quellen werden für die Erfassung von Nachhaltigkeitsrisiken im Bereich Soziales verwendet?\nAngabe von Quellen, zum Beispiel Nachhaltigkeitsberichte, ethische Richtlinien, etc.)",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.nachhaltigskeitsrisiken?.quelle),
          },
          {
            type: "cell",
            label: "Vier-Augen-Prüfung",
            explanation: "Ist eine Vier-Augen-Verifizierung vorhanden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.social?.nachhaltigskeitsrisiken?.vierAugenPruefung),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.vierAugenPruefung == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.nachhaltigskeitsrisiken?.wennVierAugenPruefungNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Beschreibung der Vier-Augen- Prüfung",
            explanation: "Wie erfolgt die Vier-Augen-Verifizierung?",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.vierAugenPruefung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.nachhaltigskeitsrisiken?.beschreibungDerVierAugenPruefung),
          },
        ],
      },
      {
        type: "section",
        label: "PAIs",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "PAI - sozial",
            explanation:
              "Werden die Sozialen PAIs bei Unternehmen abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.social?.pais?.paiSozial),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.wennPaiSozialNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.verwendeteSchluesselzahlenFuerPaiSozial),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.datenerfassungFuerPaiSozial),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.datenPlausibilitaetspruefungFuerPaiSozial),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, Gender Pay Report etc. ",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.datenquelleFuerPaiSozial),
          },
          {
            type: "cell",
            label: "PAI - Soziales auf dem Land",
            explanation: "Werden die Sozialen PAIs bei Staaten abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931 ",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.social?.pais?.paiSozialesAufDemLand),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.wennPaiSozialesAufDemLandNeinBitteBegruenden),
          },
          {
            type: "cell",
            label: "Verwendete Schlüsselzahlen",
            explanation:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.verwendeteSchluesselzahlenFuerPaiSozialesAufDemLand),
          },
          {
            type: "cell",
            label: "Datenerfassung",
            explanation:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.datenerfassungFuerPaiSozialesAufDemLand),
          },
          {
            type: "cell",
            label: "Daten Plausibilitätsprüfung",
            explanation:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.datenPlausibilitaetspruefungFuerPaiSozialesAufDemLand),
          },
          {
            type: "cell",
            label: "Datenquelle",
            explanation:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Freedom House Index ",
            shouldDisplay: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.social?.pais?.datenquelleFuerPaiSozialesAufDemLand),
          },
        ],
      },
      {
        type: "section",
        label: "SFDR",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Methodik zur Messung des signifikanten Beitrags zu einem gesellschaftlichen Ziel",
            explanation:
              "Wie erfolgt die Abbildung eines wesentlichen Beitrages zu einem sozialen Ziel?\nAngaben darüber ob mit der Methodik ein wesentlicher Beitrag zu einem sozialen Ziel gemessen werden kann und wenn ja, wie.",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.sfdr?.methodikZurMessungDesSignifikantenBeitragsZuEinemGesellschaftlichenZiel,
              ),
          },
        ],
      },
      {
        type: "section",
        label: "Kontroverse Geschäftsfelder",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Herstellung oder Vertrieb von Waffen Ausschluss",
            explanation:
              "Kann mit der Methodik Umsatz aus der Herstellung oder dem Vertrieb von Waffen ausgeschlossen werden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder
                  ?.wennHerstellungOderVertriebVonWaffenAusschlussNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Metrisch verwendet",
            explanation:
              "Welche Kennzahl wird für die Messung des Umsatzes aus Waffen herangezogen?\nAngaben zu der Zusammensetzung der Kennzahl zur Berechnung des Umsatzes aus Waffen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder
                  ?.metrischVerwendetFuerHerstellungOderVertriebVonWaffenAusschluss,
              ),
          },
          {
            type: "cell",
            label: "Methodik der Berechnung",
            explanation: "Wie erfolgt die Berechnung?\nAngaben zur Methodik zur Berechnung des Umsatzes aus Waffen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder
                  ?.methodikDerBerechnungFuerHerstellungOderVertriebVonWaffenAusschluss,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Quellen",
            explanation:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder
                  ?.verwendeteQuellenFuerHerstellungOderVertriebVonWaffenAusschluss,
              ),
          },
          {
            type: "cell",
            label: "Ausschluss verbotener Waffen",
            explanation: "Können mit der Methodik geächtete Waffen ausgeschlossen werden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.social?.kontroverseGeschaeftsfelder?.ausschlussVerbotenerWaffen),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.ausschlussVerbotenerWaffen == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder?.wennAusschlussVerbotenerWaffenNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Metrisch verwendet",
            explanation:
              "Welche Kennzahl wird für geächtete Waffen herangezogen?\nAngaben zu den Bestandteilen der Kennzahl zur Abbildung geächteter Waffen. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.ausschlussVerbotenerWaffen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder?.metrischVerwendetFuerAusschlussVerbotenerWaffen,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Quellen",
            explanation:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.ausschlussVerbotenerWaffen == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.social?.kontroverseGeschaeftsfelder?.verwendeteQuellenFuerAusschlussVerbotenerWaffen,
              ),
          },
        ],
      },
    ],
  },
  {
    type: "section",
    label: "Governance",
    expandOnPageLoad: false,
    shouldDisplay: (): boolean => true,
    children: [
      {
        type: "section",
        label: "Good Governance",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Methodik der guten Regierungsführung",
            explanation: "Können Good Governance Aspekte im Rahmen der Methodik berücksichtigt werden?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.governance?.goodGovernance?.wennMethodikDerGutenRegierungsfuehrungNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Definition von guter Regierungsführung",
            explanation:
              "Wie wird Good Governance im Rahmen der Methodik definiert?\nDefinition von Good Governance im Rahmen der Methodik.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.governance?.goodGovernance?.definitionVonGuterRegierungsfuehrung),
          },
          {
            type: "cell",
            label: "Liste der KPIs für gute Unternehmensführung",
            explanation:
              "Welche KPIs werden zur Berurteilung einer Good Governance genutzt?\nAufführung der KPIs zur Beurteilung von Good Governance. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.governance?.goodGovernance?.listeDerKpisFuerGuteUnternehmensfuehrung),
          },
          {
            type: "cell",
            label: "Verwendete Quellen",
            explanation:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Nachhaltigkeitsberichte, Internetseiten von Unternehmen.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.governance?.goodGovernance?.verwendeteQuellenFuerMethodikDerGutenRegierungsfuehrung,
              ),
          },
          {
            type: "cell",
            label: "Berücksichtigung des UNGC",
            explanation: "Werden die UNGC in  der Analyse berücksichtigt?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(dataset.governance?.goodGovernance?.beruecksichtigungDesUngc),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.beruecksichtigungDesUngc == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.governance?.goodGovernance?.wennBeruecksichtigungDesUngcNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Berücksichtigung der UNGC- Beschreibung",
            explanation:
              "Wie erfolgt die Berücksichtigung der UNGC?\nWenn eine Berücksichtigung der UNGC erfolgt Angaben darüber, wie die Berücksichtigung abgebildet wird. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.beruecksichtigungDesUngc == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.governance?.goodGovernance?.beruecksichtigungDerUngcBeschreibung),
          },
          {
            type: "cell",
            label: "Verwendete Quellen",
            explanation:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel UNGC, Nachhaltigkeitsberichte ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.beruecksichtigungDesUngc == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.governance?.goodGovernance?.verwendeteQuellenFuerBeruecksichtigungDesUngc,
              ),
          },
        ],
      },
      {
        type: "section",
        label: "Kontroverse Geschäftsfelder",
        expandOnPageLoad: false,
        shouldDisplay: (): boolean => true,
        children: [
          {
            type: "cell",
            label: "Kontroversen im Bereich der Bestechung und Korruption",
            explanation: "Werden Kontroversen im Bereich Bestechung und Korruption abgebildet?",
            shouldDisplay: (): boolean => true,
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatYesNoValueForDatatable(
                dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption,
              ),
          },
          {
            type: "cell",
            label: "Wenn Nein, bitte begründen",

            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "No",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.governance?.kontroverseGeschaeftsfelder
                  ?.wennKontroversenImBereichDerBestechungUndKorruptionNeinBitteBegruenden,
              ),
          },
          {
            type: "cell",
            label: "Verwendete Metriken und Methodik",
            explanation:
              "Wie werden Kontroversen im Bereich Bestechung und Korruption abgebildet?\nAngabe von Kennzahlen und Methodiken zur Abbildung von Kontroversen im Bereich Bestechung und Korruption. ",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(dataset.governance?.kontroverseGeschaeftsfelder?.verwendeteMetrikenUndMethodik),
          },
          {
            type: "cell",
            label: "Verwendete Quellen",
            explanation:
              "Welche Datenquellen werden verwendet?\nAngabe von Quellen, zum Beispiel rennomierte Wirtschafts- und Finanzzeitungen, Glass Lewis",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.governance?.kontroverseGeschaeftsfelder
                  ?.verwendeteQuellenFuerKontroversenImBereichDerBestechungUndKorruption,
              ),
          },
          {
            type: "cell",
            label: "Die Aktualität der Kontroversen im Bereich Bestechung und Korruption",
            explanation:
              "Wie wird die Aktualität der Kontroversen im Bereich Bestechung und Korruption gewährleistet?\nAngaben dazu, wie Adhoc/kurzfristige Meldungen bei Emittenten überwacht und in die Methodik integriert werden sowie darüber in welchem Zeitraum die Kontroversen angepasst werden.",
            shouldDisplay: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "Yes",
            valueGetter: (dataset: HeimathafenData): AvailableMLDTDisplayObjectTypes =>
              formatStringForDatatable(
                dataset.governance?.kontroverseGeschaeftsfelder
                  ?.dieAktualitaetDerKontroversenImBereichBestechungUndKorruption,
              ),
          },
        ],
      },
    ],
  },
];
