import { generateFixtureDataset, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { type EuTaxonomyDataForNonFinancials, type EuTaxonomyDetailsPerCashFlowType } from "@clients/backend";
import { generateDatapoint } from "@e2e/fixtures/common/DataPointFixtures";
import { randomFloat } from "@e2e/fixtures/common/NumberFixtures";
import {
  generateFinancialShare,
  generateEuTaxonomyDataForNonFinancials,
} from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";

type generatorFunction = (
  input: FixtureData<EuTaxonomyDataForNonFinancials>,
) => FixtureData<EuTaxonomyDataForNonFinancials>;

/**
 * Generates prepared fixtures for the eutaxonomy-non-financials framework
 * @returns the generated prepared fixtures
 */
export function generateEuTaxonomyForNonFinancialsPreparedFixtures(): Array<
  FixtureData<EuTaxonomyDataForNonFinancials>
> {
  const creationFunctions: Array<generatorFunction> = [
    createOnlyEglibileNumbers,
    createOnlyEligibleAndTotalNumbers,
    createDatasetWithoutReferencedReports,
  ];
  const fixtureBase = generateFixtureDataset<EuTaxonomyDataForNonFinancials>(
    generateEuTaxonomyDataForNonFinancials,
    creationFunctions.length,
  );
  const preparedFixtures = [];
  for (let i = 0; i < creationFunctions.length; i++) {
    preparedFixtures.push(creationFunctions[i](fixtureBase[i]));
  }
  return preparedFixtures;
}

/**
 * Creates a prepared fixture that only has eligible entries (no alignedPercentage/totalAmount)
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createOnlyEglibileNumbers(
  input: FixtureData<EuTaxonomyDataForNonFinancials>,
): FixtureData<EuTaxonomyDataForNonFinancials> {
  /**
   * Generates a details per cash flow object with only the total eligible share defined
   * @returns the details object
   */
  function generateCashFlowWithOnlyEligibleNumbers(): EuTaxonomyDetailsPerCashFlowType {
    const share = generateFinancialShare();
    share.absoluteShare = undefined;
    return { eligibleShare: share };
  }

  input.companyInformation.companyName = "only-eligible-numbers";
  input.t.opex = generateCashFlowWithOnlyEligibleNumbers();
  input.t.capex = generateCashFlowWithOnlyEligibleNumbers();
  input.t.revenue = generateCashFlowWithOnlyEligibleNumbers();
  return input;
}

/**
 * Creates a prepared fixture that only has eligible and total KPI entries (no alignedPercentage)
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createOnlyEligibleAndTotalNumbers(
  input: FixtureData<EuTaxonomyDataForNonFinancials>,
): FixtureData<EuTaxonomyDataForNonFinancials> {
  /**
   * Generates a details per cash flow object with only the total eligible share and total amount defined
   * @param referencedReports the ports to be referenced in a data point
   * @returns the details object
   */
  function generateCashFlowWithOnlyEligibleAndTotalNumbers(
    referencedReports: ReferencedDocuments,
  ): EuTaxonomyDetailsPerCashFlowType {
    return {
      totalAmount: generateDatapoint(randomFloat(1000000, 10000000000, 1), referencedReports),
      eligibleShare: generateFinancialShare(),
    };
  }

  input.companyInformation.companyName = "only-eligible-and-total-numbers";
  input.t.opex = generateCashFlowWithOnlyEligibleAndTotalNumbers(input.t.general!.referencedReports!);
  input.t.capex = generateCashFlowWithOnlyEligibleAndTotalNumbers(input.t.general!.referencedReports!);
  input.t.revenue = generateCashFlowWithOnlyEligibleAndTotalNumbers(input.t.general!.referencedReports!);
  return input;
}

/**
 * Creates a prepared fixture that has no referenced reports
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createDatasetWithoutReferencedReports(
  input: FixtureData<EuTaxonomyDataForNonFinancials>,
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "company_without_reports";
  input.t.general!.referencedReports = undefined;

  return input;
}
