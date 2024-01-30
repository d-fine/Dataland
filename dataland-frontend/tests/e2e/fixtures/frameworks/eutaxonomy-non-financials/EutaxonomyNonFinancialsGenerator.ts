import { Generator } from "@e2e/utils/FakeFixtureUtils";
import type { AssuranceDataPoint } from "@clients/backend";
import { generateAssuranceDatapoint } from "@e2e/fixtures/eutaxonomy-shared/AssuranceDataFixture";

export class EutaxonomyNonFinancialsGenerator extends Generator {
  /**
   * Generates random assurance data
   * @returns the generated random assurance data
   */
  generateAssuranceDatapoint(): AssuranceDataPoint {
    return generateAssuranceDatapoint(this.reports, this.nullProbability);
  }
}
