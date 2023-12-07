import { DEFAULT_PROBABILITY } from "@e2e/utils/FakeFixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type GdvData } from "@clients/backend";
import { GdvGenerator } from "@e2e/fixtures/frameworks/gdv/GdvGenerator";
import { pickSubsetOfElements } from "@e2e/fixtures/FixtureUtils";
import { pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { ArtDesAuditsOptions } from "@clients/backend";
import { Activity } from "@clients/backend";
import { TaxonomieBerichterstattungOptions } from "@clients/backend";
import { StatusZuGOptions } from "@clients/backend";
import { StatusZuSOptions } from "@clients/backend";
import { StatusZuEOptions } from "@clients/backend";
import { AnreizmechanismenFuerDasManagementSozialesOptions } from "@clients/backend";
import { AnreizmechanismenFuerDasManagementUmweltOptions } from "@clients/backend";
import { generateArray } from "@e2e/fixtures/FixtureUtils";
import { FrequenzDerBerichterstattungOptions } from "@clients/backend";

/**
 * Generates a set number of gdv fixtures
 * @param numFixtures the number of gdv fixtures to generate
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a set number of gdv fixtures
 */
export function generateGdvFixtures(
  numFixtures: number,
  nullProbability = DEFAULT_PROBABILITY,
): FixtureData<GdvData>[] {
  return generateFixtureDataset<GdvData>(() => generateGdvData(nullProbability), numFixtures);
}

/**
 * Generates a random gdv dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random gdv dataset
 */
export function generateGdvData(nullProbability = DEFAULT_PROBABILITY): GdvData {
  const dataGenerator = new GdvGenerator(nullProbability);
  return {
    general: {
      masterData: {
        berichtsPflicht: dataGenerator.randomYesNo(),
        gueltigkeitsDatum: dataGenerator.randomFutureDate(),
      },
    },
    allgemein: {
      esgZiele: dataGenerator.randomYesNo(),
      ziele: dataGenerator.randomShortString(),
      investitionen: dataGenerator.randomShortString(),
      sektorMitHohenKlimaauswirkungen: dataGenerator.randomYesNo(),
      sektor: dataGenerator.valueOrNull(pickSubsetOfElements(["A", "B", "C", "D", "E", "F", "G", "H", "L"])),
      nachhaltigkeitsbericht: dataGenerator.randomYesNo(),
      frequenzDerBerichterstattung: dataGenerator.valueOrNull(
        pickOneElement(Object.values(FrequenzDerBerichterstattungOptions)),
      ),
      iso14001: dataGenerator.randomBaseDataPoint(dataGenerator.guaranteedYesNo()),
      iso45001: dataGenerator.randomBaseDataPoint(dataGenerator.guaranteedYesNo()),
      iso27001: dataGenerator.randomBaseDataPoint(dataGenerator.guaranteedYesNo()),
      iso50001: dataGenerator.randomBaseDataPoint(dataGenerator.guaranteedYesNo()),
      weitereAkkreditierungen: dataGenerator.valueOrNull(
        generateArray(() => dataGenerator.guaranteedBaseDataPoint(dataGenerator.guaranteedShortString()), 1, 5, 0),
      ),
      mechanismenZurUeberwachungDerEinhaltungUnGlobalCompactPrinzipienUndOderOecdLeitsaetze:
        dataGenerator.randomYesNo(),
      erklaerungUngc: dataGenerator.randomShortString(),
      oecdLeitsaetze: dataGenerator.randomYesNo(),
      erklaerungOecd: dataGenerator.randomShortString(),
      ausrichtungAufDieUnSdgsUndAktivesVerfolgen: dataGenerator.randomShortString(),
      ausschlusslistenAufBasisVonEsgKriterien: dataGenerator.randomYesNo(),
      ausschlusslisten: dataGenerator.randomShortString(),
      oekologischeSozialeFuehrungsstandardsOderPrinzipien: dataGenerator.randomYesNo(),
      anreizmechanismenFuerDasManagementUmwelt: dataGenerator.valueOrNull(
        pickOneElement(Object.values(AnreizmechanismenFuerDasManagementUmweltOptions)),
      ),
      anreizmechanismenFuerDasManagementSoziales: dataGenerator.valueOrNull(
        pickOneElement(Object.values(AnreizmechanismenFuerDasManagementSozialesOptions)),
      ),
      esgBezogeneRechtsstreitigkeiten: dataGenerator.randomYesNo(),
      rechtsstreitigkeitenMitBezugZuE: dataGenerator.randomYesNo(),
      statusZuE: dataGenerator.valueOrNull(pickOneElement(Object.values(StatusZuEOptions))),
      einzelheitenZuDenRechtsstreitigkeitenZuE: dataGenerator.randomShortString(),
      rechtsstreitigkeitenMitBezugZuS: dataGenerator.randomYesNo(),
      statusZuS: dataGenerator.valueOrNull(pickOneElement(Object.values(StatusZuSOptions))),
      einzelheitenZuDenRechtsstreitigkeitenZuS: dataGenerator.randomShortString(),
      rechtsstreitigkeitenMitBezugZuG: dataGenerator.randomYesNo(),
      statusZuG: dataGenerator.valueOrNull(pickOneElement(Object.values(StatusZuGOptions))),
      einzelheitenZuDenRechtsstreitigkeitenZuG: dataGenerator.randomShortString(),
      esgRating: dataGenerator.randomYesNo(),
      agentur: dataGenerator.randomShortString(),
      ergebnis: dataGenerator.randomBaseDataPoint(dataGenerator.guaranteedShortString()),
      kritischePunkte: dataGenerator.randomShortString(),
      nachhaltigkeitsbezogenenAnleihen: dataGenerator.randomYesNo(),
      wichtigsteESUndGRisikenUndBewertung: dataGenerator.randomShortString(),
      hindernisseBeimUmgangMitEsgBedenken: dataGenerator.randomShortString(),
    },
    umwelt: {
      treibhausgasemissionen: {
        treibhausgasEmissionsintensitaetDerUnternehmenInDieInvestriertWird: dataGenerator.randomShortString(),
        strategieUndZieleZurReduzierungVonTreibhausgasEmissionen: dataGenerator.randomShortString(),
      },
      produkteZurVerringerungDerUmweltbelastung: dataGenerator.randomYesNo(),
      verringerungenDerUmweltbelastung: dataGenerator.randomShortString(),
      oekologischerMindestStandardFuerProduktionsprozesse: dataGenerator.randomYesNo(),
      energieverbrauch: {
        unternehmensGruppenStrategieBzglEnergieverbrauch: dataGenerator.randomShortString(),
      },
      energieeffizienzImmobilienanlagen: {
        unternehmensGruppenStrategieBzglEnergieeffizientenImmobilienanlagen: dataGenerator.randomShortString(),
      },
      wasserverbrauch: {
        unternehmensGruppenStrategieBzglWasserverbrauch: dataGenerator.randomShortString(),
      },
      abfallproduktion: {
        unternehmensGruppenStrategieBzglAbfallproduktion: dataGenerator.randomShortString(),
      },
      gefaehrlicheAbfaelle: {
        gefaehrlicherAbfall: dataGenerator.randomShortString(),
      },
      biodiversitaet: {
        negativeAktivitaetenFuerDieBiologischeVielfalt: dataGenerator.randomYesNo(),
        negativeMassnahmenFuerDieBiologischeVielfalt: dataGenerator.randomShortString(),
        positiveAktivitaetenFuerDieBiologischeVielfalt: dataGenerator.randomYesNo(),
        positiveMassnahmenFuerDieBiologischeVielfalt: dataGenerator.randomShortString(),
      },
      fossileBrennstoffe: {
        einnahmenAusFossilenBrennstoffen: dataGenerator.randomYesNo(),
      },
      taxonomie: {
        taxonomieBerichterstattung: dataGenerator.valueOrNull(
          pickOneElement(Object.values(TaxonomieBerichterstattungOptions)),
        ),
        euTaxonomieKompassAktivitaeten: dataGenerator.valueOrNull(pickSubsetOfElements(Object.values(Activity))),
      },
    },
    soziales: {
      aenderungenUnternehmensstruktur: dataGenerator.randomYesNo(),
      sicherheitsmassnahmenFuerMitarbeiter: dataGenerator.randomShortString(),
      einkommensgleichheit: {
        massnahmenZurVerbesserungDerEinkommensungleichheit: dataGenerator.randomShortString(),
      },
      geschlechterdiversitaet: {
        definitionTopManagement: dataGenerator.randomShortString(),
        einhaltungRechtlicherVorgaben: dataGenerator.randomShortString(),
      },
      audit: {
        auditsZurEinhaltungVonArbeitsstandards: dataGenerator.randomYesNo(),
        artDesAudits: dataGenerator.valueOrNull(pickOneElement(Object.values(ArtDesAuditsOptions))),
        auditErgebnisse: dataGenerator.randomShortString(),
      },
    },
    unternehmensfuehrungGovernance: {
      wirtschaftspruefer: dataGenerator.randomShortString(),
      ceoVorsitzender: dataGenerator.randomYesNo(),
      amtszeit: dataGenerator.randomShortString(),
      einbeziehungVonStakeholdern: dataGenerator.randomYesNo(),
      prozessDerEinbeziehungVonStakeholdern: dataGenerator.randomShortString(),
      mechanismenZurAusrichtungAufStakeholder: dataGenerator.randomShortString(),
      veroeffentlichteUnternehmensrichtlinien: dataGenerator.valueOrNull(
        pickSubsetOfElements([
          "Anti-Korruption",
          "Verhaltenskodex",
          "Interessenkonflikte",
          "Datenschutz",
          "Diversit\u00E4t & Inklusion",
          "Faire Behandlung von Kunden",
          "Zwangsarbeit",
          "Gesundheit und Sicherheit",
          "Mgt von Umweltgefahren",
          "Verantwortungsvolles Marketing",
          "Whistleblowing",
          "other",
        ]),
      ),
      esgKriterienUndUeberwachungDerLieferanten: dataGenerator.randomYesNo(),
      auswahlkriterien: dataGenerator.randomShortString(),
    },
  };
}
