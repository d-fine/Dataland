import { EuTaxonomyDataForNonFinancials, EuTaxonomyDetailsPerCashFlowType } from "@clients/backend";
import { ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateDatapointOrNotReportedAtRandom } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";

/**
 * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
 * @param reports a list of reports that can be referenced
 * @returns the generated data
 */
export function generateEuTaxonomyPerCashflowType(reports: ReferencedDocuments): EuTaxonomyDetailsPerCashFlowType {
  const total = randomEuroValue();
  const eligiblePercentage = randomPercentageValue();
  const alignedPercentage = randomPercentageValue();

  return {
    totalAmount: generateDatapointOrNotReportedAtRandom(total, reports),
    alignedPercentage: generateDatapointOrNotReportedAtRandom(alignedPercentage, reports),
    eligiblePercentage: generateDatapointOrNotReportedAtRandom(eligiblePercentage, reports),
  };
}

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @returns the generated fixture
 */
export function generateEuTaxonomyDataForNonFinancials(): EuTaxonomyDataForNonFinancials {
  const returnBase: EuTaxonomyDataForNonFinancials = generateEuTaxonomyWithBaseFields();

  returnBase.opex = generateEuTaxonomyPerCashflowType(assertDefined(returnBase.referencedReports));
  returnBase.capex = generateEuTaxonomyPerCashflowType(assertDefined(returnBase.referencedReports));
  returnBase.revenue = generateEuTaxonomyPerCashflowType(assertDefined(returnBase.referencedReports));

  return returnBase;
}
