import {
  Activity,
  type EuTaxonomyActivity,
  type EuTaxonomyAlignedActivity,
  type RelativeAndAbsoluteFinancialShare,
  type AmountWithCurrency,
  type EuTaxonomyDataForNonFinancials,
  type EuTaxonomyDetailsPerCashFlowType,
  YesNo,
} from "@clients/backend";
import {
  generateArray,
  getRandomNumberOfDistinctElementsFromArray,
  type ReferencedDocuments,
} from "@e2e/fixtures/FixtureUtils";
import { generateDatapointWithUnit } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomEuroValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { faker } from "@faker-js/faker";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

/**
 * Generates a random percentage between 0 and 100
 * @returns a reandom percentage
 */
function generatePercentage(): number {
  return faker.number.float({ min: 0, max: 100 });
}

/**
 * Generates a random amount of money
 * @returns an amount of money
 */
export function generateAmountWithCurrency(): AmountWithCurrency {
  return {
    amount: valueOrUndefined(randomEuroValue()),
    currency: valueOrUndefined(generateIso4217CurrencyCode()),
  };
}

/**
 * Generates a random financial share
 * @returns a financial share
 */
export function generateFinancialShare(): RelativeAndAbsoluteFinancialShare {
  return {
    relativeShareInPercent: valueOrUndefined(generatePercentage()),
    absoluteShare: valueOrUndefined(generateAmountWithCurrency()),
  };
}

/**
 * Generates a random activity
 * @returns a random activity
 */
function generateActivity(): EuTaxonomyActivity {
  return {
    activityName: faker.helpers.arrayElement(Object.values(Activity)),
    naceCodes: valueOrUndefined(generateListOfNaceCodes()),
    share: valueOrUndefined(generateFinancialShare()),
  };
}

/**
 * Generates a random aligned activity
 * @returns a random aligned activity
 */
function generateAlignedActivity(): EuTaxonomyAlignedActivity {
  return {
    ...generateActivity(),
    substantialContributionToClimateChangeMitigation: valueOrUndefined(generatePercentage()),
    substantialContributionToClimateChangeAdaption: valueOrUndefined(generatePercentage()),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      generatePercentage(),
    ),
    substantialContributionToTransitionToACircularEconomy: valueOrUndefined(generatePercentage()),
    substantialContributionToPollutionPreventionAndControl: valueOrUndefined(generatePercentage()),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      generatePercentage(),
    ),
    dnshToClimateChangeMitigation: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
    dnshToClimateChangeAdaption: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
    dnshToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      faker.helpers.arrayElement(Object.values(YesNo)),
    ),
    dnshToTransitionToACircularEconomy: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
    dnshToPollutionPreventionAndControl: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
    dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      faker.helpers.arrayElement(Object.values(YesNo)),
    ),
    minimumSafeguards: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
  };
}

/**
 * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
 * @param reports a list of reports that can be referenced
 * @returns the generated data
 */
export function generateEuTaxonomyPerCashflowType(reports: ReferencedDocuments): EuTaxonomyDetailsPerCashFlowType {
  return {
    totalAmount: valueOrUndefined(
      generateDatapointWithUnit(randomEuroValue(0, 10000000000), faker.finance.currencyCode(), reports),
    ),
    nonEligibleShare: valueOrUndefined(generateFinancialShare()),
    eligibleShare: valueOrUndefined(generateFinancialShare()),
    nonAlignedShare: valueOrUndefined(generateFinancialShare()),
    nonAlignedActivities: valueOrUndefined(generateArray(generateActivity)),
    alignedShare: valueOrUndefined(generateFinancialShare()),
    substantialContributionToClimateChangeMitigation: valueOrUndefined(generatePercentage()),
    substantialContributionToClimateChangeAdaption: valueOrUndefined(generatePercentage()),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      generatePercentage(),
    ),
    substantialContributionToTransitionToACircularEconomy: valueOrUndefined(generatePercentage()),
    substantialContributionToPollutionPreventionAndControl: valueOrUndefined(generatePercentage()),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      generatePercentage(),
    ),
    alignedActivities: valueOrUndefined(generateArray(generateAlignedActivity)),
    enablingShare: valueOrUndefined(generatePercentage()),
    transitionalShare: valueOrUndefined(generatePercentage()),
  };
}

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @returns the generated fixture
 */
export function generateEuTaxonomyDataForNonFinancials(): EuTaxonomyDataForNonFinancials {
  const data: EuTaxonomyDataForNonFinancials = {};
  data.general = generateEuTaxonomyWithBaseFields();
  data.opex = generateEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  data.capex = generateEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  data.revenue = generateEuTaxonomyPerCashflowType(assertDefined(data.general.referencedReports));
  return data;
}
