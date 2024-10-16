import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EsgDatenkatalogData, YesNo } from '@clients/backend';
import { generateEsgDatenkatalogFixtures } from './EsgDatenkatalogDataFixtures';

/**
 * Generates esg-datenkatalog prepared fixtures by generating random esg-datenkatalog datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEsgDatenkatalogPreparedFixtures(): Array<FixtureData<EsgDatenkatalogData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically
  preparedFixtures.push(generateFixtureForNoNullFieldsAndOnlyYesAnswers());
  return preparedFixtures;
}

/**
 * Generates an esg-datenkatalog fixture with no null values and all YesNo fields set to "Yes"
 * @returns the fixture
 */
function generateFixtureForNoNullFieldsAndOnlyYesAnswers(): FixtureData<EsgDatenkatalogData> {
  const newFixture = generateEsgDatenkatalogFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'EsgDatenkatalog-dataset-with-no-null-fields';

  setAllYesNoValuesToYesInCategoryAllgemein(newFixture);
  setAllYesNoValuesToYesInCategoriesUmwelSozialsGovernance(newFixture);
  return newFixture;
}

/**
 * Sets the values of all "Yes/No" questions in the categories "General" and "Allgemein" to value "Yes"
 * @param newFixture generated prepared fixture to modify
 */
function setAllYesNoValuesToYesInCategoryAllgemein(newFixture: FixtureData<EsgDatenkatalogData>): void {
  if (newFixture.t.allgemein?.generelleEsgStrategie?.dokumenteZurNachhaltigkeitsstrategie) {
    newFixture.t.allgemein.generelleEsgStrategie.dokumenteZurNachhaltigkeitsstrategie.push({
      value: 'Report with null data source',
      dataSource: null,
    });
  }
  if (newFixture.t.allgemein?.generelleEsgStrategie?.massnahmenZurErreichungDes15GradCelsiusZiels) {
    newFixture.t.allgemein.generelleEsgStrategie.massnahmenZurErreichungDes15GradCelsiusZiels = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.taxonomieKpisUndBestimmteAktivitaeten?.taxonomiebezogeneCapexPlanung) {
    newFixture.t.allgemein.taxonomieKpisUndBestimmteAktivitaeten.taxonomiebezogeneCapexPlanung = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.taxonomieKpisUndBestimmteAktivitaeten?.aktivitaetImSektorFossileBrennstoffe) {
    newFixture.t.allgemein.taxonomieKpisUndBestimmteAktivitaeten.aktivitaetImSektorFossileBrennstoffe = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.taxonomieKpisUndBestimmteAktivitaeten?.aktivitaetImSektorHerstellungVonChemikalien) {
    newFixture.t.allgemein.taxonomieKpisUndBestimmteAktivitaeten.aktivitaetImSektorHerstellungVonChemikalien =
      YesNo.Yes;
  }
  if (newFixture.t.allgemein?.taxonomieKpisUndBestimmteAktivitaeten?.aktivitaetImSektorUmstritteneWaffen) {
    newFixture.t.allgemein.taxonomieKpisUndBestimmteAktivitaeten.aktivitaetImSektorUmstritteneWaffen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.taxonomieKpisUndBestimmteAktivitaeten?.aktivitaetImSektorTabakanbauUndVerarbeitung) {
    newFixture.t.allgemein.taxonomieKpisUndBestimmteAktivitaeten.aktivitaetImSektorTabakanbauUndVerarbeitung =
      YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgRatingUndZertifizierung?.esgRating) {
    newFixture.t.allgemein.esgRatingUndZertifizierung.esgRating = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgRatingUndZertifizierung?.ratingbericht) {
    newFixture.t.allgemein.esgRatingUndZertifizierung.ratingbericht.value = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.unGlobalCompactPrinzipien?.mechanismenZurUeberwachungDerEinhaltungDerUngcp) {
    newFixture.t.allgemein.unGlobalCompactPrinzipien.mechanismenZurUeberwachungDerEinhaltungDerUngcp = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.oecdLeitsaetze?.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze) {
    newFixture.t.allgemein.oecdLeitsaetze.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitEsgBezug) {
    newFixture.t.allgemein.rechtsstreitigkeiten.rechtsstreitigkeitenMitEsgBezug = YesNo.Yes;
  }
  if (
    newFixture.t.allgemein?.transaktionenMitNachhaltigkeitskomponenten
      ?.emissionGruenerSozialerUndOderNachhaltigerSchuldtitel
  ) {
    newFixture.t.allgemein.transaktionenMitNachhaltigkeitskomponenten.emissionGruenerSozialerUndOderNachhaltigerSchuldtitel =
      YesNo.Yes;
  }
  if (newFixture.t.allgemein?.transaktionenMitNachhaltigkeitskomponenten?.emissionVonSustainibilityLinkedDebt) {
    newFixture.t.allgemein.transaktionenMitNachhaltigkeitskomponenten.emissionVonSustainibilityLinkedDebt = YesNo.Yes;
  }
}

