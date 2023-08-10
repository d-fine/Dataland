import {generateFixtureDataset, ReferencedDocuments} from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import {EuTaxonomyDataForNonFinancials, EuTaxonomyDetailsPerCashFlowType} from "@clients/backend";
import {generateEuTaxonomyDataForNonFinancials, generateFinancialShare} from "./EuTaxonomyDataForNonFinancialsFixtures";
import { generateDatapoint, generateDatapointAbsoluteAndPercentage } from "@e2e/fixtures/common/DataPointFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import {generate} from "@faker-js/faker/modules/internet/user-agent";

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
  function generateCashFlowWithOnlyEligibleNumbers(): EuTaxonomyDetailsPerCashFlowType {
    let share = generateFinancialShare()
    share.absoluteShare = undefined
    return { totalEligibleShare: share }
  }

  input.companyInformation.companyName = "only-eligible-numbers";
  input.t.opex = generateCashFlowWithOnlyEligibleNumbers()
  input.t.capex = generateCashFlowWithOnlyEligibleNumbers()
  input.t.revenue = generateCashFlowWithOnlyEligibleNumbers()
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
  function generateCashFlowWithOnlyEligibleAndTotalNumbers(referencedReports: ReferencedDocuments): EuTaxonomyDetailsPerCashFlowType {
    return {
      totalAmount: generateDatapoint(randomEuroValue(), referencedReports),
      totalEligibleShare: generateFinancialShare()
    }
  }

  input.companyInformation.companyName = "only-eligible-and-total-numbers";
  input.t.opex = generateCashFlowWithOnlyEligibleAndTotalNumbers(input.t.general!.referencedReports!)
  input.t.capex = generateCashFlowWithOnlyEligibleAndTotalNumbers(input.t.general!.referencedReports!)
  input.t.revenue = generateCashFlowWithOnlyEligibleAndTotalNumbers(input.t.general!.referencedReports!)
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
