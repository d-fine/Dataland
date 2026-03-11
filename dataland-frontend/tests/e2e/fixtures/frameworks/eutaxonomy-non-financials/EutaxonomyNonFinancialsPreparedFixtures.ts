import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyNonFinancialsData } from '@clients/backend';
import {
  generateEutaxonomyNonFinancialsData,
  generateEutaxonomyNonFinancialsFixtures,
} from './EutaxonomyNonFinancialsDataFixtures';
import { EutaxonomyNonFinancialsGenerator } from '@e2e/fixtures/frameworks/eutaxonomy-non-financials/EutaxonomyNonFinancialsGenerator';

/**
 * Generates eutaxonomy-non-financials prepared fixtures by generating random eutaxonomy-non-financials datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyNonFinancialsPreparedFixtures(): Array<FixtureData<EutaxonomyNonFinancialsData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<EutaxonomyNonFinancialsData>) => FixtureData<EutaxonomyNonFinancialsData>
  > = [
    createCompanyAlphaWithAllFieldsDefined,
    createCompanyBetaWithAllFieldsDefined,
    createCompanyGammaWithAllFieldsDefined,
    createCompanyWithIncompleteReferencedReportsList,
  ];
  const preparedFixturesBeforeManipulation = generateEutaxonomyNonFinancialsFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  return preparedFixtures;
}

/**
 * Creates a prepared fixture that has only defined fields and no fields with missing values
 * @param input the base fixture to modify
 * @param companyName the name of the associated company
 * @param reportingPeriod of the dataset
 * @returns the modified fixture
 */
function createDatasetThatHasAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>,
  companyName: string,
  reportingPeriod: string
): FixtureData<EutaxonomyNonFinancialsData> {
  input.companyInformation.companyName = companyName;
  input.reportingPeriod = reportingPeriod;
  input.t = generateEutaxonomyNonFinancialsData(0);
  return input;
}

/**
 * Creates a prepared fixture that has no fields with missing values and a specific name
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyAlphaWithAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>
): FixtureData<EutaxonomyNonFinancialsData> {
  return createDatasetThatHasAllFieldsDefined(input, 'all-fields-defined-for-eu-taxo-non-financials-alpha', '2023');
}

/**
 * Creates a prepared fixture that has no fields with missing values and a specific name
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyBetaWithAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>
): FixtureData<EutaxonomyNonFinancialsData> {
  return createDatasetThatHasAllFieldsDefined(input, 'all-fields-defined-for-eu-taxo-non-financials-beta', '2022');
}

/**
 * Creates a prepared fixture that has no fields with missing values and a specific name.
 * The aligned and non-aligned activities lists need at least one element each because this fixture is used in a test
 * where modals for displaying these lists are validated.
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyGammaWithAllFieldsDefined(
  input: FixtureData<EutaxonomyNonFinancialsData>
): FixtureData<EutaxonomyNonFinancialsData> {
  input.companyInformation.companyName = 'all-fields-defined-for-eu-taxo-non-financials-gamma';
  input.reportingPeriod = '2021';
  input.t = generateEutaxonomyNonFinancialsData(0);
  const eutaxoNonFinancialsGenerator = new EutaxonomyNonFinancialsGenerator(0);
  const someNonAlignedActivities = eutaxoNonFinancialsGenerator.randomExtendedDataPoint(
    eutaxoNonFinancialsGenerator.randomArray(() => eutaxoNonFinancialsGenerator.generateActivity(), 1, 4)
  );
  const someAlignedActivities = eutaxoNonFinancialsGenerator.randomExtendedDataPoint(
    eutaxoNonFinancialsGenerator.randomArray(() => eutaxoNonFinancialsGenerator.generateAlignedActivity(), 1, 10)
  );

  const modifiedInput = { ...input };
  if (modifiedInput.t.capex?.nonAlignedActivities) {
    modifiedInput.t.capex.nonAlignedActivities = someNonAlignedActivities;
  }
  if (modifiedInput.t.capex?.alignedActivities) {
    modifiedInput.t.capex.alignedActivities = someAlignedActivities;
  }
  return modifiedInput;
}
/**
 * Creates a prepared fixture with a missing referenced reports list
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyWithIncompleteReferencedReportsList(
  input: FixtureData<EutaxonomyNonFinancialsData>
): FixtureData<EutaxonomyNonFinancialsData> {
  input.companyInformation.companyName = 'TestForIncompleteReferencedReport';
  input.reportingPeriod = '2021';
  input.t = generateEutaxonomyNonFinancialsData(0);
  if (input.t.general) input.t.general.referencedReports = null;
  return input;
}
