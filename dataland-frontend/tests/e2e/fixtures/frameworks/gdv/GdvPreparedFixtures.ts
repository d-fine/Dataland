import { type FixtureData } from "@sharedUtils/Fixtures";
import { type GdvData, YesNo } from "@clients/backend";
import { generateGdvFixtures } from "./GdvDataFixtures";

/**
 * Generates gdv prepared fixtures by generating random gdv datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateGdvPreparedFixtures(): Array<FixtureData<GdvData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically
  preparedFixtures.push(generateFixtureForNoNullFieldsAndOnlyYesAnswers());
  return preparedFixtures;
}

/**
 * Generates a gdv fixture with no null values and all YesNo fields set to "Yes"
 * @returns the fixture
 */
function generateFixtureForNoNullFieldsAndOnlyYesAnswers(): FixtureData<GdvData> {
  const newFixture = generateGdvFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = "Gdv-dataset-with-no-null-fields";

  modifyPreparedFixturesPartOne(newFixture);
  modifyPreparedFixturesPartTwo(newFixture);
  return newFixture;
}

/**
 * Modifies the prepared fixtures
 * @param newFixture generated prepared fixture to modify
 */
function modifyPreparedFixturesPartOne(newFixture: FixtureData<GdvData>): void {
  if (newFixture.t.general?.masterData) {
    newFixture.t.general.masterData.berichtsPflicht = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgZiele?.existenzVonEsgZielen) {
    newFixture.t.allgemein.esgZiele.existenzVonEsgZielen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.sektoren?.sektorenMitHohenKlimaauswirkungen) {
    newFixture.t.allgemein.sektoren.sektorenMitHohenKlimaauswirkungen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgBerichte?.nachhaltigkeitsberichte) {
    newFixture.t.allgemein.esgBerichte.nachhaltigkeitsberichte = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.unGlobalConceptPrinzipien?.mechanismenZurUeberwachungDerEinhaltungDerUngcp) {
    newFixture.t.allgemein.unGlobalConceptPrinzipien.mechanismenZurUeberwachungDerEinhaltungDerUngcp = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.oecdLeitsaetze?.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze) {
    newFixture.t.allgemein.oecdLeitsaetze.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.sonstige?.ausschlusslistenAufBasisVonEsgKriterien) {
    newFixture.t.allgemein.sonstige.ausschlusslistenAufBasisVonEsgKriterien = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.fuehrungsstandards?.oekologischeSozialeFuehrungsstandardsOderPrinzipien) {
    newFixture.t.allgemein.fuehrungsstandards.oekologischeSozialeFuehrungsstandardsOderPrinzipien = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeiten?.esgBezogeneRechtsstreitigkeiten) {
    newFixture.t.allgemein.rechtsstreitigkeiten.esgBezogeneRechtsstreitigkeiten = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuE) {
    newFixture.t.allgemein.rechtsstreitigkeiten.rechtsstreitigkeitenMitBezugZuE = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuS) {
    newFixture.t.allgemein.rechtsstreitigkeiten.rechtsstreitigkeitenMitBezugZuS = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeiten?.rechtsstreitigkeitenMitBezugZuG) {
    newFixture.t.allgemein.rechtsstreitigkeiten.rechtsstreitigkeitenMitBezugZuG = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rating?.esgRating) {
    newFixture.t.allgemein.rating.esgRating = YesNo.Yes;
  }
}

/**
 * Modifies the prepared fixtures
 * @param newFixture generated prepared fixture to modify
 */
function modifyPreparedFixturesPartTwo(newFixture: FixtureData<GdvData>): void {
  if (newFixture.t.allgemein?.rating?.ratingbericht) {
    newFixture.t.allgemein.rating.ratingbericht.value = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.anleihen?.grueneSozialeUndOderNachhaltigeEmissionen) {
    newFixture.t.allgemein.anleihen.grueneSozialeUndOderNachhaltigeEmissionen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.anleihen?.sustainibilityLinkedDebt) {
    newFixture.t.allgemein.anleihen.sustainibilityLinkedDebt = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.produktion?.produkteZurVerringerungDerUmweltbelastung) {
    newFixture.t.umwelt.produktion.produkteZurVerringerungDerUmweltbelastung = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.produktion?.oekologischerMindestStandardFuerProduktionsprozesse) {
    newFixture.t.umwelt.produktion.oekologischerMindestStandardFuerProduktionsprozesse = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.biodiversitaet?.negativeAktivitaetenFuerDieBiologischeVielfalt) {
    newFixture.t.umwelt.biodiversitaet.negativeAktivitaetenFuerDieBiologischeVielfalt = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.biodiversitaet?.positiveAktivitaetenFuerDieBiologischeVielfalt) {
    newFixture.t.umwelt.biodiversitaet.positiveAktivitaetenFuerDieBiologischeVielfalt = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.fossileBrennstoffe?.einnahmenAusFossilenBrennstoffen) {
    newFixture.t.umwelt.fossileBrennstoffe.einnahmenAusFossilenBrennstoffen = YesNo.Yes;
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
