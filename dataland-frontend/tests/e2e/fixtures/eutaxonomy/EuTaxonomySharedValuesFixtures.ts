import { type EuTaxonomyDataForFinancials, type EuTaxonomyGeneral } from "@clients/backend";
import { generateYesNoNa, generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { generateInt } from "@e2e/fixtures/common/NumberFixtures";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { DEFAULT_PROBABILITY, valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a new Eu Taxonomy instance fitting for either "financials" or "non-financials"
 * @param referencedReports the referenced reports to be used
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns Eu Taxonomy instance with common fields
 */
export function generateEuTaxonomyWithBaseFields(
  referencedReports: ReferencedDocuments,
  undefinedProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForFinancials | EuTaxonomyGeneral {
  return {
    fiscalYearDeviation: valueOrUndefined(generateFiscalYearDeviation(), undefinedProbability),
    fiscalYearEnd: valueOrUndefined(generatePastDate(), undefinedProbability),
    numberOfEmployees: valueOrUndefined(generateInt(100000), undefinedProbability),
    referencedReports: referencedReports,
    assurance: generateAssuranceData(referencedReports),
    scopeOfEntities: valueOrUndefined(generateYesNoNa(), undefinedProbability),
    nfrdMandatory: valueOrUndefined(generateYesNo(), undefinedProbability),
    euTaxonomyActivityLevelReporting: valueOrUndefined(generateYesNo(), undefinedProbability),
  };
}
