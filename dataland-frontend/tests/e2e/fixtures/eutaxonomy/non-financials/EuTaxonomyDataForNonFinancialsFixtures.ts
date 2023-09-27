import {
  Activity,
  type EuTaxonomyActivity,
  type EuTaxonomyAlignedActivity,
  type RelativeAndAbsoluteFinancialShare,
  type AmountWithCurrency,
  type EuTaxonomyDataForNonFinancials,
  type EuTaxonomyDetailsPerCashFlowType,
} from "@clients/backend";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { getRandomNumberOfNaceCodesForSpecificActivity } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { generateCurrencyValue } from "@e2e/fixtures/common/NumberFixtures";
import { pickOneElement } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @param setMissingValuesToNull decides whether missing values are represented by "undefined" or "null"
 * @param missingValueProbability the probability (as number between 0 and 1) for missing values in optional fields
 * @returns the generated fixture
 */
export function generateEuTaxonomyDataForNonFinancials(
  setMissingValuesToNull = true,
  missingValueProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForNonFinancials {
  const dataGenerator = new EuNonFinancialsGenerator(missingValueProbability, setMissingValuesToNull);
  return {
    general: generateEuTaxonomyWithBaseFields(dataGenerator.reports, setMissingValuesToNull, missingValueProbability),
    opex: dataGenerator.generateEuTaxonomyPerCashflowType(),
    capex: dataGenerator.generateEuTaxonomyPerCashflowType(),
    revenue: dataGenerator.generateEuTaxonomyPerCashflowType(),
  };
}

export class EuNonFinancialsGenerator extends Generator {
  /**
   * Generates a random amount of money
   * @returns an amount of money
   */
  generateAmountWithCurrency(): AmountWithCurrency {
    return {
      amount: this.randomCurrencyValue(),
      currency: this.valueOrMissing(generateCurrencyCode()),
    };
  }

  /**
   * Generates a random financial share
   * @returns a financial share
   */
  randomFinancialShare(): RelativeAndAbsoluteFinancialShare | undefined | null {
    return this.valueOrMissing({
      relativeShareInPercent: this.randomPercentageValue(),
      absoluteShare: this.valueOrMissing(this.generateAmountWithCurrency()),
    });
  }

  /**
   * Generates a random activity
   * @returns a random activity
   */
  generateActivity(): EuTaxonomyActivity {
    const randomActivityName: Activity = pickOneElement(Object.values(Activity));
    return {
      activityName: randomActivityName,
      naceCodes: this.valueOrMissing(getRandomNumberOfNaceCodesForSpecificActivity(randomActivityName)),
      share: this.randomFinancialShare(),
    };
  }

  /**
   * Generates a random aligned activity
   * @returns a random aligned activity
   */
  generateAlignedActivity(): EuTaxonomyAlignedActivity {
    return {
      ...this.generateActivity(),
      substantialContributionToClimateChangeMitigationInPercent: this.randomPercentageValue(),
      substantialContributionToClimateChangeAdaptionInPercent: this.randomPercentageValue(),
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
        this.randomPercentageValue(),
      substantialContributionToTransitionToACircularEconomyInPercent: this.randomPercentageValue(),
      substantialContributionToPollutionPreventionAndControlInPercent: this.randomPercentageValue(),
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
        this.randomPercentageValue(),
      dnshToClimateChangeMitigation: this.randomYesNo(),
      dnshToClimateChangeAdaption: this.randomYesNo(),
      dnshToSustainableUseAndProtectionOfWaterAndMarineResources: this.randomYesNo(),
      dnshToTransitionToACircularEconomy: this.randomYesNo(),
      dnshToPollutionPreventionAndControl: this.randomYesNo(),
      dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: this.randomYesNo(),
      minimumSafeguards: this.randomYesNo(),
    };
  }

  /**
   * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
   * @returns the generated data
   */
  generateEuTaxonomyPerCashflowType(): EuTaxonomyDetailsPerCashFlowType {
    return {
      totalAmount: this.randomDataPoint(generateCurrencyValue(), generateCurrencyCode()),
      nonEligibleShare: this.randomFinancialShare(),
      eligibleShare: this.randomFinancialShare(),
      nonAlignedShare: this.randomFinancialShare(),
      nonAlignedActivities: this.randomArray(() => this.generateActivity(), 0, 2),
      alignedShare: this.randomFinancialShare(),
      substantialContributionToClimateChangeMitigationInPercent: this.randomPercentageValue(),
      substantialContributionToClimateChangeAdaptionInPercent: this.randomPercentageValue(),
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
        this.randomPercentageValue(),
      substantialContributionToTransitionToACircularEconomyInPercent: this.randomPercentageValue(),
      substantialContributionToPollutionPreventionAndControlInPercent: this.randomPercentageValue(),
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
        this.randomPercentageValue(),
      alignedActivities: this.randomArray(() => this.generateAlignedActivity(), 0, 2),
      enablingShareInPercent: this.randomPercentageValue(),
      transitionalShareInPercent: this.randomPercentageValue(),
    };
  }
}
