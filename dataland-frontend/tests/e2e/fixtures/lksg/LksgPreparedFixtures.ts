import { FixtureData } from "@sharedUtils/Fixtures";
import { LksgData } from "@clients/backend";
import { generateLksgFixture, generateProductionSite } from "./LksgDataFixtures";

/**
 * Generates LkSG prepared fixtures by generating random LkSG datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureForSixLksgDataSetsInDifferentYears(generateLksgFixture(1)[0]));
  preparedFixtures.push(manipulateFixtureForOneLksgDataSetWithProductionSites(generateLksgFixture(1, 0)[0]));
  preparedFixtures.push(manipulateFixtureForAllFields(generateLksgFixture(1, 0)[0]));
  preparedFixtures.push(manipulateFixtureForDate(generateLksgFixture(1)[0], "2023-04-18"));
  preparedFixtures.push(manipulateFixtureForDate(generateLksgFixture(1)[0], "2023-06-22"));
  preparedFixtures.push(manipulateFixtureForDate(generateLksgFixture(1)[0], "2022-07-30"));
  return preparedFixtures;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForSixLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "six-lksg-data-sets-in-different-years";
  if (input.t.general?.masterData?.dataDate) input.t.general.masterData.dataDate = "2022-01-01";
  else console.error("fakeFixture created improperly: dataDate missing");
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
  const twoProductionSites = [generateProductionSite(), generateProductionSite()];
  input.companyInformation.companyName = "one-lksg-data-set";
  input.t.general!.productionSpecific!.listOfProductionSites = twoProductionSites;
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

/**
 * Sets the company name of a Lksg fixture dataset to a specific given name
 * @param fixture Fixture data to be manipulated
 * @returns the manipulated input
 */
function manipulateFixtureForAllFields(fixture: FixtureData<LksgData>): FixtureData<LksgData> {
  fixture.companyInformation.companyName = "lksg-all-fields";
  fixture.t.general!.productionSpecific!.productionSites = "Yes";
  fixture.t.general!.productionSpecific!.listOfProductionSites = [generateProductionSite(0), generateProductionSite(0)];
  return fixture;
}
