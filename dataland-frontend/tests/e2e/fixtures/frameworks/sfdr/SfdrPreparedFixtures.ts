import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import { type SfdrData, YesNo } from '@clients/backend';
import { generateSfdrFixtures } from './SfdrDataFixtures';
import { type FixtureData } from '@sharedUtils/Fixtures';

/**
 * Generates SFDR prepared fixtures by generating random SFDR datasets and afterward manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSfdrPreparedFixtures(): Array<FixtureData<SfdrData>> {
  const preparedFixtures = [];

  preparedFixtures.push(manipulateFixtureForTwoSfdrDataSetsInDifferentYears(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(manipulateFixtureForOneFilledSubcategory(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(generateFixtureWithBrokenFileReference(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(generateFixtureWithIncompleteReferencedReport(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(
    manipulateFixtureForSfdrDatasetWithLotsOfNulls(
      generateFixtureDataset<SfdrData>(generateOneSfdrDatasetWithManyNulls, 1)[0]
    )
  );
  preparedFixtures.push(manipulateFixtureForNoNullFields(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(manipulateFixtureForInvalidCurrencyInput(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(manipulateFixtureForInvalidBigDecimalDataPointInput(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(manipulateFixtureForInvalidLongDataPointInput(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(manipulateFixtureForEmptyStringDocumentReference(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(manipulateFixtureForInvalidPercentageInput(generateSfdrDataWithoutNulls()));
  preparedFixtures.push(manipulateFixtureForTwoInvalidInputs(generateSfdrDataWithoutNulls()));

  return preparedFixtures;
}

/**
 * Helper function to get one SFDR data set without null entries
 * @returns One SFDR fixture data set without null entries in the data
 */
function generateSfdrDataWithoutNulls(): FixtureData<SfdrData> {
  return generateSfdrFixtures(1, 0)[0];
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * sets the currencyDataPoint to an illegal negative value
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForInvalidCurrencyInput(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'Sfdr-dataset-with-invalid-currency-input';
  input.t.social!.socialAndEmployeeMatters!.averageGrossHourlyEarningsFemaleEmployees!.value = -100;
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * sets an extendedDataPoint<BigDecimal> to an invalid negative value>
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForInvalidBigDecimalDataPointInput(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'Sfdr-dataset-with-invalid-negative-big-decimal-input';
  input.t.social!.socialAndEmployeeMatters!.workdaysLostInDays!.value = -1;
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * sets an extendedDataPoint<long> to an invalid negative value>
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForInvalidLongDataPointInput(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'Sfdr-dataset-with-invalid-negative-long-input';
  input.t.social!.antiCorruptionAndAntiBribery!.reportedConvictionsOfBriberyAndCorruption!.value = -1;
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * sets a percentage to an invalid value > 100
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForInvalidPercentageInput(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'Sfdr-dataset-with-invalid-percentage-input';
  input.t.social!.socialAndEmployeeMatters!.rateOfAccidentsInPercent!.value = 120;
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * sets a percentage to an invalid value > 100 AND a number to -1
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForTwoInvalidInputs(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'Sfdr-dataset-with-two-invalid-inputs';
  input.t.social!.socialAndEmployeeMatters!.rateOfAccidentsInPercent!.value = 120;
  input.t.social!.antiCorruptionAndAntiBribery!.reportedConvictionsOfBriberyAndCorruption!.value = -1;
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * sets a document reference to empty string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForEmptyStringDocumentReference(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'Sfdr-dataset-with-empty-string-document-reference';
  input.t.social!.socialAndEmployeeMatters!.maleBoardMembers!.dataSource!.fileReference = '';
  return input;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForNoNullFields(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'Sfdr-dataset-with-no-null-fields';
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
  input.companyInformation.companyName = 'companyWithOneFilledSfdrSubcategory';
  input.t.general.general.fiscalYearEnd = '2020-01-03';
  input.t.environmental!.energyPerformance = null;
  input.t.environmental!.waste = null;
  input.t.environmental!.water = null;
  input.t.environmental!.emissions = null;
  input.t.environmental!.greenhouseGasEmissions = null;

  input.t.social = null;
  return input;
}
/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForTwoSfdrDataSetsInDifferentYears(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'two-sfdr-data-sets-in-different-years';
  input.t.general.general.fiscalYearEnd = '2020-01-03';
  return input;
}

/**
 * Sets the company name of a SFDR fixture dataset to a specific given name
 * @param fixture Fixture data to be manipulated
 * @returns the manipulated input
 */
function manipulateFixtureForSfdrDatasetWithLotsOfNulls(fixture: FixtureData<SfdrData>): FixtureData<SfdrData> {
  fixture.companyInformation.companyName = 'sfdr-a-lot-of-nulls';
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
        dataDate: '2022-08-27',
        fiscalYearDeviation: 'Deviation',
        fiscalYearEnd: '2023-01-01',
        referencedReports: null!,
      },
    },
    social: {
      socialAndEmployeeMatters: null!,
    },
    environmental: null!,
  };
}

/**
 * Generates an SFDR dataset with a no-existing file reference.
 * @param input Fixture data to be manipulated
 * @returns the dataset
 */
function generateFixtureWithBrokenFileReference(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  const brokenFileReference = '123';
  input.companyInformation.companyName = 'TestForBrokenFileReference';
  input.t.environmental!.greenhouseGasEmissions!.scope2GhgEmissionsInTonnes!.dataSource!.fileReference =
    brokenFileReference;
  return input;
}

/**
 * Generates an SFDR dataset with a incomplete referenced report list
 * @param input Fixture data to be manipulated
 * @returns the dataset
 */
function generateFixtureWithIncompleteReferencedReport(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'TestForIncompleteReferencedReport';
  input.t.general.general.referencedReports = {
    notReferencedFile: { fileReference: 'invalidFileReference', fileName: 'notReferencedFile' },
  };
  return input;
}
