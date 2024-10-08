import { type FixtureData } from '@sharedUtils/Fixtures';
import { type AdditionalCompanyInformationData } from '@clients/backend';
import { generateAdditionalCompanyInformationFixtures } from './AdditionalCompanyInformationDataFixtures';

/**
 * Generates additional-company-information prepared fixtures by generating random additional-company-information datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateAdditionalCompanyInformationPreparedFixtures(): Array<
  FixtureData<AdditionalCompanyInformationData>
> {
  const preparedFixtures = [];
  preparedFixtures.push(generateFixturesWithNoNullFields());
  return preparedFixtures;
}

/**
 * Generate a prepared Fixture with no null entries
 * @returns the fixture
 */
function generateFixturesWithNoNullFields(): FixtureData<AdditionalCompanyInformationData> {
  const newFixture = generateAdditionalCompanyInformationFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'additional-company-information-dataset-with-no-null-fields';
  return newFixture;
}
