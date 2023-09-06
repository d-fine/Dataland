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
import { generateDatapointWithUnit } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomFloat } from "@e2e/fixtures/common/NumberFixtures";
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
 * @param undefinedProbabilityOfFields the probability of an undefined value per field
 * @returns an amount of money
 */
export function generateAmountWithCurrency(undefinedProbabilityOfFields?: number): AmountWithCurrency {
  return {
    amount: valueOrUndefined(randomFloat(1000000, 10000000000, 1), undefinedProbabilityOfFields),
    currency: valueOrUndefined(generateIso4217CurrencyCode(), undefinedProbabilityOfFields),
  };
}

/**
 * Generates a random financial share
 * @param undefinedProbabilityOfFields the probability of an undefined value per field
 * @returns a financial share
 */
export function generateFinancialShare(undefinedProbabilityOfFields?: number): RelativeAndAbsoluteFinancialShare {
  return {
    relativeShareInPercent: valueOrUndefined(generatePercentage(), undefinedProbabilityOfFields),
    absoluteShare: valueOrUndefined(
      generateAmountWithCurrency(undefinedProbabilityOfFields),
      undefinedProbabilityOfFields,
    ),
  };
}

/**
 * Generates a random activity
 * @param undefinedProbabilityOfFields the probability of an undefined value per field
 * @returns a random activity
 */
function generateActivity(undefinedProbabilityOfFields?: number): EuTaxonomyActivity {
  return {
    activityName: faker.helpers.arrayElement(Object.values(Activity)),
    naceCodes: valueOrUndefined(generateListOfNaceCodes(1, 3), undefinedProbabilityOfFields),
    share: valueOrUndefined(generateFinancialShare(undefinedProbabilityOfFields), undefinedProbabilityOfFields),
  };
}

/**
 * Generates a random aligned activity
 * @param undefinedProbabilityOfFields the probability of an undefined value per field
 * @returns a random aligned activity
 */
function generateAlignedActivity(undefinedProbabilityOfFields?: number): EuTaxonomyAlignedActivity {
  return {
    ...generateActivity(undefinedProbabilityOfFields),
    substantialContributionToClimateChangeMitigation: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToClimateChangeAdaption: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToTransitionToACircularEconomy: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToPollutionPreventionAndControl: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    dnshToClimateChangeMitigation: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
    dnshToClimateChangeAdaption: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo))),
    dnshToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      faker.helpers.arrayElement(Object.values(YesNo)),
      undefinedProbabilityOfFields,
    ),
    dnshToTransitionToACircularEconomy: valueOrUndefined(
      faker.helpers.arrayElement(Object.values(YesNo)),
      undefinedProbabilityOfFields,
    ),
    dnshToPollutionPreventionAndControl: valueOrUndefined(
      faker.helpers.arrayElement(Object.values(YesNo)),
      undefinedProbabilityOfFields,
    ),
    dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      faker.helpers.arrayElement(Object.values(YesNo)),
      undefinedProbabilityOfFields,
    ),
    minimumSafeguards: valueOrUndefined(faker.helpers.arrayElement(Object.values(YesNo)), undefinedProbabilityOfFields),
  };
}

/**
 * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
 * @param reports a list of reports that can be referenced
 * @param undefinedProbabilityOfFields the probability of an undefined value per field
 * @returns the generated data
 */
export function generateEuTaxonomyPerCashflowType(
  reports: ReferencedDocuments,
  undefinedProbabilityOfFields?: number,
): EuTaxonomyDetailsPerCashFlowType {
  return {
    totalAmount: valueOrUndefined(
      generateDatapointWithUnit(randomFloat(1000000, 10000000000, 1), faker.finance.currencyCode(), reports),
      undefinedProbabilityOfFields,
    ),
    nonEligibleShare: valueOrUndefined(
      generateFinancialShare(undefinedProbabilityOfFields),
      undefinedProbabilityOfFields,
    ),
    eligibleShare: valueOrUndefined(generateFinancialShare(undefinedProbabilityOfFields), undefinedProbabilityOfFields),
    nonAlignedShare: valueOrUndefined(
      generateFinancialShare(undefinedProbabilityOfFields),
      undefinedProbabilityOfFields,
    ),
    nonAlignedActivities: valueOrUndefined(generateArray(generateActivity, 1, 3, undefinedProbabilityOfFields)),
    alignedShare: valueOrUndefined(generateFinancialShare(undefinedProbabilityOfFields), undefinedProbabilityOfFields),
    substantialContributionToClimateChangeMitigation: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToClimateChangeAdaption: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToTransitionToACircularEconomy: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToPollutionPreventionAndControl: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      generatePercentage(),
      undefinedProbabilityOfFields,
    ),
    alignedActivities: valueOrUndefined(generateArray(generateAlignedActivity, 1, 3, undefinedProbabilityOfFields)),
    enablingShare: valueOrUndefined(generatePercentage(), undefinedProbabilityOfFields),
    transitionalShare: valueOrUndefined(generatePercentage(), undefinedProbabilityOfFields),
  };
}

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @param undefinedProbabilityOfFields the probability of an undefined value per field
 * @returns the generated fixture
 */
export function generateEuTaxonomyDataForNonFinancials(
  undefinedProbabilityOfFields?: number,
): EuTaxonomyDataForNonFinancials {
  const data: EuTaxonomyDataForNonFinancials = {};
  data.general = generateEuTaxonomyWithBaseFields(undefinedProbabilityOfFields);
  data.opex = generateEuTaxonomyPerCashflowType(
    assertDefined(data.general.referencedReports),
    undefinedProbabilityOfFields,
  );
  data.capex = generateEuTaxonomyPerCashflowType(
    assertDefined(data.general.referencedReports),
    undefinedProbabilityOfFields,
  );
  data.revenue = generateEuTaxonomyPerCashflowType(
    assertDefined(data.general.referencedReports),
    undefinedProbabilityOfFields,
  );
  return data;
}
