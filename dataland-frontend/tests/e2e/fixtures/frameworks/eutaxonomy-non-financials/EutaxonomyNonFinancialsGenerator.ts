import { Generator } from "@e2e/utils/FakeFixtureUtils";
import type {AmountWithCurrency, AssuranceDataPoint} from "@clients/backend";
import { generateAssuranceDatapoint } from "@e2e/fixtures/eutaxonomy-shared/AssuranceDataFixture";
import {generateCurrencyCode} from "@e2e/fixtures/common/CurrencyFixtures";

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
}
