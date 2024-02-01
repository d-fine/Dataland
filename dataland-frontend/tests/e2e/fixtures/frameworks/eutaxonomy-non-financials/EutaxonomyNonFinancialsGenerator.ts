import { Generator } from "@e2e/utils/FakeFixtureUtils";
import type { AmountWithCurrency, AssuranceDataPoint, EuTaxonomyActivity } from "@clients/backend";
import { generateAssuranceDatapoint } from "@e2e/fixtures/eutaxonomy-shared/AssuranceDataFixture";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { Activity } from "@clients/backend";
import { getRandomNumberOfNaceCodesForSpecificActivity } from "@e2e/fixtures/common/NaceCodeFixtures";

export class EutaxonomyNonFinancialsGenerator extends Generator {
  /**
   * Generates random assurance data
   * @returns the generated random assurance data
   */
  generateAssuranceDatapoint(): AssuranceDataPoint {
    return generateAssuranceDatapoint(this.reports, this.nullProbability);
  }

  /**
   * Generates a random amount of money
   * @returns an amount of money
   */
  generateAmountWithCurrency(): AmountWithCurrency {
    return {
      amount: this.randomCurrencyValue(),
      currency: this.valueOrNull(generateCurrencyCode()),
    };
  }

  /**
   * Generates a random eu taxonomy activity
   * @returns a random eu taxonomy activity
   */
  generateActivity(): EuTaxonomyActivity {
    const randomActivityName: Activity = pickOneElement(Object.values(Activity));
    return {
      activityName: randomActivityName,
      naceCodes: this.valueOrNull(getRandomNumberOfNaceCodesForSpecificActivity(randomActivityName)),
      share: {
        relativeShareInPercent: this.randomPercentageValue(),
        absoluteShare: this.generateAmountWithCurrency(),
      },
    };
  }
}
