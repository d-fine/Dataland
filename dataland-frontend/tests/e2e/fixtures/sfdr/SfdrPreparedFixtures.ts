import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { SfdrData } from "@clients/backend";
import { generateOneSfdrDatasetWithManyNulls, generateSfdrData } from "./SfdrDataFixtures";
import { FixtureData } from "@sharedUtils/Fixtures";

type generatorFunction = (input: FixtureData<SfdrData>) => FixtureData<SfdrData>;

/**
 * Generates SFDR prepared fixtures by generating random SFDR datasets and afterward manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSfdrPreparedFixtures(): Array<FixtureData<SfdrData>> {
  const manipulatorFunctions: Array<generatorFunction> = [
    manipulateFixtureForOneSfdr,
    manipulateFixtureForTwoSfdrDataSetsInDifferentYears,
    manipulateFixtureForOneFilledSubcategory,
  ];
  const preparedFixturesBeforeManipulation = generateFixtureDataset<SfdrData>(
    generateSfdrData,
    manipulatorFunctions.length,
  );
  const preparedFixtures = [];
  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  preparedFixtures.push(
    manipulateFixtureForSfdrDatasetWithLotsOfNulls(
      generateFixtureDataset<SfdrData>(generateOneSfdrDatasetWithManyNulls, 1)[0],
    ),
  );

  return preparedFixtures;
}

/**
 * Sets the company name in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForOneSfdr(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "company-with-one-sfdr-data-set";
  input.t.general.general.referencedReports = undefined;
  return input;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForOneFilledSubcategory(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "companyWithOneFilledSfdrSubcategory";
  input.t.general.general.fiscalYearEnd = "2020-01-03";
  input.t.environmental!.energyPerformance = undefined;
  input.t.environmental!.waste = undefined;
  input.t.environmental!.water = undefined;
  input.t.environmental!.emissions = undefined;
  input.t.environmental!.greenhouseGasEmissions = undefined;
  input.t.environmental!.biodiversity!.primaryForestAndWoodedLandOfNativeSpeciesExposure = {
    quality: "Audited",
    dataSource: {
      report: "string",
      page: 0,
      tagName: "string",
    },
    comment: "string",
    value: "Yes",
  };
  input.t.environmental!.biodiversity!.protectedAreasExposure = {
    quality: "Audited",
    dataSource: {
      report: "string",
      page: 0,
      tagName: "string",
    },
    comment: "string",
    value: "No",
  };
  input.t.environmental!.biodiversity!.rareOrEndangeredEcosystemsExposure = {
    quality: "Audited",
    dataSource: {
      report: "string",
      page: 0,
      tagName: "string",
    },
    comment: "string",
    value: "Yes",
  };

  input.t.social = undefined;
  return input;
}
/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForTwoSfdrDataSetsInDifferentYears(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "two-sfdr-data-sets-in-different-years";
  input.t.general.general.fiscalYearEnd = "2020-01-03";
  return input;
}

/**
 * Sets the company name of a SFDR fixture dataset to a specific given name
 * @param fixture Fixture data to be manipulated
 * @returns the manipulated input
 */
function manipulateFixtureForSfdrDatasetWithLotsOfNulls(fixture: FixtureData<SfdrData>): FixtureData<SfdrData> {
  fixture.companyInformation.companyName = "sfdr-a-lot-of-nulls";
  return fixture;
}
