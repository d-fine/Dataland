import { generateFixtureDataset, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { type NewEuTaxonomyDataForNonFinancials, type NewEuTaxonomyDetailsPerCashFlowType } from "@clients/backend";
import { generateDatapoint } from "@e2e/fixtures/common/DataPointFixtures";
import { randomEuroValue } from "@e2e/fixtures/common/NumberFixtures";
import {
  generateFinancialShare,
  generateNewEuTaxonomyDataForNonFinancials,
} from "@e2e/fixtures/eutaxonomy/new-non-financials/NewEuTaxonomyDataForNonFinancialsFixtures";

type generatorFunction = (
  input: FixtureData<NewEuTaxonomyDataForNonFinancials>,
) => FixtureData<NewEuTaxonomyDataForNonFinancials>;

/**
 * Generates prepared fixtures for the eutaxonomy-non-financials framework
 * @returns the generated prepared fixtures
 */
export function generateNewEuTaxonomyForNonFinancialsPreparedFixtures(): Array<
  FixtureData<NewEuTaxonomyDataForNonFinancials>
> {
  const creationFunctions: Array<generatorFunction> = [
    createOnlyEglibileNumbers,
    createOnlyEligibleAndTotalNumbers,
    createDatasetWithoutReferencedReports,
  ];
  const fixtureBase = generateFixtureDataset<NewEuTaxonomyDataForNonFinancials>(
    generateNewEuTaxonomyDataForNonFinancials,
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
  input: FixtureData<NewEuTaxonomyDataForNonFinancials>,
): FixtureData<NewEuTaxonomyDataForNonFinancials> {
  /**
   * Generates a details per cash flow object with only the total eligible share defined
   * @returns the details object
   */
  function generateCashFlowWithOnlyEligibleNumbers(): NewEuTaxonomyDetailsPerCashFlowType {
    const share = generateFinancialShare();
    share.absoluteShare = undefined;
    return { totalEligibleShare: share };
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
  input: FixtureData<NewEuTaxonomyDataForNonFinancials>,
): FixtureData<NewEuTaxonomyDataForNonFinancials> {
  /**
   * Generates a details per cash flow object with only the total eligible share and total amount defined
   * @param referencedReports the ports to be referenced in a data point
   * @returns the details object
   */
  function generateCashFlowWithOnlyEligibleAndTotalNumbers(
    referencedReports: ReferencedDocuments,
  ): NewEuTaxonomyDetailsPerCashFlowType {
    return {
      totalAmount: generateDatapoint(randomEuroValue(), referencedReports),
      totalEligibleShare: generateFinancialShare(),
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
  input: FixtureData<NewEuTaxonomyDataForNonFinancials>,
): FixtureData<NewEuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "company_without_reports";
  input.t.general!.referencedReports = undefined;

  return input;
}
