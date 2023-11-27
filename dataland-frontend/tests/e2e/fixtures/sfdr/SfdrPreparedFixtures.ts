import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type SfdrData, YesNo } from "@clients/backend";
import { generateSfdrData, generateSfdrFixtures } from "./SfdrDataFixtures";
import { type FixtureData } from "@sharedUtils/Fixtures";

type generatorFunction = (input: FixtureData<SfdrData>) => FixtureData<SfdrData>;

/**
 * Generates SFDR prepared fixtures by generating random SFDR datasets and afterward manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSfdrPreparedFixtures(): Array<FixtureData<SfdrData>> {
  const manipulatorFunctions: Array<generatorFunction> = [
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
  preparedFixtures.push(manipulateFixtureForNoNullFields(generateSfdrFixtures(1, 0)[0]));
  preparedFixtures.push(manipulateFixtureForInvalidInputValues(generateSfdrFixtures(1,0)[0]))

  return preparedFixtures;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * sets the currencyDataPoint to an illegal negative value
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForInvalidInputValues(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "Sfdr-dataset-with-invalid-currency-input";
  input.t.social!.socialAndEmployeeMatters!.averageGrossHourlyEarningsFemaleEmployees!.value = -100;
  return input;
}


/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForNoNullFields(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = "Sfdr-dataset-with-no-null-fields";
  input.t.environmental!.biodiversity!.protectedAreasExposure!.value = YesNo.No;
  input.t.environmental!.biodiversity!.rareOrEndangeredEcosystemsExposure!.value = YesNo.Yes;
  input.t.environmental!.biodiversity!.primaryForestAndWoodedLandOfNativeSpeciesExposure!.value = YesNo.Yes;
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
  input.t.environmental!.energyPerformance = null;
  input.t.environmental!.waste = null;
  input.t.environmental!.water = null;
  input.t.environmental!.emissions = null;
  input.t.environmental!.greenhouseGasEmissions = null;
  input.t.environmental!.biodiversity!.primaryForestAndWoodedLandOfNativeSpeciesExposure = {
    quality: "Audited",
    dataSource: {
      fileReference: "string",
      page: 0,
      tagName: "string",
    },
    comment: "string",
    value: "Yes",
  };
  input.t.environmental!.biodiversity!.protectedAreasExposure = {
    quality: "Audited",
    dataSource: {
      fileReference: "string",
      page: 0,
      tagName: "string",
    },
    comment: "string",
    value: "No",
  };
  input.t.environmental!.biodiversity!.rareOrEndangeredEcosystemsExposure = {
    quality: "Audited",
    dataSource: {
      fileReference: "string",
      page: 0,
      tagName: "string",
    },
    comment: "string",
    value: "Yes",
  };

  input.t.social = null;
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

/**
 * Generates an SFDR dataset with the value null for some categories, subcategories and field values.
 * Datasets that were uploaded via the Dataland API can look like this in production.
 * @returns the dataset
 */
function generateOneSfdrDatasetWithManyNulls(): SfdrData {
  return {
    general: {
      general: {
        dataDate: "2022-08-27",
        fiscalYearDeviation: "Deviation",
        fiscalYearEnd: "2023-01-01",
        scopeOfEntities: null!,
        referencedReports: null!,
      },
    },
    social: {
      socialAndEmployeeMatters: null!,
    },
    environmental: null!,
  };
}
