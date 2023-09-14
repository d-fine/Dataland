import { type EuTaxonomyDataForFinancials, type EuTaxonomyGeneral } from "@clients/backend";
import { generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import { generateYesNoNa, generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { generateNumber } from "@e2e/fixtures/common/NumberFixtures";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a new Eu Taxonomy instance fitting for either "financials" or "non-financials"
 * @returns Eu Taxonomy instance with common fields
 */
export function generateEuTaxonomyWithBaseFields(): EuTaxonomyDataForFinancials | EuTaxonomyGeneral {
  const referencedReports = generateReferencedReports();
  return {
    fiscalYearDeviation: valueOrUndefined(generateFiscalYearDeviation()),
    fiscalYearEnd: valueOrUndefined(generatePastDate()),
    numberOfEmployees: valueOrUndefined(generateNumber(100000)),
    referencedReports: referencedReports,
    assurance: generateAssuranceData(referencedReports),
    scopeOfEntities: valueOrUndefined(generateYesNoNa()),
    nfrdMandatory: valueOrUndefined(generateYesNo()),
    euTaxonomyActivityLevelReporting: valueOrUndefined(generateYesNo()),
  };
}
