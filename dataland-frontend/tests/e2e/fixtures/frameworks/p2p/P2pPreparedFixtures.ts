import { type FixtureData } from '@sharedUtils/Fixtures';
import { type PathwaysToParisData } from '@clients/backend';
import { generateP2pFixtures } from './P2pDataFixtures';

/**
 * Generates Pathway To Paris prepared fixtures by generating random Pathway To Paris datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @param nullProbability probability for a field to be "null"
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns the prepared fixtures
 */
export function generateP2pPreparedFixtures(
  nullProbability: number,
  toggleRandomSectors = true
): Array<FixtureData<PathwaysToParisData>> {
  const preparedFixtures = [];
  preparedFixtures.push(
    manipulateFixtureForSixP2pDataSetsInDifferentYears(generateP2pFixtures(1, nullProbability, toggleRandomSectors)[0])
  );
  preparedFixtures.push(manipulateFixtureForDate(generateP2pFixtures(1, nullProbability)[0], '2023-04-18'));
  preparedFixtures.push(manipulateFixtureForNoNullFields(generateP2pFixtures(1, 0, false)[0]));
  preparedFixtures.push(
    manipulateFixtureForOneP2pDataSetWithFourSectors(generateP2pFixtures(1, nullProbability, toggleRandomSectors)[0])
  );
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
  input.companyInformation.companyName = 'six-p2p-data-sets-in-different-years';
  if (input.t.general?.general?.dataDate) input.t.general.general.dataDate = '2023-01-01';
  else console.error('fakeFixture created improperly: dataDate missing');
  input.reportingPeriod = '2023';
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForNoNullFields(input: FixtureData<PathwaysToParisData>): FixtureData<PathwaysToParisData> {
  input.companyInformation.companyName = 'P2p-dataset-with-no-null-fields';
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
  input.companyInformation.companyName = 'P2p-date-' + date;
  input.t.general.general.dataDate = date;
  input.reportingPeriod = date.split('-')[0];
  return input;
}

/**
 * Sets the company name in the fixture data to a specific string, the fields "ccsTechnologyAdoption",
 * "preCalcinedClayUsage" and "externalFeedCertification" to defined values, and sets exactly three sectors.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForOneP2pDataSetWithFourSectors(
  input: FixtureData<PathwaysToParisData>
): FixtureData<PathwaysToParisData> {
  input.companyInformation.companyName = 'one-p2p-data-set-with-four-sectors';
  input.t.general.general.dataDate = '2022-01-01';
  input.reportingPeriod = '2022';
  input.t.general.general.sectors = ['Ammonia', 'Cement', 'FreightTransportByRoad', 'LivestockFarming'];
  input.t.general.emissionsPlanning!.relativeEmissionsInPercent = 12;
  input.t.ammonia!.decarbonisation!.ccsTechnologyAdoptionInPercent = 54;
  input.t.cement!.material!.preCalcinedClayUsageInPercent = 23;
  input.t.livestockFarming!.animalFeed!.externalFeedCertification = {
    value: 'Yes',
    dataSource: {
      fileName: 'Policy',
      fileReference: '50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63',
    },
  };
  input.t.freightTransportByRoad!.technology!.driveMixPerFleetSegment = {
    SmallTrucks: {
      driveMixPerFleetSegmentInPercent: 77.5327,
      totalAmountOfVehicles: 1234,
    },
  };
  return input;
}
