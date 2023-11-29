import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type GdvData } from "@clients/backend";

/**
 * Generates a set number of gdv fixtures
 * @param numFixtures the number of gdv fixtures to generate
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a set number of gdv fixtures
 */
export function generateGdvFixtures(
  numFixtures: number,
  nullProbability = DEFAULT_PROBABILITY,
): FixtureData<GdvData>[] {
  return generateFixtureDataset<GdvData>(
    () => generateGdvData(nullProbability),
    numFixtures,
    
  );
}

/**
 * Generates a random gdv dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random gdv dataset
 */
export function generateGdvData(nullProbability = DEFAULT_PROBABILITY): GdvData {
  const dataGenerator = new GdvGenerator(nullProbability);
  return {
    general: {
        masterData: {
            berichtsPflicht: dataGenerator.randomYesNo(),
        },
    },
    allgemein: {
        esgZiele: dataGenerator.randomYesNo(),
        ziele: dataGenerator.randomShortString(),
        investitionen: dataGenerator.randomShortString(),
        sektorMitHohenKlimaauswirkungen: dataGenerator.randomYesNo(),
    },
}
}

export class GdvGenerator extends Generator {

}
