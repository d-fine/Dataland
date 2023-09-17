import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { type EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateEuTaxonomyDataForNonFinancials } from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";

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
  const creationFunctions: Array<generatorFunction> = [createDatasetThatHasAllFieldsDefined];
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
 * Creates a prepared fixture that has only defined fields and no undefined fields
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createDatasetThatHasAllFieldsDefined(
  input: FixtureData<EuTaxonomyDataForNonFinancials>,
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "all-fields-defined-for-eu-taxo-non-financials";
  input.t = generateEuTaxonomyDataForNonFinancials(0);
  return input;
}
