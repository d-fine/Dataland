import { Generator } from '@e2e/utils/FakeFixtureUtils';
import type { EuTaxonomyActivity, EuTaxonomyAlignedActivity } from '@clients/backend';
import { pickOneElement } from '@e2e/fixtures/FixtureUtils';
import { Activity } from '@clients/backend';
import { getRandomNumberOfNaceCodesForSpecificActivity } from '@e2e/fixtures/common/NaceCodeFixtures';

export class EutaxonomyNonFinancialsGenerator extends Generator {
  /**
   * Generates a random eu taxonomy activity
   * @returns a random eu taxonomy activity
   */
  generateActivity(): EuTaxonomyActivity {
    const randomActivityName: Activity = pickOneElement(Object.values(Activity));
    return {
      activityName: randomActivityName,
      naceCodes: this.valueOrNull(getRandomNumberOfNaceCodesForSpecificActivity(randomActivityName) ?? []),
      share: {
        relativeShareInPercent: this.randomPercentageValue(),
        absoluteShare: this.valueOrNull(this.generateAmountWithCurrency()),
      },
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
      substantialContributionToClimateChangeAdaptationInPercent: this.randomPercentageValue(),
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent:
        this.randomPercentageValue(),
      substantialContributionToTransitionToACircularEconomyInPercent: this.randomPercentageValue(),
      substantialContributionToPollutionPreventionAndControlInPercent: this.randomPercentageValue(),
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent:
        this.randomPercentageValue(),
      dnshToClimateChangeMitigation: this.randomYesNo(),
      dnshToClimateChangeAdaptation: this.randomYesNo(),
      dnshToSustainableUseAndProtectionOfWaterAndMarineResources: this.randomYesNo(),
      dnshToTransitionToACircularEconomy: this.randomYesNo(),
      dnshToPollutionPreventionAndControl: this.randomYesNo(),
      dnshToProtectionAndRestorationOfBiodiversityAndEcosystems: this.randomYesNo(),
      minimumSafeguards: this.randomYesNo(),
      enablingActivity: this.randomYesNo(),
      transitionalActivity: this.randomYesNo(),
    };
  }
}
