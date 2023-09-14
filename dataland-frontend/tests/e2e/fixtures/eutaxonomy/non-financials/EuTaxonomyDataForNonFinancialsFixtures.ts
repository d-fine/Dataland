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
import { generateEuroValue } from "@e2e/fixtures/common/NumberFixtures";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { faker } from "@faker-js/faker";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

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
      amount: this.valueOrUndefined(generateEuroValue()),
      currency: this.valueOrUndefined(generateIso4217CurrencyCode()),
    };
  }

  /**
   * Generates a random financial share
   * @returns a financial share
   */
  generateFinancialShare(): RelativeAndAbsoluteFinancialShare | undefined {
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
    return {
      activityName: faker.helpers.arrayElement(Object.values(Activity)),
      naceCodes: this.valueOrUndefined(generateListOfNaceCodes()),
      share: this.generateFinancialShare(),
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
      totalAmount: this.randomDataPoint(this.generateAmountWithCurrency()),
      nonEligibleShare: this.generateFinancialShare(),
      eligibleShare: this.generateFinancialShare(),
      nonAlignedShare: this.generateFinancialShare(),
      nonAlignedActivities: this.randomArray(() => this.generateActivity()),
      alignedShare: this.generateFinancialShare(),
      substantialContributionToClimateChangeMitigationInPercent: this.randomPercentageValue(),
      substantialContributionToClimateChangeAdaptionInPercent: this.randomPercentageValue(),
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
        this.randomPercentageValue(),
      substantialContributionToTransitionToACircularEconomyInPercent: this.randomPercentageValue(),
      substantialContributionToPollutionPreventionAndControlInPercent: this.randomPercentageValue(),
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
        this.randomPercentageValue(),
      alignedActivities: this.randomArray(() => this.generateAlignedActivity()),
      enablingShareInPercent: this.randomPercentageValue(),
      transitionalShareInPercent: this.randomPercentageValue(),
    };
  }
}
