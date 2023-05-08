import { FixtureData } from "@sharedUtils/Fixtures";
import { LksgData } from "@clients/backend";
import { generateLksgFixture, generateProductionSite } from "./LksgDataFixtures";

type generatorFunction = (input: FixtureData<LksgData>) => FixtureData<LksgData>;

/**
 * Generates LkSG prepared fixtures by generating random LkSG datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const manipulatorFunctions: Array<generatorFunction> = [
    manipulateFixtureForSixLksgDataSetsInDifferentYears,
    manipulateFixtureForOneLksgDataSetWithProductionSites,
  ];
  const preparedFixturesBeforeManipulation = generateLksgFixture(manipulatorFunctions.length);
  const preparedFixtures = [];
  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  const morePreparedFixturesBeforeManipulation = generateLksgFixture(3);
  preparedFixtures.push(generateLksgFixture(1, 0)[0]);
  preparedFixtures.push(manipulateFixtureForDate(morePreparedFixturesBeforeManipulation[0], "2023-04-18"));
  preparedFixtures.push(manipulateFixtureForDate(morePreparedFixturesBeforeManipulation[1], "2023-06-22"));
  preparedFixtures.push(manipulateFixtureForDate(morePreparedFixturesBeforeManipulation[2], "2022-07-30"));
  return preparedFixtures;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForSixLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "six-lksg-data-sets-in-different-years";
  input.t.general!.masterData!.dataDate = "2022-01-01";
  input.reportingPeriod = "2022";
  return input;
}

/**
 * Sets the company name in the fixture data to a specific string, the field "employeeUnder18Apprentices" to "No", and
 * sets exactly two production sites for the "listOfProductionSites" field.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForOneLksgDataSetWithProductionSites(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "one-lksg-data-set";
  input.t.social!.childLabor!.employeeUnder18Apprentices = "No";
  input.t.general!.productionSpecific!.listOfProductionSites = [generateProductionSite(), generateProductionSite()];
  return input;
}

/**
 * Sets the company name, and, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @param date the date in the format "YYYY-MM-DD"
 * @returns the manipulated fixture data
 */
function manipulateFixtureForDate(input: FixtureData<LksgData>, date: string): FixtureData<LksgData> {
  input.companyInformation.companyName = "LkSG-date-" + date;
  input.t.general!.masterData!.dataDate = date;
  input.reportingPeriod = date.split("-")[0];
  return input;
}
