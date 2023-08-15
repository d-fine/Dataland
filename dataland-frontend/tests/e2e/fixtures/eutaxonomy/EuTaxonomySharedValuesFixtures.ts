import {EuTaxonomyDataForFinancials, EuTaxonomyDataForNonFinancials, EuTaxonomyGeneral} from "@clients/backend";
import { generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import { randomYesNoNa, randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";
import { randomNumber } from "@e2e/fixtures/common/NumberFixtures";
import { randomFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a new Eu Taxonomy instance fitting for either "financials" or "non-financials"
 * @returns Eu Taxonomy instance with common fields
 */
export function generateEuTaxonomyWithBaseFields(): EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials | EuTaxonomyGeneral {
  const referencedReports = generateReferencedReports();
  return {
    fiscalYearDeviation: valueOrUndefined(randomFiscalYearDeviation()),
    fiscalYearEnd: valueOrUndefined(randomPastDate()),
    numberOfEmployees: valueOrUndefined(randomNumber(100000)),
    referencedReports: referencedReports,
    assurance: generateAssuranceData(referencedReports),
    scopeOfEntities: valueOrUndefined(randomYesNoNa()),
    nfrdMandatory: valueOrUndefined(randomYesNo()),
    euTaxonomyActivityLevelReporting: valueOrUndefined(randomYesNo()),
  };
}
