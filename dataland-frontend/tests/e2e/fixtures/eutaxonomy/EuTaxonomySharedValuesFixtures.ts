import { type EuTaxonomyDataForFinancials, type EuTaxonomyGeneral } from "@clients/backend";
import { generateYesNoNa, generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { generateInt } from "@e2e/fixtures/common/NumberFixtures";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { DEFAULT_PROBABILITY, valueOrNull } from "@e2e/utils/FakeFixtureUtils";
import { type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a new Eu Taxonomy instance fitting for either "financials" or "non-financials"
 * @param referencedReports the referenced reports to be used
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns Eu Taxonomy instance with common fields
 */
export function generateEuTaxonomyWithBaseFields(
  referencedReports: ReferencedDocuments,
  nullProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForFinancials | EuTaxonomyGeneral {
  return {
    fiscalYearDeviation: valueOrNull(generateFiscalYearDeviation(), nullProbability),
    fiscalYearEnd: valueOrNull(generatePastDate(), nullProbability),
    numberOfEmployees: valueOrNull(generateInt(100000), nullProbability),
    referencedReports: referencedReports,
    assurance: generateAssuranceData(referencedReports),
    scopeOfEntities: valueOrNull(generateYesNoNa(), nullProbability),
    nfrdMandatory: valueOrNull(generateYesNo(), nullProbability),
    euTaxonomyActivityLevelReporting: valueOrNull(generateYesNo(), nullProbability),
  };
}
