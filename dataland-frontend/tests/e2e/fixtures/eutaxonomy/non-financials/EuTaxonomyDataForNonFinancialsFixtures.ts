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
import { pickOneElement } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns the generated fixture
 */
export function generateEuTaxonomyDataForNonFinancials(
  undefinedProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForNonFinancials {
  const dataGenerator = new EuNonFinancialsGenerator(undefinedProbability);
  return {
    general: generateEuTaxonomyWithBaseFields(dataGenerator.reports, undefinedProbability),
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
      currency: this.valueOrUndefined(generateCurrencyCode()),
    };
  }

  /**
   * Generates a random financial share
   * @returns a financial share
   */
  randomFinancialShare(): RelativeAndAbsoluteFinancialShare | undefined {
    return this.valueOrUndefined({
      relativeShareInPercent: this.randomPercentageValue(),
      absoluteShare: this.valueOrUndefined(this.generateAmountWithCurrency()),
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
      naceCodes: this.valueOrUndefined(getRandomNumberOfNaceCodesForSpecificActivity(randomActivityName)),
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
      totalAmount: this.randomCurrencyDataPoint(),
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
