import { type FixtureData } from "@sharedUtils/Fixtures";
import { type GdvData, YesNo } from "@clients/backend";
import { generateGdvFixtures } from "./GdvDataFixtures";

/**
 * Generates gdv prepared fixtures by generating random gdv datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateGdvPreparedFixtures(): Array<FixtureData<GdvData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<GdvData>) => FixtureData<GdvData>> = [];
  manipulatorFunctions.push(manipulateFixtureForNoNullFields);
  const preparedFixturesBeforeManipulation = generateGdvFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForNoNullFields(input: FixtureData<GdvData>): FixtureData<GdvData> {
  input = generateGdvFixtures(1, 0)[0];
  input.companyInformation.companyName = "Gdv-dataset-with-no-null-fields";
  if (input.t.general?.masterData) {
    input.t.general.masterData.berichtsPflicht = YesNo.Yes;
  }
  if (input.t.allgemein) {
    input.t.allgemein.sektorMitHohenKlimaauswirkungen = YesNo.Yes;
  }
  return input;
}
