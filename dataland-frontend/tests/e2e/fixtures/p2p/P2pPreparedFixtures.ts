import { FixtureData } from "@sharedUtils/Fixtures";
import { PathwaysToParisData,} from "@clients/backend";
import { generateP2pFixture } from "./P2pDataFixtures";

/**
 * Generates LkSG prepared fixtures by generating random LkSG datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateP2pPreparedFixtures(undef_probability = 0.5, boolRandom = true): Array<FixtureData<PathwaysToParisData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureForSixP2pDataSetsInDifferentYears(generateP2pFixture(1, undef_probability, boolRandom)[0]));
  preparedFixtures.push(manipulateFixtureForDate(generateP2pFixture(1, undef_probability, boolRandom)[0], "2023-04-18"));
  preparedFixtures.push(manipulateFixtureForDate(generateP2pFixture(1, undef_probability, boolRandom)[0], "2023-06-22"));
  preparedFixtures.push(manipulateFixtureForDate(generateP2pFixture(1, undef_probability, boolRandom)[0], "2022-07-30"));
  preparedFixtures.push(manipulateFixtureForOneP2pDataSetWithTwoSectors(generateP2pFixture(1, undef_probability, boolRandom)[0]));
  return preparedFixtures;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForSixP2pDataSetsInDifferentYears(
  input: FixtureData<PathwaysToParisData>
): FixtureData<PathwaysToParisData> {
  input.companyInformation.companyName = "six-p2p-data-sets-in-different-years";
  if (input.t.general?.general?.dataDate) input.t.general.general.dataDate = "2023-01-01";
  else console.error("fakeFixture created improperly: dataDate missing");
  input.reportingPeriod = "2023";
  return input;
}

/**
 * Sets the company name, and, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @param date the date in the format "YYYY-MM-DD"
 * @returns the manipulated fixture data
 */
function manipulateFixtureForDate(
  input: FixtureData<PathwaysToParisData>,
  date: string
): FixtureData<PathwaysToParisData> {
  input.companyInformation.companyName = "P2p-date-" + date;
  input.t.general.general.dataDate = date;
  input.reportingPeriod = date.split("-")[0];
  return input;
}

/**
 * Sets the company name in the fixture data to a specific string, the field "employeeUnder18Apprentices" to "No", and
 * sets exactly two production sites for the "listOfProductionSites" field.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForOneP2pDataSetWithTwoSectors(
  input: FixtureData<PathwaysToParisData>
): FixtureData<PathwaysToParisData> {
  input.companyInformation.companyName = "one-p2p-data-set-with-two-sectors";
  input.t.general.general.dataDate = "2022-01-01";
  input.reportingPeriod = "2022";
  input.t.general.general.sector = ["Ammonia", "Cement", "LivestockFarming"];
  input.t.ammonia!.decarbonisation!.ccsTechnologyAdoption = 0.54;
  input.t.cement!.material!.preCalcinedClayUsage = 0.23;
  input.t.livestockFarming!.animalFeed!.externalFeedCertification = {
    value: "Yes",
    dataSource: {
      name: "Policy",
      reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
    },
  };
  return input;
}
