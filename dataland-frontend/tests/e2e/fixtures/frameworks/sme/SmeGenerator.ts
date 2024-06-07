import { Generator } from "@e2e/utils/FakeFixtureUtils";
import { type SmeSubsidiaries } from "@clients/backend";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";
import { faker } from "@faker-js/faker";

export class SmeGenerator extends Generator {
  /**
   * Generates a random subsidiary
   * @returns a random subsidiary
   */
  generateSmeSubsidiary(): SmeSubsidiaries {
    return {
      nameOfSubsidiary: this.valueOrNull(faker.company.name()),
      addressOfSubsidiary: generateAddress(this.nullProbability),
    };
  }
}
