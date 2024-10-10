import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EsgQuestionnaireData, YesNo } from '@clients/backend';
import { generateEsgQuestionnaireFixtures } from './EsgQuestionnaireDataFixtures';

/**
 * Generates esg-questionnaire prepared fixtures by generating random esg-questionnaire datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEsgQuestionnairePreparedFixtures(): Array<FixtureData<EsgQuestionnaireData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically
  preparedFixtures.push(generateFixtureForNoNullFieldsAndOnlyYesAnswers());
  return preparedFixtures;
}

/**
 * Generates an esg-questionnaire fixture with no null values and all YesNo fields set to "Yes"
 * @returns the fixture
 */
function generateFixtureForNoNullFieldsAndOnlyYesAnswers(): FixtureData<EsgQuestionnaireData> {
  const newFixture = generateEsgQuestionnaireFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'EsgQuestionnaire-dataset-with-no-null-fields';

  modifyPreparedFixturesPartOne(newFixture);
  modifyPreparedFixturesPartTwo(newFixture);
  return newFixture;
}

/**
 * Modifies the prepared fixtures
 * @param newFixture generated prepared fixture to modify
 */
function modifyPreparedFixturesPartOne(newFixture: FixtureData<EsgQuestionnaireData>): void {
  if (newFixture.t.general?.masterData) {
    newFixture.t.general.masterData.berichtspflichtUndEinwilligungZurVeroeffentlichung = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.taxonomieKpisUndBestimmteAktivitaeten?.taxonomiebezogeneCapexPlanungVorhanden) {
    newFixture.t.allgemein.taxonomieKpisUndBestimmteAktivitaeten.taxonomiebezogeneCapexPlanungVorhanden = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.taxonomieKpisUndBestimmteAktivitaeten?.aktivitaetImSektorFossileBrennstoffe) {
    newFixture.t.allgemein.taxonomieKpisUndBestimmteAktivitaeten.aktivitaetImSektorFossileBrennstoffe == YesNo.Yes;
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
}

/**
 * Modifies the prepared fixtures
 * @param newFixture generated prepared fixture to modify
 */
function modifyPreparedFixturesPartTwo(newFixture: FixtureData<EsgQuestionnaireData>): void {
  if (newFixture.t.allgemein?.rechtsstreitigkeiten?.esgBezogeneRechtsstreitigkeitenInvolvierung) {
    newFixture.t.allgemein.rechtsstreitigkeiten.esgBezogeneRechtsstreitigkeitenInvolvierung = YesNo.Yes;
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
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.messungSteuerungUndUeberwachungDerKlimaUndUmweltrisiken) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.messungSteuerungUndUeberwachungDerKlimaUndUmweltrisiken = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.nutzungVonSzenarioanalysen) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.nutzungVonSzenarioanalysen = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.risikenUndMassnahmenKlima?.beruecksichtigungVonKlimaUndUmweltrisiken) {
    newFixture.t.umwelt.risikenUndMassnahmenKlima.beruecksichtigungVonKlimaUndUmweltrisiken = YesNo.Yes;
  }

  // TODO wip

  if (newFixture.t.allgemein?.generelleEsgStrategie?.dokumenteZurNachhaltigkeitsstrategie) {
    newFixture.t.allgemein.generelleEsgStrategie.dokumenteZurNachhaltigkeitsstrategie.push({
      value: 'Report with null data source',
      dataSource: null,
    });
  }
  // TODO at the end we need to make sure that all yes-no-questions that are depended on are set to "Yes" to enforce a
  //  TODO full dataset and upload page in the blanket test
}
