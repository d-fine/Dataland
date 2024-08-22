import { type FixtureData } from '@sharedUtils/Fixtures';
import { type HeimathafenData } from '@clients/backend';
import { generateHeimathafenFixtures } from './HeimathafenDataFixtures';

/**
 * Generates heimathafen prepared fixtures by generating random heimathafen datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateHeimathafenPreparedFixtures(): Array<FixtureData<HeimathafenData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  preparedFixtures.push(generateFixturesWithNoNullFields());
  return preparedFixtures;
}

/**
 * Generate a prepared Fixture with no null entries
 * @returns the fixture
 */
function generateFixturesWithNoNullFields(): FixtureData<HeimathafenData> {
  const newFixture = generateHeimathafenFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'Heimathafen-dataset-with-no-null-fields';
  return newFixture;
}
