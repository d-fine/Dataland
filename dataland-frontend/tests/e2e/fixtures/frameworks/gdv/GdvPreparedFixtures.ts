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

  if (newFixture.t.general?.masterData) {
    newFixture.t.general.masterData.berichtsPflicht = YesNo.Yes;
  }

  if (newFixture.t.allgemein?.esgZiele?.existenzVonEsgZielen) {
    newFixture.t.allgemein.esgZiele.existenzVonEsgZielen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.sektoren?.sektorenMitHohenKlimaauswirkungen) {
    newFixture.t.allgemein.sektoren.sektorenMitHohenKlimaauswirkungen = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgBerichte?.nachhaltigkeitsbericht) {
    newFixture.t.allgemein.esgBerichte.nachhaltigkeitsbericht = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.unGlobalConceptPrinzipien?.mechanismenZurUeberwachungDerEinhaltungDerUnGlobalCompactPrinzipien) {
    newFixture.t.allgemein.unGlobalConceptPrinzipien.mechanismenZurUeberwachungDerEinhaltungDerUnGlobalCompactPrinzipien =
      YesNo.Yes;
  }
  if (newFixture.t.allgemein?.oecdLeitsaetze?.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze) {
    newFixture.t.allgemein.oecdLeitsaetze.mechanismenZurUeberwachungDerEinhaltungDerOecdLeitsaetze = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.ausschlusslistenAufBasisVonEsgKriterien) {
    newFixture.t.allgemein.ausschlusslistenAufBasisVonEsgKriterien = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.oekologischeSozialeFuehrungsstandardsOderPrinzipien) {
    newFixture.t.allgemein.oekologischeSozialeFuehrungsstandardsOderPrinzipien = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgBezogeneRechtsstreitigkeiten) {
    newFixture.t.allgemein.esgBezogeneRechtsstreitigkeiten = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeitenMitBezugZuE) {
    newFixture.t.allgemein.rechtsstreitigkeitenMitBezugZuE = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeitenMitBezugZuS) {
    newFixture.t.allgemein.rechtsstreitigkeitenMitBezugZuS = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.rechtsstreitigkeitenMitBezugZuG) {
    newFixture.t.allgemein.rechtsstreitigkeitenMitBezugZuG = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.esgRating) {
    newFixture.t.allgemein.esgRating = YesNo.Yes;
  }
  if (newFixture.t.allgemein?.nachhaltigkeitsbezogenenAnleihen) {
    newFixture.t.allgemein.nachhaltigkeitsbezogenenAnleihen = YesNo.Yes;
  }

  if (newFixture.t.umwelt?.produkteZurVerringerungDerUmweltbelastung) {
    newFixture.t.umwelt.produkteZurVerringerungDerUmweltbelastung = YesNo.Yes;
  }
  if (newFixture.t.umwelt?.oekologischerMindestStandardFuerProduktionsprozesse) {
    newFixture.t.umwelt.oekologischerMindestStandardFuerProduktionsprozesse = YesNo.Yes;
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

  if (newFixture.t.soziales) {
    newFixture.t.soziales.aenderungenUnternehmensstruktur = YesNo.Yes;
  }

  if (newFixture.t.soziales?.audit) {
    newFixture.t.soziales.audit.auditsZurEinhaltungVonArbeitsstandards = YesNo.Yes;
  }

  if (newFixture.t.unternehmensfuehrungGovernance?.ceoVorsitzender) {
    newFixture.t.unternehmensfuehrungGovernance.ceoVorsitzender = YesNo.Yes;
  }
  if (newFixture.t.unternehmensfuehrungGovernance?.einbeziehungVonStakeholdern) {
    newFixture.t.unternehmensfuehrungGovernance.einbeziehungVonStakeholdern = YesNo.Yes;
  }
  if (newFixture.t.unternehmensfuehrungGovernance?.esgKriterienUndUeberwachungDerLieferanten) {
    newFixture.t.unternehmensfuehrungGovernance.esgKriterienUndUeberwachungDerLieferanten = YesNo.Yes;
  }

  return newFixture;
}
