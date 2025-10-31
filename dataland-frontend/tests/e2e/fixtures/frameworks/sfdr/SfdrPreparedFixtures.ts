import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import { IdentifierType, QualityOptions, type SfdrData, YesNo } from '@clients/backend';
import { generateSfdrFixtures } from './SfdrDataFixtures';
import { type FixtureData } from '@sharedUtils/Fixtures';

/**
 * Generates SFDR prepared fixtures by generating random SFDR datasets and afterward manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSfdrPreparedFixtures(): Array<FixtureData<SfdrData>> {
  return [
    manipulateFixtureForOneFilledSubcategory(generateSfdrDataWithoutNulls()),
    generateFixtureWithBrokenFileReference(generateSfdrDataWithoutNulls()),
    generateFixtureWithIncompleteReferencedReport(generateSfdrDataWithoutNulls()),
    manipulateFixtureForSfdrDatasetWithLotsOfNulls(
      generateFixtureDataset<SfdrData>(generateOneSfdrDatasetWithManyNulls, 1)[0]!
    ),
    manipulateFixtureForNoNullFields(generateSfdrDataWithoutNulls()),
    manipulateFixtureForInvalidCurrencyInput(generateSfdrDataWithoutNulls()),
    manipulateFixtureForInvalidBigDecimalDataPointInput(generateSfdrDataWithoutNulls()),
    manipulateFixtureForInvalidLongDataPointInput(generateSfdrDataWithoutNulls()),
    manipulateFixtureForEmptyStringDocumentReference(generateSfdrDataWithoutNulls()),
    manipulateFixtureForInvalidPercentageInput(generateSfdrDataWithoutNulls()),
    manipulateFixtureForTwoInvalidInputs(generateSfdrDataWithoutNulls()),
    generateFixtureWithDifferentExtendedDatapointCases(generateSfdrDataWithoutNulls()),
    ...generateFixtureOfTwoCompaniesWithSubsidiaryRelationship(
      generateSfdrDataWithoutNulls(),
      generateSfdrDataWithoutNulls()
    ),
  ];
}

/**
 * Helper function to get one SFDR data set without null entries
 * @returns One SFDR fixture data set without null entries in the data
 */
function generateSfdrDataWithoutNulls(): FixtureData<SfdrData> {
  return generateSfdrFixtures(1, 0)[0]!;
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
  input.t.social!.socialAndEmployeeMatters!.rateOfAccidents!.value = -1;
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
  input.t.social!.socialAndEmployeeMatters!.rateOfAccidents!.value = -1;
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
  input.t.social!.socialAndEmployeeMatters!.maleBoardMembersSupervisoryBoard!.dataSource!.fileReference = '';
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
  input.t.general!.general!.fiscalYearEnd!.value = '2020-01-03';
  input.t.environmental!.energyPerformance = null;
  input.t.environmental!.waste = null;
  input.t.environmental!.water = null;
  input.t.environmental!.emissions = null;
  input.t.environmental!.greenhouseGasEmissions = null;

  input.t.social = null;
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
        fiscalYearDeviation: { value: 'Deviation' },
        fiscalYearEnd: { value: '2023-01-01' },
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
  input.t.general!.general!.referencedReports = {
    notReferencedFile: { fileReference: 'invalidFileReference', fileName: 'notReferencedFile' },
  };
  return input;
}

/**
 * Generates an SFDR dataset with three data points that only contain a value, a quality and a comment.
 * @param input Fixture data to be manipulated
 * @returns the dataset
 */
function generateFixtureWithDifferentExtendedDatapointCases(input: FixtureData<SfdrData>): FixtureData<SfdrData> {
  input.companyInformation.companyName = 'TestForDataPointDisplayLogic';
  if (input.t.environmental?.greenhouseGasEmissions?.scope1GhgEmissionsInTonnes) {
    input.t.environmental.greenhouseGasEmissions.scope1GhgEmissionsInTonnes = { value: 30 };
  }
  if (input.t.environmental?.greenhouseGasEmissions?.scope2GhgEmissionsInTonnes) {
    input.t.environmental.greenhouseGasEmissions.scope2GhgEmissionsInTonnes = { quality: QualityOptions.NoDataFound };
  }
  if (input.t.environmental?.greenhouseGasEmissions?.scope2GhgEmissionsLocationBasedInTonnes) {
    input.t.environmental.greenhouseGasEmissions.scope2GhgEmissionsLocationBasedInTonnes = {
      comment: 'This is a datapoint with only comment info.',
    };
  }
  if (input.t.environmental?.greenhouseGasEmissions?.scope2GhgEmissionsMarketBasedInTonnes) {
    input.t.environmental.greenhouseGasEmissions.scope2GhgEmissionsMarketBasedInTonnes = {
      quality: QualityOptions.Estimated,
      comment: '',
    };
  }
  if (input.t.environmental?.greenhouseGasEmissions?.scope3GhgEmissionsInTonnes) {
    input.t.environmental.greenhouseGasEmissions.scope3GhgEmissionsInTonnes = {
      value: 12,
      comment: '',
    };
  }
  return input;
}

/**
 * Generates SFDR datasets for two companies that are in a parent child relation.
 * @param input_parent Fixture data to be manipulated and become the parent companies data
 * @param input_child Fixture data to be manipulated and become the child companies data
 */
function generateFixtureOfTwoCompaniesWithSubsidiaryRelationship(
  input_parent: FixtureData<SfdrData>,
  input_child: FixtureData<SfdrData>
): Array<FixtureData<SfdrData>> {
  input_child.companyInformation.parentCompanyLei = input_parent.companyInformation.identifiers[IdentifierType.Lei]![0];
  input_child.companyInformation.companyName = 'test company with parent lei and existing parent';
  input_parent.companyInformation.companyName = 'test company with lei and existing child';
  return [input_parent, input_child];
}
