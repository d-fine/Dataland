import {
  Activity,
  type EuTaxonomyActivity,
  type EuTaxonomyAlignedActivity,
  type RelativeAndAbsoluteFinancialShare,
  type AmountWithCurrency,
  type EuTaxonomyDataForNonFinancials,
  type EuTaxonomyDetailsPerCashFlowType,
} from "@clients/backend";
import { generateArray, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomFloat, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { faker } from "@faker-js/faker";
import { getRandomNumberOfNaceCodesForSpecificActivity } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { generateDatapointWithUnit } from "@e2e/fixtures/common/DataPointFixtures";
import { randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";

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
    relativeShareInPercent: valueOrUndefined(randomPercentageValue(), undefinedProbabilityOfFields),
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
  const randomActivityName: Activity = faker.helpers.arrayElement(Object.values(Activity));
  return {
    activityName: randomActivityName,
    naceCodes: valueOrUndefined(
      getRandomNumberOfNaceCodesForSpecificActivity(randomActivityName),
      undefinedProbabilityOfFields,
    ),
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
    ...generateActivity(0),
    substantialContributionToClimateChangeMitigationInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToClimateChangeAdaptionInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToTransitionToACircularEconomyInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToPollutionPreventionAndControlInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    dnshToClimateChangeMitigation: valueOrUndefined(randomYesNo(), undefinedProbabilityOfFields),
    dnshToClimateChangeAdaption: valueOrUndefined(randomYesNo(), undefinedProbabilityOfFields),
    dnshToSustainableUseAndProtectionOfWaterAndMarineResources: valueOrUndefined(
      randomYesNo(),
      undefinedProbabilityOfFields,
    ),
    dnshToTransitionToACircularEconomy: valueOrUndefined(randomYesNo(), undefinedProbabilityOfFields),
    dnshToPollutionPreventionAndControl: valueOrUndefined(randomYesNo(), undefinedProbabilityOfFields),
    dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: valueOrUndefined(
      randomYesNo(),
      undefinedProbabilityOfFields,
    ),
    minimumSafeguards: valueOrUndefined(randomYesNo(), undefinedProbabilityOfFields),
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
    nonAlignedActivities: valueOrUndefined(
      generateArray(generateActivity, 1, 3, undefinedProbabilityOfFields),
      undefinedProbabilityOfFields,
    ),
    alignedShare: valueOrUndefined(generateFinancialShare(undefinedProbabilityOfFields), undefinedProbabilityOfFields),
    substantialContributionToClimateChangeMitigationInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToClimateChangeAdaptionInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToTransitionToACircularEconomyInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToPollutionPreventionAndControlInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: valueOrUndefined(
      randomPercentageValue(),
      undefinedProbabilityOfFields,
    ),
    alignedActivities: valueOrUndefined(
      generateArray(generateAlignedActivity, 1, 3, undefinedProbabilityOfFields),
      undefinedProbabilityOfFields,
    ),
    enablingShareInPercent: valueOrUndefined(randomPercentageValue(), undefinedProbabilityOfFields),
    transitionalShareInPercent: valueOrUndefined(randomPercentageValue(), undefinedProbabilityOfFields),
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
