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
  /*
  if (newFixture.t.allgemein?.esgZiele?.existenzVonEsgZielen) {
    newFixture.t.allgemein.esgZiele.existenzVonEsgZielen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.sektoren?.sektorenMitHohenKlimaauswirkungen) {
    newFixture.t.allgemein.sektoren.sektorenMitHohenKlimaauswirkungen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgBerichte?.nachhaltigkeitsberichte) {
    newFixture.t.allgemein.esgBerichte.nachhaltigkeitsberichte = YesNo.Yes;
  } */ // TODO comment out changed fields
  if (newFixture.t.allgemein?.unGlobalCompactPrinzipien?.mechanismenZurUeberwachungDerEinhaltungDerUngcp) {
    newFixture.t.allgemein.unGlobalCompactPrinzipien.mechanismenZurUeberwachungDerEinhaltungDerUngcp = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.oecdLeitsaetze?.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze) {
    newFixture.t.allgemein.oecdLeitsaetze.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgRatingAndZertifizierung?.esgRating) {
    newFixture.t.allgemein.esgRatingAndZertifizierung.esgRating = YesNo.Yes;
  }
}

/**
 * Modifies the prepared fixtures
 * @param newFixture generated prepared fixture to modify
 */
function modifyPreparedFixturesPartTwo(newFixture: FixtureData<EsgQuestionnaireData>): void {
  if (newFixture.t.allgemein?.esgRatingAndZertifizierung?.ratingbericht) {
    newFixture.t.allgemein.esgRatingAndZertifizierung.ratingbericht.value = YesNo.Yes;
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
  if (newFixture.t.allgemein?.generelleEsgStrategie?.dokumenteZurNachhaltigkeitsstrategie) {
    newFixture.t.allgemein.generelleEsgStrategie.dokumenteZurNachhaltigkeitsstrategie.push({
      value: 'Report with null data source',
      dataSource: null,
    });
  }
  if (
    newFixture.t.soziales?.unternehmensstrukturaenderungen?.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur
  ) {
    newFixture.t.soziales.unternehmensstrukturaenderungen.vorhandenseinKuerzlicherAenderungenDerUnternehmensstruktur =
      YesNo.Yes;
  }
  if (newFixture.t.soziales?.audit) {
    newFixture.t.soziales.audit.auditsZurEinhaltungVonArbeitsstandards = YesNo.Yes;
  }
  if (newFixture.t.unternehmensfuehrungGovernance?.sonstige?.trennungVonCeoOderVorsitzenden) {
    newFixture.t.unternehmensfuehrungGovernance.sonstige.trennungVonCeoOderVorsitzenden = YesNo.Yes;
  }
  if (newFixture.t.unternehmensfuehrungGovernance?.stakeholder?.einbeziehungVonStakeholdern) {
    newFixture.t.unternehmensfuehrungGovernance.stakeholder.einbeziehungVonStakeholdern = YesNo.Yes;
  }
  if (newFixture.t.unternehmensfuehrungGovernance?.lieferantenauswahl?.esgKriterienUndUeberwachungDerLieferanten) {
    newFixture.t.unternehmensfuehrungGovernance.lieferantenauswahl.esgKriterienUndUeberwachungDerLieferanten =
      YesNo.Yes;
  }
}
