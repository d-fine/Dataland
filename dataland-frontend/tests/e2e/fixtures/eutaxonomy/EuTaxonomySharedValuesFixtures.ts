import { type EuTaxonomyDataForFinancials, type EuTaxonomyGeneral } from "@clients/backend";
import { generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import { randomYesNoNa, randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";
import { randomInt } from "@e2e/fixtures/common/NumberFixtures";
import { randomFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a new Eu Taxonomy base fields, which fit for either "financials" or "non-financials"
 * @param undefinedProbabilityOfFields the probability of an undefined value per field
 * @returns Eu Taxonomy data object with common fields
 */
export function generateEuTaxonomyWithBaseFields(
  undefinedProbabilityOfFields?: number,
): EuTaxonomyDataForFinancials | EuTaxonomyGeneral {
  const referencedReports = generateReferencedReports();
  return {
    fiscalYearDeviation: valueOrUndefined(randomFiscalYearDeviation(), undefinedProbabilityOfFields),
    fiscalYearEnd: valueOrUndefined(randomPastDate(), undefinedProbabilityOfFields),
    numberOfEmployees: valueOrUndefined(randomInt(100000), undefinedProbabilityOfFields),
    referencedReports: referencedReports,
    assurance: generateAssuranceData(referencedReports),
    scopeOfEntities: valueOrUndefined(randomYesNoNa(), undefinedProbabilityOfFields),
    nfrdMandatory: valueOrUndefined(randomYesNo(), undefinedProbabilityOfFields),
    euTaxonomyActivityLevelReporting: valueOrUndefined(randomYesNo(), undefinedProbabilityOfFields),
  };
}
