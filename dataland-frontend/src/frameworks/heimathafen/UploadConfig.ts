import { type Category } from "@/utils/GenericFrameworkTypes";
import { type HeimathafenData } from "@clients/backend";

export const heimathafenDataModel = [
  {
    name: "general",
    label: "General",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "datenanbieter",
        label: "Datenanbieter",
        fields: [
          {
            name: "unternehmenseigentumUndEigentuemerstruktur",
            label: "Unternehmenseigentum und Eigentümerstruktur",
            description:
              "Bitte geben Sie eine kurze Auskunft über die Besitzverhältnisse und Eigentümerstruktur des Unternehmens.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "anzahlDerFuerEsgZustaendigenMitarbeiter",
            label: "Anzahl der für ESG zuständigen Mitarbeiter",
            description:
              "Wie viele Mitarbeiter in Ihrem Unternehmen sind für den ESG-Bereich in Ihrem Unternehmen verantwortlich",

            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "methodik",
        label: "Methodik",
        fields: [
          {
            name: "verstaendnisVonNachhaltigkeitAlsTeilDerBewertung",
            label: "Verständnis von Nachhaltigkeit als Teil der Bewertung",
            description:
              "Bitte führen Sie Ihr Verständnis von Nachhaltigkeit im Rahmen der Bewertung aus.\nBitte machen Sie Angaben zu den Komponenten, die Sie bei der Bewertung des Grades der Nachhaltigkeit von Unternehmen berücksichtigen.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "kriterienFuerIhreNachhaltigkeitsratings",
            label: "Kriterien für Ihre Nachhaltigkeitsratings",
            description: "Welche Kriterien legen Sie für Ihre Nachhaltigkeitsratings zugrunde?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "verfahrenZurVorbereitungDerAnalyseOderMethodik",
            label: "Verfahren zur Vorbereitung der Analyse oder Methodik",
            description:
              "Bitte beschreiben Sie uns das Vorgehen zur Erstellung Ihrer Analysen, bzw. die zugrunde liegende Methodik.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wieIstIhreBewertungsskalaDefiniert",
            label: "Wie ist Ihre Bewertungsskala definiert",
            description: "Wie ist Ihre Ratingskala definiert?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "bewertungAktuell",
            label: "Bewertung aktuell",
            description:
              "Wie stellen Sie die Aktualität ihrer Ratings sicher? Wie häufig/in welchen Zeitabständen werden Updates zur Verfügung gestellt?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "sindIhreBewertungenUnabhaengig",
            label: "Sind Ihre Bewertungen unabhängig",
            description:
              "Erfolgen Ihre Ratings unabhängig (von Kunden, Kooperationspartnern, Unternehmen, etc.)\nWelche Parteien können aktiv Einfluss auf die Gestaltung des Ratings nehmen?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "datenerfassung",
            label: "Datenerfassung",
            description:
              "Wie erfolgt die Datenerhebung?\nHier sollten Angaben zur Vorgehensweise bei der Datenerhebung gemacht werden, z.B. mithilfe eines Fragebogens, Interviews, etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "dieMethodikUmfasstUmweltSozialesUndGovernance",
            label: "Die Methodik umfasst Umwelt, Soziales und Governance",
            description:
              "Deckt die Methodik die Bereiche Umwelt, Soziales und Governance ab?\nHier sollte darauf eingangen werden, ob die Methodik alle drei Bereiche abdeckt oder ein Fokus auf bestimmte Themenbereiche vorliegt.",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "datenPlausibilitaetspruefung",
            label: "Daten Plausibilitätsprüfung",
            description:
              "Wie werden die erhobenen Daten plausibilisiert?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten (z.B. numerische Daten werden verlangt und Text wurde eingetragen)",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "intervalleFuerDieDatenaktualisierung",
            label: "Intervalle für die Datenaktualisierung",
            description:
              "In welchen Zeiträumen erfolgt eine Aktualisierung der Daten?\nAngabe des Zeitraums in dem das Rating aktualisiert wird (z.B. quartalsweise, monatlich), sowie wie z.B. mit Adhoc Meldungen umgegangen wird.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "zuverlaessigkeitDerMethodikSicherstellen",
            label: "Zuverlässigkeit der Methodik sicherstellen",
            description:
              "Wie wird die Reliabilität der Methodik sichergestellt?\nBei einer Methodik muss sichergestellt werden, dass mehrere Anwender zum selben Ergebnis kommen. Angaben dazu, wie das gewährleistet wird.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "minimierenOderVerhindernSieSubjektiveFaktoren",
            label: "Minimieren oder verhindern Sie subjektive Faktoren",
            description:
              "Wie werden subjektive Einflussfaktoren minimiert bzw. verhindert?\nSubjektive Einschätzungen spielen im Rating Markt eine große Rolle, Angaben dazu, wie Subjektivität reduziert wird. (z.B. durch Vier-Augen Prinzip, automatische Prozesse)",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "listePotenziellerInteressenkonflikte",
            label: "Liste potenzieller Interessenkonflikte",
            description:
              "Bitte führen Sie mögliche Interessenskonflikte auf.\nKurze Beschreibung möglicher entstehender Interessenskonflikte bei der Bewertung eines Unternehmens.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "interessenkonfliktenEntgegenwirken",
            label: "Interessenkonflikten entgegenwirken",
            description:
              "Wie wird Interessenskonflikten entgegengewirkt?\nWenn der Erheber der Daten zugleich der Nutzer der Daten ist, kann es zu Interessenskonflikten kommen. Beschreibung der Prozesse, um dem entgegenzuwirken (z.B. Maßnahmen zur Erhöhung der Transparenz, Erfüllung bestimmter Vorgaben, Vier-Augen Prinzip)",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "dokumentationDerDatenerfassungUndSicherstellungDesProzesses",
            label: "Dokumentation der Datenerfassung und Sicherstellung des Prozesses",
            description:
              "Wie wird die Dokumentation der erhobenen Daten und der Prozesse sichergestellt?\nAngabe des Dokumentationsortes von Daten und Prozessen und Ausführung der Art und Weise der Dokumentation. Angabe von Maßnahmen zur Unveränderlichkeit von Informationen.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "bewertungVonQualitaetsstandards",
            label: "Bewertung von Qualitätsstandards",
            description: "Welche Qualitätsstandards liegen Ihrem Rating zugrunde?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "ratingTransparenzstandards",
            label: "Rating-Transparenzstandards",
            description: "Welche Transparenzstandards liegen Ihrem Rating zugrunde?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "qualitaetssicherungsprozess",
            label: "Qualitätssicherungsprozess",
            description: "Gibt es einen Qualitätssicherungsprozess?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "fallsNeinGebenSieBitteDieGruendeAn",
            label: "Falls nein, geben Sie bitte die Gründe an",
            description: "Wenn Nein, bitte begründen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.qualitaetssicherungsprozess == "No",
          },
          {
            name: "strukturDesQualitaetssicherungsprozesses",
            label: "Struktur des Qualitätssicherungsprozesses",
            description:
              "Wie ist der Qualitätssicherungsprozess aufgebaut?\nBeschreibung des Prozesses, wie z.B. sichergestellt wird, dass keine falschen Daten oder unvollständige Daten erhoben werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.qualitaetssicherungsprozess == "Yes",
          },
          {
            name: "dieAktualitaetDerMethodik",
            label: "Die Aktualität der Methodik",
            description:
              "Wie wird die Aktualität der Methodik sichergestellt?\nAngaben dazu, wie Adhoc/kurzfristige Meldungen bei Emittenten und kurzfristige regulatorische Veränderungen überwacht und in die Methodik integriert werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.qualitaetssicherungsprozess == "Yes",
          },
          {
            name: "paisInDieAnalyseEinbezogen",
            label: "PAIs in die Analyse einbezogen",
            description: "Werden PAIs in der Analyse berücksichtigt?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "listeDerEingeschlossenenPais",
            label: "Liste der eingeschlossenen PAIs",
            description: "Welche PAIs werden in der Analyse berücksichtigt?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.paisInDieAnalyseEinbezogen == "Yes",
          },
          {
            name: "quelleDerPaiSammlung",
            label: "Quelle der PAI-Sammlung",
            description: "Welche Quellen werden für die Erhebung der PAIs verwendet?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.paisInDieAnalyseEinbezogen == "Yes",
          },
          {
            name: "umgangMitAusreissern",
            label: "Umgang mit Ausreißern",
            description: "Wie erfolgt der Umgang mit Ausreißern?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.general?.methodik?.paisInDieAnalyseEinbezogen == "Yes",
          },
          {
            name: "identifizierungVonKontroversenGeschaeften",
            label: "Identifizierung von kontroversen Geschäften",
            description: "Wie werden kontroverse Geschäftsfelder identifiziert?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "aktuelleKontroversen",
            label: "Aktuelle Kontroversen",
            description: "Wie wird die Aktualität der Kontroversen gewährleistet?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "kontroversenUmDieQuellenerfassung",
            label: "Kontroversen um die Quellenerfassung",
            description: "Welche Quellen werden zur Erfassung von Kontroversen genutzt?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "implementierung",
        label: "Implementierung",
        fields: [
          {
            name: "angeboteneSprachen",
            label: "Angebotene Sprachen",
            description: "In welchen Sprachen wird das Produkt/Tool/System angeboten?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "bereitgestellteDokumentationsarten",
            label: "Bereitgestellte Dokumentationsarten",
            description: "Welche Arten von Dokumentationen stellen Sie in welcher Form zur Verfügung?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "bereitgestellteDokumentationAufDeutsch",
            label: "Bereitgestellte Dokumentation auf Deutsch",
            description:
              "Wird eine detaillierte technische deutschsprachige Dokumentation des Systems zur Verfügung gestellt?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "leistungstests",
            label: "Leistungstests",
            description: "Wurden Performancetests durchgeführt und können Sie diese Ergebnisse zur Verfügung stellen?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "sicherheitstests",
            label: "Sicherheitstests",
            description: "Wurden Sicherheitstests durchgeführt und können Sie diese Ergebnisse zur Verfügung stellen?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "beschreibungDerSystemarchitektur",
            label: "Beschreibung der Systemarchitektur",
            description:
              "Geben Sie bitte eine kurze Beschreibung Ihrer Systemarchitektur (Datenbank, CPU, Prozessoren, Schnittstellen, Server etc.) bei ASP Betrieb an.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "erforderlichesClientBetriebssystem",
            label: "Erforderliches Client- Betriebssystem",
            description: "Welches Client Betriebssystem wird benötigt?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "angebotFuerFetteDuenneZitrischeKunden",
            label: "Angebot für fette/dünne/zitrische Kunden",
            description: "Wird der Client als Fat-Client, Thin-Client, Citrix-Client angeboten?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "serverBackup",
            label: "Server Backup",
            description: "Wie wird das Backup der Server durchgeführt?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "standardisiertesKonzeptZurWiederherstellungImKatastrophenfall",
            label: "Standardisiertes Konzept zur Wiederherstellung im Katastrophenfall",
            description:
              "Haben Sie ein standardisiertes Disaster Recovery Konzept? Fügen Sie hierzu bitte ein Testat oder einen Auszug seitens eines Prüfers bei.",

            component: "YesNoBaseDataPointFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "stammUndBewegungsdatenLesen",
            label: "Stamm- und Bewegungsdaten lesen",
            description: "Können Stamm- und Bewegungsdaten über eine Datawarehouse-Lösung eingelesen werden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "kompatibilitaetMitAnderenDatenquellen",
            label: "Kompatibilität mit anderen Datenquellen",
            description: "Ist Ihre Lösung mit anderen Datenquellen nutzbar?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "importDerErgebnisseInDasDataWarehouse",
            label: "Import der Ergebnisse in das Data Warehouse",
            description: "Können die Ergebnisse einem Datawarehouse zum Einlesen zur Verfügung gestellt werden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "erforderlichesDatenbanksystem",
            label: "Erforderliches Datenbanksystem",
            description: "Welches Datenbanksystem wird benötigt (Hersteller, Versionsnummer)?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "beschreibungDesDesignsUndDerStrukturDerDatenbankEn",
            label: "Beschreibung des Designs und der Struktur der Datenbank(en)",
            description: "Bitte beschreiben Sie den Aufbau und die Struktur der Datenbank(en).",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "direkterZugriffAufDieDatenbank",
            label: "Direkter Zugriff auf die Datenbank",
            description: "Ist ein direkter (lesender) Zugriff auf die Datenbank Tabellen möglich?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "schreibenderZugriffAufDieDatenbank",
            label: "Schreibender Zugriff auf die Datenbank",
            description: "Ist ein schreibender Zugriff direkt oder über Zwischentabellen auf die Datenbank möglich?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "unterstuetzungDerEchtzeitverarbeitung",
            label: "Unterstützung der Echtzeitverarbeitung",
            description: "Unterstützt das System Real-time Processing?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "unterstuetzungFuerZeitnaheVerarbeitung",
            label: "Unterstützung für zeitnahe Verarbeitung",
            description: "Unterstützt das System Near-time Processing?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "unterstuetzungDerStapelverarbeitung",
            label: "Unterstützung der Stapelverarbeitung",
            description: "Unterstützt das System Batch Processing?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "unterstuetzteBiLoesung",
            label: "Unterstützte BI-Lösung",
            description: "Welche BI-Lösung unterstützt Ihr System?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "flexibilitaetBeimImportExportVonDaten",
            label: "Flexibilität beim Import/Export von Daten",
            description:
              "Besitzt das System die Flexibilität, um über Standard-Dateiformate (xls, xlsx, csv, FundsXML, txt) Daten importieren und exportieren zu können?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "dauerhafteVerfuegbarkeit",
            label: "dauerhafte Verfügbarkeit",
            description: "Wird eine jederzeitige Verfügbarkeit (24hrs,7d) angeboten?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "uebertragenVonDatenhistorien",
            label: "Übertragen von Datenhistorien",
            description: "Welche Funktionen stellt das System zur Übernahme der Datenhistorien zur Verfügung?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "unterstuetzterZeitraumDerDatenhistorien",
            label: "Unterstützter Zeitraum der Datenhistorien",
            description: "Welchen Zeitraum an Datenhistorien unterstützt das System?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "fruehesterStartterminFuerEinIntegrationsprojekt",
            label: "Frühester Starttermin für ein Integrationsprojekt",
            description: "Ab wann könnte frühestens ein Integrationsprojekt starten?",

            component: "DateFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "geschaetzterZeitrahmenFuerDieVollstaendigeIntegrationDesProjekts",
            label: "Geschätzter Zeitrahmen für die vollständige Integration des Projekts",
            description:
              "Welchen Zeitraum schätzen Sie für ein vollständig abgeschlossenes Integrationsprojekt des beschriebenen Umfangs?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "durchschnittlicheAnzahlDerBenoetigtenRessourcen",
            label: "Durchschnittliche Anzahl der benötigten Ressourcen",
            description:
              "Wieviel Ressourcen werden im Durchschnitt auf Kundenseite während der Implementierung benötigt?",

            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "anzahlDerVerfuegbarenRessourcen",
            label: "Anzahl der verfügbaren Ressourcen",
            description:
              "Wieviele Ressourcen stehen ab wann und mit welcher Kapazität zur Umsetzung des Projekts zur Verfügung (Support, Entwicklung, Beratung)?",

            component: "NumberFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "kundenbetreuung",
            label: "Kundenbetreuung",
            description:
              "Wie und mit wievielen Mitarbeitern können Sie einen Kunden in den ersten sechs Monaten nach Einführung unterstützen?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
    ],
  },
  {
    name: "environmental",
    label: "Environmental",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "nachhaltigskeitsrisiken",
        label: "Nachhaltigskeitsrisiken",
        fields: [
          {
            name: "methodikFuerOekologischeNachhaltigkeitsrisiken",
            label: "Methodik für ökologische Nachhaltigkeitsrisiken",
            description:
              "Werden Nachhaltigkeitsrisiken bezogen auf den Bereich Umwelt in der Methodik abgebildet?\nNachhaltigkeitsrisiken können einen wesentlichen negativen Einfluss auf die Performance eines Unternehmens haben. Angaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Umwelt in der Methodik abgebildet werden.",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenOekologischeNachhaltigkeitsrisiken",
            label: "Wenn Nein, bitte begründen (ökologische Nachhaltigkeitsrisiken)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "No",
          },
          {
            name: "kartierteRisikenFuerDieOekologischeNachhaltigkeit",
            label: "Kartierte Risiken für die ökologische Nachhaltigkeit",
            description:
              "Welche Nachhaltigkeitsrisiken im Bereich Umwelt werden abgebildet?\nAufführung der Nachhaltigkeitsrisiken, die abgebildet werden können. (z.B. Klimarisiken, Risiken bzgl. Biodiversität, Risiken bzgl. Wasser)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
          },
          {
            name: "identifizierungDerWesentlichenRisikenFuerDieOekologischeNachhaltigkeitUndDerKonstruktionsmethodik",
            label:
              "Identifizierung der wesentlichen Risiken für die ökologische Nachhaltigkeit und der Konstruktionsmethodik",
            description:
              "Wie werden wesentliche Nachhaltigkeitsrisiken eines Unternehmens im Bereich Umwelt identifiziert und in der Methodik berücksichtigt?\nAngaben zur Wesentlichkeitsanalyse bei der Einstufung der Wesentlichkeit eines Risikos bezogen auf ein Unternehmen. Sowie Angaben dazu, wie sich die unterschiedliche Risikoeinstufung in der Methodik widerspiegelt.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
          },
          {
            name: "umweltbewertungUnterBeruecksichtigungVonNachhaltigkeitsrisiken",
            label: "Umweltbewertung unter Berücksichtigung von Nachhaltigkeitsrisiken",
            description:
              "Wie werden Nachhaltigkeitsrisiken in der Bewertung bezogen auf den Bereich Umwelt berücksichtigt?\nAngaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Umwelt in die Erstellung des Ratings miteinbezogen werden und wenn ja wie.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
          },
          {
            name: "risikenFuerDieOekologischeNachhaltigkeitAbsichern",
            label: "Risiken für die ökologische Nachhaltigkeit absichern",
            description: "Wie wird die Überwachung von Nachhaltigkeitsrisiken im Bereich Umwelt sichergestellt?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.methodikFuerOekologischeNachhaltigkeitsrisiken == "Yes",
          },
          {
            name: "vierAugenPruefung",
            label: "Vier-Augen-Prüfung",
            description: "Ist eine Vier-Augen-Verifizierung vorhanden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruenden",
            label: "Wenn Nein, bitte begründen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.vierAugenPruefung == "No",
          },
          {
            name: "beschreibungDerVierAugenPruefung",
            label: "Beschreibung der Vier-Augen- Prüfung",
            description: "Wie erfolgt die Vier-Augen-Verifizierung?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.nachhaltigskeitsrisiken?.vierAugenPruefung == "Yes",
          },
        ],
      },
      {
        name: "pais",
        label: "PAIs",
        fields: [
          {
            name: "sechsPaisTreibhausgasemissionen",
            label: "Sechs PAIs - Treibhausgasemissionen",
            description:
              "Werden die sechs PAIs bezogen auf Treibhausgasemissionen abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruenden",
            label: "Wenn Nein, bitte begründen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "No",
          },
          {
            name: "wennJaBitteDiePaisAuflisten",
            label: "Wenn Ja, bitte die PAIs auflisten",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
          },
          {
            name: "verwendeteSchluesselzahlenTreibhausgasemissionen",
            label: "Verwendete Schlüsselzahlen (Treibhausgasemissionen)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
          },
          {
            name: "datenerfassungTreibhausgasemissionen",
            label: "Datenerfassung (Treibhausgasemissionen)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten, Benchmarking)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungTreibhausgasemissionen",
            label: "Daten Plausibilitätsprüfung (Treibhausgasemissionen)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde erfasst.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
          },
          {
            name: "datenquelleTreibhausgasemissionen",
            label: "Datenquelle (Treibhausgasemissionen)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärungen etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.sechsPaisTreibhausgasemissionen == "Yes",
          },
          {
            name: "paisBiologischeVielfalt",
            label: "PAIs - biologische Vielfalt",
            description: "Wird der PAI auf Biodiversität abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenBiodiversitaet",
            label: "Wenn Nein, bitte begründen (Biodiversität)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paisBiologischeVielfalt == "No",
          },
          {
            name: "verwendeteSchluesselzahlenBiodiversitaet",
            label: "Verwendete Schlüsselzahlen (Biodiversität)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung des PAIs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
          },
          {
            name: "datenerfassungBiodiversitaet",
            label: "Datenerfassung (Biodiversität)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungBiodiversitaet",
            label: "Daten Plausibilitätsprüfung (Biodiversität)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
          },
          {
            name: "datenquelleBiodiversitaet",
            label: "Datenquelle (Biodiversität)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärungen, Interviews etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.pais?.paisBiologischeVielfalt == "Yes",
          },
          {
            name: "paiWasser",
            label: "PAI - Wasser",
            description: "Wird der PAI auf Wasser abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenWasser",
            label: "Wenn Nein, bitte begründen (Wasser)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "No",
          },
          {
            name: "verwendeteSchluesselzahlenWasser",
            label: "Verwendete Schlüsselzahlen (Wasser)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
          },
          {
            name: "datenerfassungWasser",
            label: "Datenerfassung (Wasser)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungWasser",
            label: "Daten Plausibilitätsprüfung (Wasser)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
          },
          {
            name: "datenquelleWasser",
            label: "Datenquelle (Wasser)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärungen, Interviews, Daten von NGOs etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiWasser == "Yes",
          },
          {
            name: "paiAbfall",
            label: "PAI - Abfall",
            description: "Wird der PAI auf Abfall abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenAbfall",
            label: "Wenn Nein, bitte begründen (Abfall)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "No",
          },
          {
            name: "verwendeteSchluesselzahlenAbfall",
            label: "Verwendete Schlüsselzahlen (Abfall)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung des PAIs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
          },
          {
            name: "datenerfassungAbfall",
            label: "Datenerfassung (Abfall)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungAbfall",
            label: "Daten Plausibilitätsprüfung (Abfall)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
          },
          {
            name: "datenquelleAbfall",
            label: "Datenquelle (Abfall)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, nichtfinanzielle Erklärung, Daten von NGOs etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiAbfall == "Yes",
          },
          {
            name: "paiUmweltAufDemLand",
            label: "PAI - Umwelt auf dem Land",
            description: "Wir der PAI auf Umwelt bei Staaten abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenUmweltAufDemLand",
            label: "Wenn Nein, bitte begründen (Umwelt auf dem Land)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiUmweltAufDemLand == "No",
          },
          {
            name: "verwendeteSchluesselzahlenUmweltAufDemLand",
            label: "Verwendete Schlüsselzahlen (Umwelt auf dem Land)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung des PAIs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
          },
          {
            name: "datenerfassungUmweltAufDemLand",
            label: "Datenerfassung (Umwelt auf dem Land)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungUmweltAufDemLand",
            label: "Daten Plausibilitätsprüfung (Umwelt auf dem Land)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
          },
          {
            name: "datenquelleUmweltAufDemLand",
            label: "Datenquelle (Umwelt auf dem Land)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Umweltbundesamt)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.environmental?.pais?.paiUmweltAufDemLand == "Yes",
          },
        ],
      },
      {
        name: "sfdr",
        label: "SFDR",
        fields: [
          {
            name: "methodikZurMessungEinesSignifikantenBeitragsZuEinemUmweltziel",
            label: "Methodik zur Messung eines signifikanten Beitrags zu einem Umweltziel",
            description:
              "Wie erfolgt die Abbildung eines wesentlichen Beitrages zu einem Umweltziel?\nAngaben darüber ob mit der Methodik ein wesentlicher Beitrag zu einem Umweltziel gemessen werden kann und wenn ja, zu welchem und wie.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "kontroverseGeschaeftsfelder",
        label: "Kontroverse Geschäftsfelder",
        fields: [
          {
            name: "ausschlussDerTabakerzeugungTabakerzeugung",
            label: "Ausschluss der Tabakerzeugung (Tabakerzeugung)",
            description: "Kann mit der Methodik Umsatz aus der Tabakproduktion ausgeschlossen werden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenTabakerzeugung",
            label: "Wenn Nein, bitte begründen (Tabakerzeugung)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugungTabakerzeugung == "No",
          },
          {
            name: "metrischVerwendetTabakerzeugung",
            label: "Metrisch verwendet (Tabakerzeugung)",
            description:
              "Welche Kennzahl wird für die Messung des Umsatzes aus Tabakproduktion herangezogen?\nAngaben zu der Zusammensetzung der Kennzahl zur Berechnung des Umsatzes aus der Tabakproduktion.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugungTabakerzeugung == "Yes",
          },
          {
            name: "methodikDerBerechnungTabakerzeugung",
            label: "Methodik der Berechnung (Tabakerzeugung)",
            description:
              "Wie erfolgt die Berechnung?\nAngaben zur Methodik zur Berechnung des Umsatzes aus der Tabakproduktion.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugungTabakerzeugung == "Yes",
          },
          {
            name: "verwendeteQuellenTabakerzeugung",
            label: "Verwendete Quellen (Tabakerzeugung)",
            description:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerTabakerzeugungTabakerzeugung == "Yes",
          },
          {
            name: "ausschlussDerKohlefoerderungUndVerteilung",
            label: "Ausschluss der Kohleförderung und -verteilung",
            description:
              "Kann mit der Methodik Umsatz aus der Herstellung und dem Vertrieb von Kohle ausgeschlossen werden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenKohleerzeugung",
            label: "Wenn Nein, bitte begründen (Kohleerzeugung)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "No",
          },
          {
            name: "metrischVerwendetKohleerzeugung",
            label: "Metrisch verwendet (Kohleerzeugung)",
            description:
              "Welche Kennzahl wird für die Messung des Umsatzes aus Kohle herangezogen?\nAngaben zu der Zusammensetzung der Kennzahl zur Berechnung des Umsatzes aus Kohle.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "Yes",
          },
          {
            name: "methodikDerBerechnungKohleerzeugung",
            label: "Methodik der Berechnung (Kohleerzeugung)",
            description: "Wie erfolgt die Berechnung?\nAngaben zur Methodik zur Berechnung des Umsatzes aus Kohle.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "Yes",
          },
          {
            name: "verwendeteQuellenKohleerzeugung",
            label: "Verwendete Quellen (Kohleerzeugung)",
            description:
              "Welche Quellen werden verwendet?Angabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.environmental?.kontroverseGeschaeftsfelder?.ausschlussDerKohlefoerderungUndVerteilung == "Yes",
          },
        ],
      },
    ],
  },
  {
    name: "social",
    label: "Social",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "nachhaltigskeitsrisiken",
        label: "Nachhaltigskeitsrisiken",
        fields: [
          {
            name: "methodikSozialeNachhaltigkeitsrisiken",
            label: "Methodik Soziale Nachhaltigkeitsrisiken",
            description:
              "Werden Nachhaltigkeitsrisiken bezogen auf den Bereich Soziales in der Methodik abgebildet?\nNachhaltigkeitsrisiken können einen wesentlichen negativen Einfluss auf die Performance eines Unternehmens haben. Angaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Soziales in der Methodik abgebildet werden.",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenNachhaltigkeitsrisiken",
            label: "Wenn Nein, bitte begründen (Nachhaltigkeitsrisiken)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.methodikSozialeNachhaltigkeitsrisiken == "No",
          },
          {
            name: "kartierteSozialeNachhaltigkeitsrisiken",
            label: "Kartierte soziale Nachhaltigkeitsrisiken ",
            description:
              "Welche Nachhaltigkeitsrisiken im Bereich Soziales werden abgebildet?\nAufführung der Nachhaltigkeitsrisiken, die abgebildet werden können (z.B. Risiken in Bezug auf Arbeitnehmerbelange, Demographie, Gesundheitsschutz).",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.methodikSozialeNachhaltigkeitsrisiken == "Yes",
          },
          {
            name: "identifizierungWesentlicherSozialerNachhaltigkeitsrisikenUndKonstruktionsmethodik",
            label: "Identifizierung wesentlicher sozialer Nachhaltigkeitsrisiken und Konstruktionsmethodik",
            description:
              "Wie werden wesentliche Nachhaltigkeitsrisiken eines Unternehmens im Bereich Soziales identifiziert und berücksichtigt?\nAngaben zur Wesentlichkeitsanalyse bei der Einstufung der Wesentlichkeit eines Risikos bezogen auf ein Unternehmen. Sowie Angaben dazu, wie sich die unterschiedliche Risikoeinstufung in der Methodik widerspiegelt.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.methodikSozialeNachhaltigkeitsrisiken == "Yes",
          },
          {
            name: "sozialeBewertungUnterBeruecksichtigungVonNachhaltigkeitsrisiken",
            label: "Soziale Bewertung unter Berücksichtigung von Nachhaltigkeitsrisiken",
            description:
              "Wie werden Nachhaltigkeitsrisiken in der Bewertung bezogen auf den Bereich Soziales berücksichtigt?\nAngaben dazu, ob Nachhaltigkeitsrisiken aus dem Bereich Soziales in die Erstellung des Ratings miteinbezogen werden und wenn ja wie.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "sozialeNachhaltigkeitsrisikenAbsichern",
            label: "Soziale Nachhaltigkeitsrisiken absichern",
            description: "Wie wird die Überwachung von Nachhaltigkeitsrisiken im Bereich Soziales sichergestellt?",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "quelle",
            label: "Quelle",
            description:
              "Welche Quellen werden für die Erfassung von Nachhaltigkeitsrisiken im Bereich Soziales verwendet?\nAngabe von Quellen, zum Beispiel Nachhaltigkeitsberichte, ethische Richtlinien, etc.)",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "vierAugenPruefung",
            label: "Vier-Augen-Prüfung",
            description: "Ist eine Vier-Augen-Verifizierung vorhanden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenVierAugenPruefung",
            label: "Wenn Nein, bitte begründen (Vier-Augen-Prüfung)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.vierAugenPruefung == "No",
          },
          {
            name: "beschreibungDerVierAugenPruefung",
            label: "Beschreibung der Vier-Augen- Prüfung",
            description: "Wie erfolgt die Vier-Augen-Verifizierung?",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.nachhaltigskeitsrisiken?.vierAugenPruefung == "Yes",
          },
        ],
      },
      {
        name: "pais",
        label: "PAIs",
        fields: [
          {
            name: "paiSozial",
            label: "PAI - sozial ",
            description:
              "Werden die Sozialen PAIs bei Unternehmen abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenSozialeUnternehmen",
            label: "Wenn Nein, bitte begründen (soziale Unternehmen)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "No",
          },
          {
            name: "verwendeteSchluesselzahlenSozialeUnternehmen",
            label: "Verwendete Schlüsselzahlen (soziale Unternehmen)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
          },
          {
            name: "datenerfassungSozialeUnternehmen",
            label: "Datenerfassung (soziale Unternehmen)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungSozialeUnternehmen",
            label: "Daten Plausibilitätsprüfung (soziale Unternehmen)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
          },
          {
            name: "datenquelleSozialeUnternehmen",
            label: "Datenquelle (soziale Unternehmen)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Geschäftsberichte von Unternehmen, Gender Pay Report etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozial == "Yes",
          },
          {
            name: "paiSozialesAufDemLand",
            label: "PAI - Soziales auf dem Land",
            description: "Werden die Sozialen PAIs bei Staaten abgebildet?\nJa/Nein bezogen auf Annex I C(2022)1931",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenSozialesAufDemLand",
            label: "Wenn Nein, bitte begründen (Soziales auf dem Land)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "No",
          },
          {
            name: "verwendeteSchluesselzahlenSozialesAufDemLand",
            label: "Verwendete Schlüsselzahlen (Soziales auf dem Land)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngabe der Kennzahlen, die zur Berechnung und Abbildung der PAIs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
          },
          {
            name: "datenerfassungSozialesAufDemLand",
            label: "Datenerfassung (Soziales auf dem Land)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zur Datenerhebung der o.g. Kennzahlen (z.B. Fragebogen, Interviews, Übernahme der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungSozialesAufDemLand",
            label: "Daten Plausibilitätsprüfung (Soziales auf dem Land)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
          },
          {
            name: "datenquelleSozialesAufDemLand",
            label: "Datenquelle (Soziales auf dem Land)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen zur Erhebung der KPIs, zum Beispiel Freedom House Index",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.social?.pais?.paiSozialesAufDemLand == "Yes",
          },
        ],
      },
      {
        name: "sfdr",
        label: "SFDR",
        fields: [
          {
            name: "methodikZurMessungDesSignifikantenBeitragsZuEinemGesellschaftlichenZiel",
            label: "Methodik zur Messung des signifikanten Beitrags zu einem gesellschaftlichen Ziel",
            description:
              "Wie erfolgt die Abbildung eines wesentlichen Beitrages zu einem sozialen Ziel?\nAngaben darüber ob mit der Methodik ein wesentlicher Beitrag zu einem sozialen Ziel gemessen werden kann und wenn ja, wie.",

            component: "InputTextFormField",
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "kontroverseGeschaeftsfelder",
        label: "Kontroverse Geschäftsfelder",
        fields: [
          {
            name: "herstellungOderVertriebVonWaffenAusschluss",
            label: "Herstellung oder Vertrieb von Waffen Ausschluss",
            description:
              "Kann mit der Methodik Umsatz aus der Herstellung oder dem Vertrieb von Waffen ausgeschlossen werden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenWaffen",
            label: "Wenn Nein, bitte begründen (Waffen)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "No",
          },
          {
            name: "metrischVerwendetWaffen",
            label: "Metrisch verwendet (Waffen)",
            description:
              "Welche Kennzahl wird für die Messung des Umsatzes aus Waffen herangezogen?\nAngaben zu der Zusammensetzung der Kennzahl zur Berechnung des Umsatzes aus Waffen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "Yes",
          },
          {
            name: "methodikDerBerechnungWaffen",
            label: "Methodik der Berechnung (Waffen)",
            description: "Wie erfolgt die Berechnung?\nAngaben zur Methodik zur Berechnung des Umsatzes aus Waffen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "Yes",
          },
          {
            name: "verwendeteQuellenWaffen",
            label: "Verwendete Quellen (Waffen)",
            description:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.herstellungOderVertriebVonWaffenAusschluss == "Yes",
          },
          {
            name: "ausschlussVerbotenerWaffen",
            label: "Ausschluss verbotener Waffen",
            description: "Können mit der Methodik geächtete Waffen ausgeschlossen werden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenVerboteneWaffen",
            label: "Wenn Nein, bitte begründen (verbotene Waffen)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.ausschlussVerbotenerWaffen == "No",
          },
          {
            name: "metrischVerwendetVerboteneWaffen",
            label: "Metrisch verwendet (verbotene Waffen)",
            description:
              "Welche Kennzahl wird für geächtete Waffen herangezogen?\nAngaben zu den Bestandteilen der Kennzahl zur Abbildung geächteter Waffen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.ausschlussVerbotenerWaffen == "Yes",
          },
          {
            name: "verwendeteQuellenVerboteneWaffen",
            label: "Verwendete Quellen (verbotene Waffen)",
            description:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.social?.kontroverseGeschaeftsfelder?.ausschlussVerbotenerWaffen == "Yes",
          },
        ],
      },
    ],
  },
  {
    name: "governance",
    label: "Governance",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "goodGovernance",
        label: "Good Governance",
        fields: [
          {
            name: "methodikDerGutenRegierungsfuehrung",
            label: "Methodik der guten Regierungsführung",
            description: "Können Good Governance Aspekte im Rahmen der Methodik berücksichtigt werden?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenGoodGovernance",
            label: "Wenn Nein, bitte begründen (Good Governance)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "No",
          },
          {
            name: "definitionVonGuterRegierungsfuehrung",
            label: "Definition von guter Regierungsführung",
            description:
              "Wie wird Good Governance im Rahmen der Methodik definiert?\nDefinition von Good Governance im Rahmen der Methodik.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "Yes",
          },
          {
            name: "listeDerKpisFuerGuteUnternehmensfuehrung",
            label: "Liste der KPIs für gute Unternehmensführung",
            description:
              "Welche KPIs werden zur Berurteilung einer Good Governance genutzt?\nAufführung der KPIs zur Beurteilung von Good Governance.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "Yes",
          },
          {
            name: "verwendeteQuellenGoodGovernance",
            label: "Verwendete Quellen (Good Governance)",
            description:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel Nachhaltigkeitsberichte, Internetseiten von Unternehmen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.goodGovernance?.methodikDerGutenRegierungsfuehrung == "Yes",
          },
        ],
      },
      {
        name: "globalCompact",
        label: "Global Compact",
        fields: [
          {
            name: "beruecksichtigungDesUngc",
            label: "Berücksichtigung des UNGC",
            description: "Werden die UNGC in der Analyse berücksichtigt?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenGlobalCompact",
            label: "Wenn Nein, bitte begründen (Global Compact)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.globalCompact?.beruecksichtigungDesUngc == "No",
          },
          {
            name: "beruecksichtigungDerUngcBeschreibung",
            label: "Berücksichtigung der UNGC- Beschreibung",
            description:
              "Wie erfolgt die Berücksichtigung der UNGC?\nWenn eine Berücksichtigung der UNGC erfolgt Angaben darüber, wie die Berücksichtigung abgebildet wird.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.globalCompact?.beruecksichtigungDesUngc == "Yes",
          },
          {
            name: "verwendeteQuellenGlobalCompact",
            label: "Verwendete Quellen (Global Compact)",
            description:
              "Welche Quellen werden verwendet?\nAngabe von Quellen, zum Beispiel UNGC, Nachhaltigkeitsberichte",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.globalCompact?.beruecksichtigungDesUngc == "Yes",
          },
        ],
      },
      {
        name: "kontroverseGeschaeftsfelder",
        label: "Kontroverse Geschäftsfelder",
        fields: [
          {
            name: "kontroversenImBereichDerBestechungUndKorruption",
            label: "Kontroversen im Bereich der Bestechung und Korruption",
            description: "Werden Kontroversen im Bereich Bestechung und Korruption abgebildet?",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenKontroverseGeschaeftsfelder",
            label: "Wenn Nein, bitte begründen (Kontroverse Geschäftsfelder)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "No",
          },
          {
            name: "verwendeteMetrikenUndMethodik",
            label: "Verwendete Metriken und Methodik",
            description:
              "Wie werden Kontroversen im Bereich Bestechung und Korruption abgebildet?\nAngabe von Kennzahlen und Methodiken zur Abbildung von Kontroversen im Bereich Bestechung und Korruption.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "Yes",
          },
          {
            name: "verwendeteQuellenKontroverseGeschaeftsfelder",
            label: "Verwendete Quellen (Kontroverse Geschäftsfelder)",
            description:
              "Welche Datenquellen werden verwendet?\nAngabe von Quellen, zum Beispiel rennomierte Wirtschafts- und Finanzzeitungen, Glass Lewis",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "Yes",
          },
          {
            name: "dieAktualitaetDerKontroversenImBereichBestechungUndKorruption",
            label: "Die Aktualität der Kontroversen im Bereich Bestechung und Korruption",
            description:
              "Wie wird die Aktualität der Kontroversen im Bereich Bestechung und Korruption gewährleistet?\nAngaben dazu, wie Adhoc/kurzfristige Meldungen bei Emittenten überwacht und in die Methodik integriert werden sowie darüber in welchem Zeitraum die Kontroversen angepasst werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.governance?.kontroverseGeschaeftsfelder?.kontroversenImBereichDerBestechungUndKorruption == "Yes",
          },
        ],
      },
    ],
  },
  {
    name: "sdg",
    label: "SDG",
    color: "",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "keineArmut",
        label: "Keine Armut",
        fields: [
          {
            name: "sdgKeineArmut",
            label: "abctest",
            description: "deftestHU",

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenKeineArmut",
            label: "Wenn Nein, bitte begründen (Keine Armut)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keineArmut?.sdgKeineArmut == "No",
          },
          {
            name: "verwendeteSchluesselzahlenKeineArmut",
            label: "Verwendete Schlüsselzahlen (Keine Armut)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keineArmut?.sdgKeineArmut == "Yes",
          },
          {
            name: "datenerfassungKeineArmut",
            label: "Datenerfassung (Keine Armut)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keineArmut?.sdgKeineArmut == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungKeineArmut",
            label: "Daten Plausibilitätsprüfung (Keine Armut)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keineArmut?.sdgKeineArmut == "Yes",
          },
          {
            name: "datenquelleKeineArmut",
            label: "Datenquelle (Keine Armut)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keineArmut?.sdgKeineArmut == "Yes",
          },
        ],
      },
      {
        name: "keinHunger",
        label: "Kein Hunger",
        fields: [
          {
            name: "sdgKeinHunger",
            label: "SDG - Kein Hunger",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "kein Hunger" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "verwendeteSchluesselzahlenKeinHunger",
            label: "Verwendete Schlüsselzahlen (Kein Hunger)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keinHunger?.sdgKeinHunger == "Yes",
          },
          {
            name: "datenerfassungKeinHunger",
            label: "Datenerfassung (Kein Hunger)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keinHunger?.sdgKeinHunger == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungKeinHunger",
            label: "Daten Plausibilitätsprüfung (Kein Hunger)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keinHunger?.sdgKeinHunger == "Yes",
          },
          {
            name: "datenquelleKeinHunger",
            label: "Datenquelle (Kein Hunger)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.keinHunger?.sdgKeinHunger == "Yes",
          },
        ],
      },
      {
        name: "gesundheitUndWohlergehen",
        label: "Gesundheit und Wohlergehen",
        fields: [
          {
            name: "sdgGesundheitUndWohlergehen",
            label: "SDG - Gesundheit und Wohlergehen",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Gesundheit und Wohlergehen" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenGesundheitUndWohlergehen",
            label: "Wenn Nein, bitte begründen (Gesundheit und Wohlergehen)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.gesundheitUndWohlergehen?.sdgGesundheitUndWohlergehen == "No",
          },
          {
            name: "verwendeteSchluesselzahlenGesundheitUndWohlergehen",
            label: "Verwendete Schlüsselzahlen (Gesundheit und Wohlergehen)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.gesundheitUndWohlergehen?.sdgGesundheitUndWohlergehen == "Yes",
          },
          {
            name: "datenerfassungGesundheitUndWohlergehen",
            label: "Datenerfassung (Gesundheit und Wohlergehen)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten).",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.gesundheitUndWohlergehen?.sdgGesundheitUndWohlergehen == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungGesundheitUndWohlergehen",
            label: "Daten Plausibilitätsprüfung (Gesundheit und Wohlergehen)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.gesundheitUndWohlergehen?.sdgGesundheitUndWohlergehen == "Yes",
          },
          {
            name: "datenquelleGesundheitUndWohlergehen",
            label: "Datenquelle (Gesundheit und Wohlergehen)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Daten von NGOs etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.gesundheitUndWohlergehen?.sdgGesundheitUndWohlergehen == "Yes",
          },
        ],
      },
      {
        name: "hochwertigeBildung",
        label: "Hochwertige Bildung",
        fields: [
          {
            name: "sdgHochwertigeBildung",
            label: "SDG - Hochwertige Bildung",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Hochwertige Bildung" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenHochwertigeBildung",
            label: "Wenn Nein, bitte begründen (Hochwertige Bildung)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.hochwertigeBildung?.sdgHochwertigeBildung == "No",
          },
          {
            name: "verwendeteSchluesselzahlenHochwertigeBildung",
            label: "Verwendete Schlüsselzahlen (Hochwertige Bildung)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.hochwertigeBildung?.sdgHochwertigeBildung == "Yes",
          },
          {
            name: "datenerfassungHochwertigeBildung",
            label: "Datenerfassung (Hochwertige Bildung)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.hochwertigeBildung?.sdgHochwertigeBildung == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungHochwertigeBildung",
            label: "Daten Plausibilitätsprüfung (Hochwertige Bildung)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.hochwertigeBildung?.sdgHochwertigeBildung == "Yes",
          },
          {
            name: "datenquelleHochwertigeBildung",
            label: "Datenquelle (Hochwertige Bildung)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.hochwertigeBildung?.sdgHochwertigeBildung == "Yes",
          },
        ],
      },
      {
        name: "geschlechtergleichheit",
        label: "Geschlechtergleichheit",
        fields: [
          {
            name: "sdgGeschlechtergleichheit",
            label: "SDG - Geschlechtergleichheit",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Geschlechtergleichheit" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenGeschlechtergleichheit",
            label: "Wenn Nein, bitte begründen (Geschlechtergleichheit)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.geschlechtergleichheit?.sdgGeschlechtergleichheit == "No",
          },
          {
            name: "verwendeteSchluesselzahlenGeschlechtergleichheit",
            label: "Verwendete Schlüsselzahlen (Geschlechtergleichheit)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.geschlechtergleichheit?.sdgGeschlechtergleichheit == "Yes",
          },
          {
            name: "datenerfassungGeschlechtergleichheit",
            label: "Datenerfassung (Geschlechtergleichheit)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.geschlechtergleichheit?.sdgGeschlechtergleichheit == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungGeschlechtergleichheit",
            label: "Daten Plausibilitätsprüfung (Geschlechtergleichheit)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.geschlechtergleichheit?.sdgGeschlechtergleichheit == "Yes",
          },
          {
            name: "datenquelleGeschlechtergleichheit",
            label: "Datenquelle (Geschlechtergleichheit)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.geschlechtergleichheit?.sdgGeschlechtergleichheit == "Yes",
          },
        ],
      },
      {
        name: "sauberesWasserUndSanitaereEinrichtungen",
        label: "Sauberes Wasser und sanitäre Einrichtungen",
        fields: [
          {
            name: "sdgSauberesWasserUndSanitaereEinrichtungen",
            label: "SDG - Sauberes Wasser und sanitäre Einrichtungen",
            description:
              'Kann mit der Methodik ein Beitrag zum SDG "Sauberes Wasser und Sanitäreinrichtungen" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenSauberesWasserUndSanitaereEinrichtungen",
            label: "Wenn Nein, bitte begründen (Sauberes Wasser und sanitäre Einrichtungen)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.sauberesWasserUndSanitaereEinrichtungen?.sdgSauberesWasserUndSanitaereEinrichtungen == "No",
          },
          {
            name: "verwendeteSchluesselzahlenSauberesWasserUndSanitaereEinrichtungen",
            label: "Verwendete Schlüsselzahlen (Sauberes Wasser und sanitäre Einrichtungen)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.sauberesWasserUndSanitaereEinrichtungen?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
          },
          {
            name: "datenerfassungSauberesWasserUndSanitaereEinrichtungen",
            label: "Datenerfassung (Sauberes Wasser und sanitäre Einrichtungen)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.sauberesWasserUndSanitaereEinrichtungen?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungSauberesWasserUndSanitaereEinrichtungen",
            label: "Daten Plausibilitätsprüfung (Sauberes Wasser und sanitäre Einrichtungen)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.sauberesWasserUndSanitaereEinrichtungen?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
          },
          {
            name: "datenquelleSauberesWasserUndSanitaereEinrichtungen",
            label: "Datenquelle (Sauberes Wasser und sanitäre Einrichtungen)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.sauberesWasserUndSanitaereEinrichtungen?.sdgSauberesWasserUndSanitaereEinrichtungen == "Yes",
          },
        ],
      },
      {
        name: "bezahlbareUndSaubereEnergie",
        label: "Bezahlbare und saubere Energie",
        fields: [
          {
            name: "sdgBezahlbareUndSaubereEnergie",
            label: "SDG - Bezahlbare und saubere Energie",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Bezahlbare und saubere Energie" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenBezahlbareUndSaubereEnergie",
            label: "Wenn Nein, bitte begründen (Bezahlbare und saubere Energie)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.bezahlbareUndSaubereEnergie?.sdgBezahlbareUndSaubereEnergie == "No",
          },
          {
            name: "verwendeteSchluesselzahlenBezahlbareUndSaubereEnergie",
            label: "Verwendete Schlüsselzahlen (Bezahlbare und saubere Energie)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.bezahlbareUndSaubereEnergie?.sdgBezahlbareUndSaubereEnergie == "Yes",
          },
          {
            name: "datenerfassungBezahlbareUndSaubereEnergie",
            label: "Datenerfassung (Bezahlbare und saubere Energie)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.bezahlbareUndSaubereEnergie?.sdgBezahlbareUndSaubereEnergie == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungBezahlbareUndSaubereEnergie",
            label: "Daten Plausibilitätsprüfung (Bezahlbare und saubere Energie)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.bezahlbareUndSaubereEnergie?.sdgBezahlbareUndSaubereEnergie == "Yes",
          },
          {
            name: "datenquelleBezahlbareUndSaubereEnergie",
            label: "Datenquelle (Bezahlbare und saubere Energie)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.bezahlbareUndSaubereEnergie?.sdgBezahlbareUndSaubereEnergie == "Yes",
          },
        ],
      },
      {
        name: "menschenwuerdigeArbeitUndWirtschaftswachstum",
        label: "Menschenwürdige Arbeit und Wirtschaftswachstum",
        fields: [
          {
            name: "sdgMenschenwuerdigeArbeitUndWirtschaftswachstum",
            label: "SDG - Menschenwürdige Arbeit und Wirtschaftswachstum",
            description:
              'Kann mit der Methodik ein Beitrag zum SDG "Menschenwürdige Arbeit und Wirtschaftswachstum" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenMenschenwuerdigeArbeitUndWirtschaftswachstum",
            label: "Wenn Nein, bitte begründen (Menschenwürdige Arbeit und Wirtschaftswachstum)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.menschenwuerdigeArbeitUndWirtschaftswachstum
                ?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "No",
          },
          {
            name: "verwendeteSchluesselzahlenMenschenwuerdigeArbeitUndWirtschaftswachstum",
            label: "Verwendete Schlüsselzahlen (Menschenwürdige Arbeit und Wirtschaftswachstum)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.menschenwuerdigeArbeitUndWirtschaftswachstum
                ?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
          },
          {
            name: "datenerfassungMenschenwuerdigeArbeitUndWirtschaftswachstum",
            label: "Datenerfassung (Menschenwürdige Arbeit und Wirtschaftswachstum)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.menschenwuerdigeArbeitUndWirtschaftswachstum
                ?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungMenschenwuerdigeArbeitUndWirtschaftswachstum",
            label: "Daten Plausibilitätsprüfung (Menschenwürdige Arbeit und Wirtschaftswachstum)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.menschenwuerdigeArbeitUndWirtschaftswachstum
                ?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
          },
          {
            name: "datenquelleMenschenwuerdigeArbeitUndWirtschaftswachstum",
            label: "Datenquelle (Menschenwürdige Arbeit und Wirtschaftswachstum)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.menschenwuerdigeArbeitUndWirtschaftswachstum
                ?.sdgMenschenwuerdigeArbeitUndWirtschaftswachstum == "Yes",
          },
        ],
      },
      {
        name: "industrieInnovationUndInfrastruktur",
        label: " Industrie, Innovation und Infrastruktur",
        fields: [
          {
            name: "sdgIndustrieInnovationUndInfrastruktur",
            label: "SDG - Industrie, Innovation und Infrastruktur",
            description:
              'Kann mit der Methodik ein Beitrag zum SDG "Industrie, Innovation und Infrastruktur" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenIndustrieInnovationUndInfrastruktur",
            label: "Wenn Nein, bitte begründen (Industrie, Innovation und Infrastruktur)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.industrieInnovationUndInfrastruktur?.sdgIndustrieInnovationUndInfrastruktur == "No",
          },
          {
            name: "verwendeteSchluesselzahlenIndustrieInnovationUndInfrastruktur",
            label: "Verwendete Schlüsselzahlen (Industrie, Innovation und Infrastruktur)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.industrieInnovationUndInfrastruktur?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
          },
          {
            name: "datenerfassungIndustrieInnovationUndInfrastruktur",
            label: "Datenerfassung (Industrie, Innovation und Infrastruktur)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.industrieInnovationUndInfrastruktur?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungIndustrieInnovationUndInfrastruktur",
            label: "Daten Plausibilitätsprüfung (Industrie, Innovation und Infrastruktur)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.industrieInnovationUndInfrastruktur?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
          },
          {
            name: "datenquelleIndustrieInnovationUndInfrastruktur",
            label: "Datenquelle (Industrie, Innovation und Infrastruktur)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.industrieInnovationUndInfrastruktur?.sdgIndustrieInnovationUndInfrastruktur == "Yes",
          },
        ],
      },
      {
        name: "wenigerUngleichheiten",
        label: "Weniger Ungleichheiten",
        fields: [
          {
            name: "sdgWenigerUngleichheiten",
            label: "SDG - Weniger Ungleichheiten",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Weniger Ungleichheiten" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenWenigerUngleichheiten",
            label: "Wenn Nein, bitte begründen (Weniger Ungleichheiten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.wenigerUngleichheiten?.sdgWenigerUngleichheiten == "No",
          },
          {
            name: "verwendeteSchluesselzahlenWenigerUngleichheiten",
            label: "Verwendete Schlüsselzahlen (Weniger Ungleichheiten)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.wenigerUngleichheiten?.sdgWenigerUngleichheiten == "Yes",
          },
          {
            name: "datenerfassungWenigerUngleichheiten",
            label: "Datenerfassung (Weniger Ungleichheiten)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.wenigerUngleichheiten?.sdgWenigerUngleichheiten == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungWenigerUngleichheiten",
            label: "Daten Plausibilitätsprüfung (Weniger Ungleichheiten)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.wenigerUngleichheiten?.sdgWenigerUngleichheiten == "Yes",
          },
          {
            name: "datenquelleWenigerUngleichheiten",
            label: "Datenquelle (Weniger Ungleichheiten)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.wenigerUngleichheiten?.sdgWenigerUngleichheiten == "Yes",
          },
        ],
      },
      {
        name: "nachhaltigeStaedteUndGemeinden",
        label: "Nachhaltige Städte und Gemeinden",
        fields: [
          {
            name: "sdgNachhaltigeStaedteUndGemeinden",
            label: "SDG - Nachhaltige Städte und Gemeinden",
            description:
              'Kann mit der Methodik ein Beitrag zum SDG "Nachhaltige Städte und Gemeinden" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenNachhaltigeStaedteUndGemeinden",
            label: "Wenn Nein, bitte begründen (Nachhaltige Städte und Gemeinden)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaltigeStaedteUndGemeinden?.sdgNachhaltigeStaedteUndGemeinden == "No",
          },
          {
            name: "verwendeteSchluesselzahlenNachhaltigeStaedteUndGemeinden",
            label: "Verwendete Schlüsselzahlen (Nachhaltige Städte und Gemeinden)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaltigeStaedteUndGemeinden?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
          },
          {
            name: "datenerfassungNachhaltigeStaedteUndGemeinden",
            label: "Datenerfassung (Nachhaltige Städte und Gemeinden)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaltigeStaedteUndGemeinden?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungNachhaltigeStaedteUndGemeinden",
            label: "Daten Plausibilitätsprüfung (Nachhaltige Städte und Gemeinden)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaltigeStaedteUndGemeinden?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
          },
          {
            name: "datenquelleNachhaltigeStaedteUndGemeinden",
            label: "Datenquelle (Nachhaltige Städte und Gemeinden)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaltigeStaedteUndGemeinden?.sdgNachhaltigeStaedteUndGemeinden == "Yes",
          },
        ],
      },
      {
        name: "nachhaligerKonsumUndProduktion",
        label: "Nachhaliger Konsum und Produktion",
        fields: [
          {
            name: "sdgNachhaligerKonsumUndProduktion",
            label: "SDG - Nachhaliger Konsum und Produktion",
            description:
              'Kann mit der Methodik ein Beitrag zum SDG "Nachhaliger Konsum und Produktion" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenNachhaligerKonsumUndProduktion",
            label: "Wenn Nein, bitte begründen (Nachhaliger Konsum und Produktion)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaligerKonsumUndProduktion?.sdgNachhaligerKonsumUndProduktion == "No",
          },
          {
            name: "verwendeteSchluesselzahlenNachhaligerKonsumUndProduktion",
            label: "Verwendete Schlüsselzahlen (Nachhaliger Konsum und Produktion)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaligerKonsumUndProduktion?.sdgNachhaligerKonsumUndProduktion == "Yes",
          },
          {
            name: "datenerfassungNachhaligerKonsumUndProduktion",
            label: "Datenerfassung (Nachhaliger Konsum und Produktion)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaligerKonsumUndProduktion?.sdgNachhaligerKonsumUndProduktion == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungNachhaligerKonsumUndProduktion",
            label: "Daten Plausibilitätsprüfung (Nachhaliger Konsum und Produktion)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaligerKonsumUndProduktion?.sdgNachhaligerKonsumUndProduktion == "Yes",
          },
          {
            name: "datenquelleNachhaligerKonsumUndProduktion",
            label: "Datenquelle (Nachhaliger Konsum und Produktion)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.nachhaligerKonsumUndProduktion?.sdgNachhaligerKonsumUndProduktion == "Yes",
          },
        ],
      },
      {
        name: "massnahmenZumKlimaschutz",
        label: "Maßnahmen zum Klimaschutz",
        fields: [
          {
            name: "sdgMassnahmenZumKlimaschutz",
            label: "SDG - Maßnahmen zum Klimaschutz",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Maßnahmen zum Klimaschutz" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenMassnahmenZumKlimaschutz",
            label: "Wenn Nein, bitte begründen (Maßnahmen zum Klimaschutz)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.massnahmenZumKlimaschutz?.sdgMassnahmenZumKlimaschutz == "No",
          },
          {
            name: "verwendeteSchluesselzahlenMassnahmenZumKlimaschutz",
            label: "Verwendete Schlüsselzahlen (Maßnahmen zum Klimaschutz)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.massnahmenZumKlimaschutz?.sdgMassnahmenZumKlimaschutz == "Yes",
          },
          {
            name: "datenerfassungMassnahmenZumKlimaschutz",
            label: "Datenerfassung (Maßnahmen zum Klimaschutz)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.massnahmenZumKlimaschutz?.sdgMassnahmenZumKlimaschutz == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungMassnahmenZumKlimaschutz",
            label: "Daten Plausibilitätsprüfung (Maßnahmen zum Klimaschutz)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.massnahmenZumKlimaschutz?.sdgMassnahmenZumKlimaschutz == "Yes",
          },
          {
            name: "datenquelleMassnahmenZumKlimaschutz",
            label: "Datenquelle (Maßnahmen zum Klimaschutz)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.massnahmenZumKlimaschutz?.sdgMassnahmenZumKlimaschutz == "Yes",
          },
        ],
      },
      {
        name: "lebenUnterWasser",
        label: "Leben unter Wasser",
        fields: [
          {
            name: "sdgLebenUnterWasser",
            label: "SDG - Leben unter Wasser",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Leben unter Wasser" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenLebenUnterWasser",
            label: "Wenn Nein, bitte begründen (Leben unter Wasser)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenUnterWasser?.sdgLebenUnterWasser == "No",
          },
          {
            name: "verwendeteSchluesselzahlenLebenUnterWasser",
            label: "Verwendete Schlüsselzahlen (Leben unter Wasser)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenUnterWasser?.sdgLebenUnterWasser == "Yes",
          },
          {
            name: "datenerfassungLebenUnterWasser",
            label: "Datenerfassung (Leben unter Wasser)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenUnterWasser?.sdgLebenUnterWasser == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungLebenUnterWasser",
            label: "Daten Plausibilitätsprüfung (Leben unter Wasser)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenUnterWasser?.sdgLebenUnterWasser == "Yes",
          },
          {
            name: "datenquelleLebenUnterWasser",
            label: "Datenquelle (Leben unter Wasser)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenUnterWasser?.sdgLebenUnterWasser == "Yes",
          },
        ],
      },
      {
        name: "lebenAnLand",
        label: "Leben an Land",
        fields: [
          {
            name: "sdgLebenAnLand",
            label: "SDG - Leben an Land",
            description: 'Kann mit der Methodik ein Beitrag zum SDG "Leben an Land" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenLebenAnLand",
            label: "Wenn Nein, bitte begründen (Leben an Land)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenAnLand?.sdgLebenAnLand == "No",
          },
          {
            name: "verwendeteSchluesselzahlenLebenAnLand",
            label: "Verwendete Schlüsselzahlen (Leben an Land)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenAnLand?.sdgLebenAnLand == "Yes",
          },
          {
            name: "datenerfassungLebenAnLand",
            label: "Datenerfassung (Leben an Land)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenAnLand?.sdgLebenAnLand == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungLebenAnLand",
            label: "Daten Plausibilitätsprüfung (Leben an Land)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenAnLand?.sdgLebenAnLand == "Yes",
          },
          {
            name: "datenquelleLebenAnLand",
            label: "Datenquelle (Leben an Land)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean => dataset.sdg?.lebenAnLand?.sdgLebenAnLand == "Yes",
          },
        ],
      },
      {
        name: "friedenGerechtigkeitUndStarkeInstitutionen",
        label: "Frieden, Gerechtigkeit und starke Institutionen",
        fields: [
          {
            name: "sdgFriedenGerechtigkeitUndStarkeInstitutionen",
            label: "SDG - Frieden, Gerechtigkeit und starke Institutionen",
            description:
              'Kann mit der Methodik ein Beitrag zum SDG "Frieden, Gerechtigkeit und starke Institutionen" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenFriedenGerechtigkeitUndStarkeInstitutionen",
            label: "Wenn Nein, bitte begründen (Frieden, Gerechtigkeit und starke Institutionen)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.friedenGerechtigkeitUndStarkeInstitutionen?.sdgFriedenGerechtigkeitUndStarkeInstitutionen ==
              "No",
          },
          {
            name: "verwendeteSchluesselzahlenFriedenGerechtigkeitUndStarkeInstitutionen",
            label: "Verwendete Schlüsselzahlen (Frieden, Gerechtigkeit und starke Institutionen)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.friedenGerechtigkeitUndStarkeInstitutionen?.sdgFriedenGerechtigkeitUndStarkeInstitutionen ==
              "Yes",
          },
          {
            name: "datenerfassungFriedenGerechtigkeitUndStarkeInstitutionen",
            label: "Datenerfassung (Frieden, Gerechtigkeit und starke Institutionen)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.friedenGerechtigkeitUndStarkeInstitutionen?.sdgFriedenGerechtigkeitUndStarkeInstitutionen ==
              "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungFriedenGerechtigkeitUndStarkeInstitutionen",
            label: "Daten Plausibilitätsprüfung (Frieden, Gerechtigkeit und starke Institutionen)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.friedenGerechtigkeitUndStarkeInstitutionen?.sdgFriedenGerechtigkeitUndStarkeInstitutionen ==
              "Yes",
          },
          {
            name: "datenquelleFriedenGerechtigkeitUndStarkeInstitutionen",
            label: "Datenquelle (Frieden, Gerechtigkeit und starke Institutionen)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.friedenGerechtigkeitUndStarkeInstitutionen?.sdgFriedenGerechtigkeitUndStarkeInstitutionen ==
              "Yes",
          },
        ],
      },
      {
        name: "partnerschaftenZurErreichungDerZiele",
        label: "Partnerschaften zur Erreichung der Ziele",
        fields: [
          {
            name: "sdgPartnerschaftenZurErreichungDerZiele",
            label: "SDG - Partnerschaften zur Erreichung der Ziele",
            description:
              'Kann mit der Methodik ein Beitrag zum SDG "Partnerschaften zur Erreichung der Ziele" gemessen werden?',

            component: "YesNoFormField",
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "wennNeinBitteBegruendenPartnerschaftenZurErreichungDerZiele",
            label: "Wenn Nein, bitte begründen (Partnerschaften zur Erreichung der Ziele)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.partnerschaftenZurErreichungDerZiele?.sdgPartnerschaftenZurErreichungDerZiele == "No",
          },
          {
            name: "verwendeteSchluesselzahlenPartnerschaftenZurErreichungDerZiele",
            label: "Verwendete Schlüsselzahlen (Partnerschaften zur Erreichung der Ziele)",
            description:
              "Welche Kennzahlen werden verwendet?\nAngaben zu den KPIs die zur Messung des SDGs verwendet werden.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.partnerschaftenZurErreichungDerZiele?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
          },
          {
            name: "datenerfassungPartnerschaftenZurErreichungDerZiele",
            label: "Datenerfassung (Partnerschaften zur Erreichung der Ziele)",
            description:
              "Wie erfolgt die Datenerhebung?\nAngaben zu dem Vorgang bei der Datenerhebung (z.B. Fragebogen, Interviews, Nutzung der Daten aus Geschäftsberichten)",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.partnerschaftenZurErreichungDerZiele?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
          },
          {
            name: "datenPlausibilitaetspruefungPartnerschaftenZurErreichungDerZiele",
            label: "Daten Plausibilitätsprüfung (Partnerschaften zur Erreichung der Ziele)",
            description:
              "Wie erfolgt die Datenplausibilisierung?\nErkennung von Ausreißern (z.B. mithilfe von Benchmarking), Maßnahmen zur Erkennung von unplausiblen Daten z.B. numerische Daten werden verlangt und Text wurde eingetragen",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.partnerschaftenZurErreichungDerZiele?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
          },
          {
            name: "datenquellePartnerschaftenZurErreichungDerZiele",
            label: "Datenquelle (Partnerschaften zur Erreichung der Ziele)",
            description:
              "Welche Quellen werden genutzt?\nAngabe von Quellen, zum Beispiel Geschäftsberichte von Unternehmen, Nachhaltigkeitsberichte etc.",

            component: "InputTextFormField",
            required: false,
            showIf: (dataset: HeimathafenData): boolean =>
              dataset.sdg?.partnerschaftenZurErreichungDerZiele?.sdgPartnerschaftenZurErreichungDerZiele == "Yes",
          },
        ],
      },
    ],
  },
] as Category[];
