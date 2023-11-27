import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type HeimathafenData } from "@clients/backend";

/**
 * Generates a set number of heimathafen fixtures
 * @param numFixtures the number of heimathafen fixtures to generate
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a set number of heimathafen fixtures
 */
export function generateHeimathafenFixtures(
  numFixtures: number,
  nullProbability = DEFAULT_PROBABILITY,
): FixtureData<HeimathafenData>[] {
  return generateFixtureDataset<HeimathafenData>(() => generateHeimathafenData(nullProbability), numFixtures);
}

/**
 * Generates a random heimathafen dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random heimathafen dataset
 */
export function generateHeimathafenData(nullProbability = DEFAULT_PROBABILITY): HeimathafenData {
  const dataGenerator = new HeimathafenGenerator(nullProbability);
  return {
    general: {
      datenanbieter: {
        unternehmenseigentumUndEigentuemerstruktur: dataGenerator.randomShortString(),
      },
    },
  };
}

export class HeimathafenGenerator extends Generator {}
