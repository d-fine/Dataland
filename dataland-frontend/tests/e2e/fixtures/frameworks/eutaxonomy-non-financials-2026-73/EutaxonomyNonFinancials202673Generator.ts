import { Generator } from '@e2e/utils/FakeFixtureUtils';
import { Activity } from '@clients/backend';
import type { EuTaxonomyEligibleOrAlignedActivity } from '@clients/backend';
import { pickOneElement } from '@e2e/fixtures/FixtureUtils';
import { getRandomNumberOfNaceCodesForSpecificActivity } from '@e2e/fixtures/common/NaceCodeFixtures';

export class EutaxonomyNonFinancials202673Generator extends Generator {
  /**
   * Generates a random eligible or aligned activity for the eutaxonomy-non-financials-2026-73 framework
   * @returns a random EuTaxonomyEligibleOrAlignedActivity
   */
  generateEligibleOrAlignedActivity(): EuTaxonomyEligibleOrAlignedActivity {
    const randomActivityName: Activity = pickOneElement(Object.values(Activity));
    return {
      activityName: randomActivityName,
      naceCodes: this.valueOrNull(getRandomNumberOfNaceCodesForSpecificActivity(randomActivityName) ?? []),
      share: this.valueOrNull({
        relativeShareInPercent: this.randomPercentageValue(),
        absoluteShare: this.valueOrNull(this.generateAmountWithCurrency()),
      }),
      relativeEligibleShareInPercent: this.valueOrNull(this.randomPercentageValue()),
      substantialContributionToClimateChangeMitigationInPercent: this.valueOrNull(this.randomPercentageValue()),
      substantialContributionToClimateChangeAdaptationInPercent: this.valueOrNull(this.randomPercentageValue()),
      substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent: this.valueOrNull(
        this.randomPercentageValue()
      ),
      substantialContributionToTransitionToACircularEconomyInPercent: this.valueOrNull(this.randomPercentageValue()),
      substantialContributionToPollutionPreventionAndControlInPercent: this.valueOrNull(this.randomPercentageValue()),
      substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent: this.valueOrNull(
        this.randomPercentageValue()
      ),
      enablingActivity: this.valueOrNull(this.randomYesNo()),
      transitionalActivity: this.valueOrNull(this.randomYesNo()),
    };
  }
}
