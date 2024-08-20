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
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<AdditionalCompanyInformationData>) => FixtureData<AdditionalCompanyInformationData>
  > = [];
  const preparedFixturesBeforeManipulation = generateAdditionalCompanyInformationFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}
