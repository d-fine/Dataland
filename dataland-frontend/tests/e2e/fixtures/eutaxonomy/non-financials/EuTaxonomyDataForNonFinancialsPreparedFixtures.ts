import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateEuTaxonomyDataForNonFinancials } from "./EuTaxonomyDataForNonFinancialsFixtures";
import { generateDatapoint } from "@e2e/fixtures/common/DataPointFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";

type generatorFunction = (
  input: FixtureData<EuTaxonomyDataForNonFinancials>
) => FixtureData<EuTaxonomyDataForNonFinancials>;

export function generateEuTaxonomyForNonFinancialsPreparedFixtures(): Array<
  FixtureData<EuTaxonomyDataForNonFinancials>
> {
  const creationFunctions: Array<generatorFunction> = [createOnlyEglibileNumbers, createOnlyEligibleAndTotalNumbers];
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

function createOnlyEglibileNumbers(
  input: FixtureData<EuTaxonomyDataForNonFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "only-eligible-numbers";
  input.t.opex = {
    alignedPercentage: undefined,
    totalAmount: undefined,
    eligiblePercentage: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  input.t.capex = {
    alignedPercentage: undefined,
    totalAmount: undefined,
    eligiblePercentage: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  input.t.revenue = {
    alignedPercentage: undefined,
    totalAmount: undefined,
    eligiblePercentage: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  return input;
}

function createOnlyEligibleAndTotalNumbers(
  input: FixtureData<EuTaxonomyDataForNonFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "only-eligible-and-total-numbers";
  input.t.opex = {
    alignedPercentage: undefined,
    totalAmount: generateDatapoint(randomEuroValue(), input.t.referencedReports!),
    eligiblePercentage: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  input.t.capex = {
    alignedPercentage: undefined,
    totalAmount: generateDatapoint(randomEuroValue(), input.t.referencedReports!),
    eligiblePercentage: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  input.t.revenue = {
    alignedPercentage: undefined,
    totalAmount: generateDatapoint(randomEuroValue(), input.t.referencedReports!),
    eligiblePercentage: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  return input;
}