/**
 * Sets the values of all "Yes/No" questions in the categories "Umwelt", "Soziales" and "Governance" to value "Yes"
 * @param newFixture generated prepared fixture to modify
 */
function setAllYesNoValuesToYesInCategoriesUmwelSozialsGovernance(newFixture: FixtureData<EsgDatenkatalogData>): void {
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.messungSteuerungUndUeberwachungDerKlimaUndUmweltrisiken) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.messungSteuerungUndUeberwachungDerKlimaUndUmweltrisiken = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.nutzungVonSzenarioanalysen) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.nutzungVonSzenarioanalysen = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.beruecksichtigungVonKlimaUndUmweltrisiken) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.beruecksichtigungVonKlimaUndUmweltrisiken = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.produkteZurReduzierungDerUmweltbelastung) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.produkteZurReduzierungDerUmweltbelastung = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.kompensationsinstrumenteTreibhausgasemissionen) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.kompensationsinstrumenteTreibhausgasemissionen = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKreislaufwirtschaft?.abfallmanagementsystem) {
    newFixture.t.umwelt.risikenUndMassnahmenKreislaufwirtschaft.abfallmanagementsystem = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKreislaufwirtschaft?.geplanteErhoehungDesAnteilsVonRecyclaten) {
    newFixture.t.umwelt.risikenUndMassnahmenKreislaufwirtschaft.geplanteErhoehungDesAnteilsVonRecyclaten = YesNo.Yes;
  }
  if (
    newFixture.t.umwelt?.risikenUndMassnahmenBiodiversitaetUndOekosysteme
      ?.negativeAuswirkungenAufBiodiversitaetUndOekosystem
  ) {
    newFixture.t.umwelt.risikenUndMassnahmenBiodiversitaetUndOekosysteme.negativeAuswirkungenAufBiodiversitaetUndOekosystem =
      YesNo.Yes;
  }
  if (
    newFixture.t.umwelt?.risikenUndMassnahmenBiodiversitaetUndOekosysteme
      ?.positiveAuswirkungenAufBiodiversitaetUndOekosystem
  ) {
    newFixture.t.umwelt.risikenUndMassnahmenBiodiversitaetUndOekosysteme.positiveAuswirkungenAufBiodiversitaetUndOekosystem =
      YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenBiodiversitaetUndOekosysteme?.planZurReduktionDesWasserverbrauchs) {
    newFixture.t.umwelt.risikenUndMassnahmenBiodiversitaetUndOekosysteme.planZurReduktionDesWasserverbrauchs =
      YesNo.Yes;
  }
  if (newFixture.t.soziales?.risikenUndMassnahmen?.weitereWesentlicheSozialeRisiken) {
    newFixture.t.soziales.risikenUndMassnahmen.weitereWesentlicheSozialeRisiken = YesNo.Yes;
  }
  if (newFixture.t.governance?.vorstandsprofil?.kopplungVonVerguetungDesTopManagementsAnNachhaltigkeitsziele) {
    newFixture.t.governance.vorstandsprofil.kopplungVonVerguetungDesTopManagementsAnNachhaltigkeitsziele = YesNo.Yes;
  }
  if (newFixture.t.governance?.stakeholderdialog?.csrdKonformerProzessZurBeruecksichtigungDerStakeholderinteressen) {
    newFixture.t.governance.stakeholderdialog.csrdKonformerProzessZurBeruecksichtigungDerStakeholderinteressen =
      YesNo.Yes;
  }
}
