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
import { generateArray, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateDatapoint } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { faker } from "@faker-js/faker";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

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
    relativeShareInPercent: valueOrUndefined(randomPercentageValue()),
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
    substantialContributionToClimateChangeMitigation: valueOrUndefined(randomPercentageValue()),
    substantialContributionToClimateChangeAdaption: valueOrUndefined(randomPercentageValue()),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      randomPercentageValue(),
    ),
    substantialContributionToTransitionToACircularEconomy: valueOrUndefined(randomPercentageValue()),
    substantialContributionToPollutionPreventionAndControl: valueOrUndefined(randomPercentageValue()),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      randomPercentageValue(),
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
    totalAmount: valueOrUndefined(generateDatapoint(valueOrUndefined(generateAmountWithCurrency()), reports)),
    nonEligibleShare: valueOrUndefined(generateFinancialShare()),
    eligibleShare: valueOrUndefined(generateFinancialShare()),
    nonAlignedShare: valueOrUndefined(generateFinancialShare()),
    nonAlignedActivities: valueOrUndefined(generateArray(generateActivity)),
    alignedShare: valueOrUndefined(generateFinancialShare()),
    substantialContributionToClimateChangeMitigation: valueOrUndefined(randomPercentageValue()),
    substantialContributionToClimateChangeAdaption: valueOrUndefined(randomPercentageValue()),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      randomPercentageValue(),
    ),
    substantialContributionToTransitionToACircularEconomy: valueOrUndefined(randomPercentageValue()),
    substantialContributionToPollutionPreventionAndControl: valueOrUndefined(randomPercentageValue()),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      randomPercentageValue(),
    ),
    alignedActivities: valueOrUndefined(generateArray(generateAlignedActivity)),
    enablingShare: valueOrUndefined(randomPercentageValue()),
    transitionalShare: valueOrUndefined(randomPercentageValue()),
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
