import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateEuTaxonomyDataForNonFinancials } from "./EuTaxonomyDataForNonFinancialsFixtures";
import { generateDatapoint, generateDatapointAbsoluteAndPercentage } from "@e2e/fixtures/common/DataPointFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";

type generatorFunction = (
  input: FixtureData<EuTaxonomyDataForNonFinancials>
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
    creationFunctions.length
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
  input: FixtureData<EuTaxonomyDataForNonFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "only-eligible-numbers";
  input.t.opex = {
    alignedData: undefined,
    totalAmount: undefined,
    eligibleData: generateDatapointAbsoluteAndPercentage(randomPercentageValue(), null, input.t.referencedReports!),
  };
  input.t.capex = {
    alignedData: undefined,
    totalAmount: undefined,
    eligibleData: generateDatapointAbsoluteAndPercentage(randomPercentageValue(), null, input.t.referencedReports!),
  };
  input.t.revenue = {
    alignedData: undefined,
    totalAmount: undefined,
    eligibleData: generateDatapointAbsoluteAndPercentage(randomPercentageValue(), null, input.t.referencedReports!),
  };
  return input;
}

/**
 * Creates a prepared fixture that only has eligible and total KPI entries (no alignedPercentage)
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createOnlyEligibleAndTotalNumbers(
  input: FixtureData<EuTaxonomyDataForNonFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "only-eligible-and-total-numbers";
  input.t.opex = {
    alignedData: undefined,
    totalAmount: generateDatapoint(randomEuroValue(), input.t.referencedReports!),
    eligibleData: generateDatapointAbsoluteAndPercentage(
      randomEuroValue(),
      randomPercentageValue(),
      input.t.referencedReports!
    ),
  };
  input.t.capex = {
    alignedData: undefined,
    totalAmount: generateDatapoint(randomEuroValue(), input.t.referencedReports!),
    eligibleData: generateDatapointAbsoluteAndPercentage(
      randomEuroValue(),
      randomPercentageValue(),
      input.t.referencedReports!
    ),
  };
  input.t.revenue = {
    alignedData: undefined,
    totalAmount: generateDatapoint(randomEuroValue(), input.t.referencedReports!),
    eligibleData: generateDatapointAbsoluteAndPercentage(
      randomEuroValue(),
      randomPercentageValue(),
      input.t.referencedReports!
    ),
  };
  return input;
}

/**
 * Creates a prepared fixture that has no referenced reports
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createDatasetWithoutReferencedReports(
  input: FixtureData<EuTaxonomyDataForNonFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "company_without_reports";
  input.t.referencedReports = undefined;

  return input;
}
