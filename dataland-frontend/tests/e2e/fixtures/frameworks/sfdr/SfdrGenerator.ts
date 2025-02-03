import { Generator } from '@e2e/utils/FakeFixtureUtils';
import { type SfdrHighImpactClimateSectorEnergyConsumption } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/sfdr-high-impact-climate-sector-energy-consumption';
import { pickSubsetOfElements } from '@e2e/fixtures/FixtureUtils';
import { HighImpactClimateSector } from '@/api-models/HighImpactClimateSector';
import { generateFloat } from '@e2e/fixtures/common/NumberFixtures';

export class SfdrGenerator extends Generator {
  /**
   * Generates a random map of procurement categories
   * @returns random map of procurement categories
   */
  generateHighImpactClimateSectors(): { [key: string]: SfdrHighImpactClimateSectorEnergyConsumption } {
     
    const keys: HighImpactClimateSector[] = pickSubsetOfElements(Object.values(HighImpactClimateSector), 0);
    return Object.fromEntries(
      new Map<string, SfdrHighImpactClimateSectorEnergyConsumption>(
        keys.map((naceCode) => [
          naceCode as string,
          {
            highImpactClimateSectorEnergyConsumptionInGWh: this.randomExtendedDataPoint(generateFloat()),
            highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue:
              this.randomExtendedDataPoint(generateFloat()),
          } as SfdrHighImpactClimateSectorEnergyConsumption,
        ])
      )
    );
  }
}
