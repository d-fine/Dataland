import { Generator } from "@e2e/utils/FakeFixtureUtils";
import { ReleaseMedium, type SmePollutionEmission, type SmeSubsidiary } from "@clients/backend";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";
import { faker } from "@faker-js/faker";
import { generateFloat } from "@e2e/fixtures/common/NumberFixtures";
import { pickOneElement } from "@e2e/fixtures/FixtureUtils";

export class SmeGenerator extends Generator {
  /**
   * Generates a random subsidiary
   * @returns a random subsidiary
   */
  generateSmeSubsidiary(): SmeSubsidiary {
    return {
      nameOfSubsidiary: this.valueOrNull(faker.company.name()),
      addressOfSubsidiary: generateAddress(this.nullProbability),
    };
  }
  /**
   * Generates a random pollution emission
   * @returns a random pollution emission
   */
  generateSmePollutionEmission(): SmePollutionEmission {
    return {
      pollutionType: this.valueOrNull(faker.science.chemicalElement())?.name,
      emissionInKilograms: this.valueOrNull(generateFloat()),
      releaseMedium: pickOneElement(Object.values(ReleaseMedium)),
    };
  }
}
