import { type EuTaxonomyDataForFinancials, type EuTaxonomyGeneral } from "@clients/backend";
import { generateYesNoNa, generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { generateInt } from "@e2e/fixtures/common/NumberFixtures";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { DEFAULT_PROBABILITY, valueOrMissing } from "@e2e/utils/FakeFixtureUtils";
import { type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a new Eu Taxonomy instance fitting for either "financials" or "non-financials"
 * @param referencedReports the referenced reports to be used
 * @param setMissingValuesToNull decides whether missing values are represented by "undefined" or "null"
 * @param missingValueProbability the probability (as number between 0 and 1) for missing values in optional fields
 * @returns Eu Taxonomy instance with common fields
 */
export function generateEuTaxonomyWithBaseFields(
  referencedReports: ReferencedDocuments,
  setMissingValuesToNull: boolean,
  missingValueProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForFinancials | EuTaxonomyGeneral {
  return {
    fiscalYearDeviation: valueOrMissing(generateFiscalYearDeviation(), missingValueProbability, setMissingValuesToNull),
    fiscalYearEnd: valueOrMissing(generatePastDate(), missingValueProbability, setMissingValuesToNull),
    numberOfEmployees: valueOrMissing(generateInt(100000), missingValueProbability, setMissingValuesToNull),
    referencedReports: referencedReports,
    assurance: generateAssuranceData(referencedReports, setMissingValuesToNull),
    scopeOfEntities: valueOrMissing(generateYesNoNa(), missingValueProbability, setMissingValuesToNull),
    nfrdMandatory: valueOrMissing(generateYesNo(), missingValueProbability, setMissingValuesToNull),
    euTaxonomyActivityLevelReporting: valueOrMissing(generateYesNo(), missingValueProbability, setMissingValuesToNull),
  };
}
