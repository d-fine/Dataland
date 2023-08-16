import { type EuTaxonomyDataForNonFinancials, type EuTaxonomyDetailsPerCashFlowType } from "@clients/backend";
import { type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateDatapoint, generateDatapointAbsoluteAndPercentage } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
 * @param reports a list of reports that can be referenced
 * @returns the generated data
 */
export function generateEuTaxonomyPerCashflowType(reports: ReferencedDocuments): EuTaxonomyDetailsPerCashFlowType {
  return {
    totalAmount: valueOrUndefined(generateDatapoint(valueOrUndefined(randomEuroValue()), reports)),
    alignedData: valueOrUndefined(
      generateDatapointAbsoluteAndPercentage(
        valueOrUndefined(randomEuroValue()),
        valueOrUndefined(randomPercentageValue()),
        reports,
      ),
    ),
    eligibleData: valueOrUndefined(
      generateDatapointAbsoluteAndPercentage(
        valueOrUndefined(randomEuroValue()),
        valueOrUndefined(randomPercentageValue()),
        reports,
      ),
    ),
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
