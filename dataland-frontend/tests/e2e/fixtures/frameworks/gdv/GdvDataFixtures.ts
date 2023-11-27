import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type GdvData } from "@clients/backend";

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
  return generateFixtureDataset<GdvData>(
    () => generateGdvData(nullProbability),
    numFixtures,
    
  );
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
            gueltigkeitsDatum: dataGenerator.guaranteedFutureDate(),
            berichtsPflicht: dataGenerator.randomYesNo(),
        },
    },
    allgemein: {
        esgZiele: dataGenerator.randomYesNo(),
        ziele: dataGenerator.randomShortString(),
        investitionen: dataGenerator.randomShortString(),
        sektorMitHohenKlimaauswirkungen: dataGenerator.randomYesNo(),
        nachhaltigkeitsbericht: dataGenerator.randomYesNo(),
        frequenzDerBerichterstattung: dataGenerator.randomShortString(),
        mechanismenZurUeberwachungDerEinhaltungUnGlobalCompactPrinzipienUndOderOecdLeitsaetze: dataGenerator.randomYesNo(),
        uncgPrinzipien: dataGenerator.randomYesNo(),
        erklaerungUngc: dataGenerator.randomShortString(),
        oecdLeitsaetze: dataGenerator.randomYesNo(),
        erklaerungOecd: dataGenerator.randomShortString(),
        ausrichtungAufDieUnSdgsUndAktivesVerfolgen: dataGenerator.randomShortString(),
        ausschlusslistenAufBasisVonEsgKriterien: dataGenerator.randomYesNo(),
        ausschlusslisten: dataGenerator.randomShortString(),
        oekologischeSozialeFuehrungsstandardsOderPrinzipien: dataGenerator.randomYesNo(),
        anreizmechanismenFuerDasManagementUmwelt: dataGenerator.randomShortString(),
        anreizmechanismenFuerDasManagementSoziales: dataGenerator.randomShortString(),
        esgBezogeneRechtsstreitigkeiten: dataGenerator.randomYesNo(),
        rechtsstreitigkeitenMitBezugZuE: dataGenerator.randomYesNo(),
        statusE: dataGenerator.randomShortString(),
        einzelheitenZuDenRechtsstreitigkeitenE: dataGenerator.randomShortString(),
        rechtsstreitigkeitenMitBezugZuS: dataGenerator.randomYesNo(),
        statusS: dataGenerator.randomShortString(),
        einzelheitenZuDenRechtsstreitigkeitenS: dataGenerator.randomShortString(),
        rechtsstreitigkeitenMitBezugZuG: dataGenerator.randomYesNo(),
        statusG: dataGenerator.randomShortString(),
        einzelheitenZuDenRechtsstreitigkeitenG: dataGenerator.randomShortString(),
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
            taxonomieBerichterstattung: dataGenerator.randomYesNo(),
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
            artDesAudits: dataGenerator.randomShortString(),
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
        esgKriterienUndUeberwachungDerLieferanten: dataGenerator.randomYesNo(),
        auswahlkriterien: dataGenerator.randomShortString(),
    },
}
}

export class GdvGenerator extends Generator {

}
