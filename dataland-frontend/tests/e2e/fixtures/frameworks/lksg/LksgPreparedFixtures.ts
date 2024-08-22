import { type FixtureData } from '@sharedUtils/Fixtures';
import { type LksgData, YesNo } from '@clients/backend';
import { generateLksgFixtures } from './LksgDataFixtures';
import { LksgGenerator } from '@e2e/fixtures/frameworks/lksg/LksgGenerator';
import { generateNaceCodes } from '@e2e/fixtures/common/NaceCodeFixtures';

/**
 * Generates lksg prepared fixtures by generating random lksg datasets and
 * afterward manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<LksgData>) => FixtureData<LksgData>> = [
    generateFixutreWithNoNullFields,
    generateFixtureToNotBeAManufacturingCompany,
    generateFixtureToHaveNoChildLaborUnder18AndChildLaborUnder15,
    generateFixtureToContainProcurementCategories,
    generateFixtureForSixLksgDataSetsInDifferentYears,
    generateOneLksgDatasetWithOnlyNulls,
    generateFixtureForSixLksgDataSetsInDifferentYears,
    generateFixtureForOneLksgDataSetWithProductionSites,
    generateFixtureToContainSubcontractingCountries,
    generateFixtureWithBrokenFileReference,
  ];
  const preparedFixturesBeforeManipulation = generateLksgFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  preparedFixtures.push(generateFixtureForDate('2023-04-18'));
  preparedFixtures.push(generateFixtureForDate('2023-06-22'));
  preparedFixtures.push(generateFixtureForDate('2022-07-30'));
  return preparedFixtures;
}

/**
 * Generates a lksg fixture with no null values
 * @returns the fixture
 */
function generateFixutreWithNoNullFields(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 0)[0];
  newFixture.t.general.masterData.industry = generateNaceCodes(1, 5);
  newFixture.t.general.productionSpecific!.subcontractingCompaniesCountries = <{ [key: string]: Array<string> }>{
    DE: generateNaceCodes(1, 5),
    GB: generateNaceCodes(0, 5),
  };
  newFixture.companyInformation.companyName = 'lksg-all-fields';
  return newFixture;
}

/**
 * Ensures that the fixture contains production sites but is not a manufacturing company (to test show-if)
 * @returns the manipulated fixture data
 */
function generateFixtureToNotBeAManufacturingCompany(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  newFixture.companyInformation.companyName = 'lksg-not-a-manufacturing-company-but-has-production-sites';
  const lksgGeneratorNoUndefined = new LksgGenerator(0);
  const twoProductionSites = [
    lksgGeneratorNoUndefined.generateLksgProductionSite(),
    lksgGeneratorNoUndefined.generateLksgProductionSite(),
  ];

  newFixture.t.general.productionSpecific!.manufacturingCompany = YesNo.No;
  newFixture.t.general.productionSpecific!.productionSites = YesNo.No;
  newFixture.t.general.productionSpecific!.listOfProductionSites = twoProductionSites;

  return newFixture;
}

/**
 * Ensures that the fixture contains child labor under 18
 * @returns the manipulated fixture data
 */
function generateFixtureToHaveNoChildLaborUnder18AndChildLaborUnder15(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];

  newFixture.companyInformation.companyName = 'lksg-with-nulls-and-no-child-labor-under-18';
  newFixture.t.social!.childLabor!.employeeSUnder18 = YesNo.No;
  newFixture.t.social!.childLabor!.employeeSUnder15 = YesNo.Yes;
  newFixture.t.general.masterData.numberOfEmployees = null;
  return newFixture;
}

/**
 * Ensures that the fixture contains procurement categories that are displayed (respecting show-if)
 * @returns the manipulated fixture data
 */
function generateFixtureToContainProcurementCategories(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'lksg-with-procurement-categories';
  newFixture.t.general.productionSpecific!.manufacturingCompany = YesNo.Yes;
  if (Object.keys(newFixture.t.general.productionSpecificOwnOperations!.procurementCategories ?? {}).length < 1) {
    throw Error(
      'The fixture should contain procurement categories as the undefined percentage was set to 0. But it does not!'
    );
  }
  return newFixture;
}

/**
 * Ensures that the fixture contains subcontracting companies
 * @returns the manipulated fixture data
 */
function generateFixtureToContainSubcontractingCountries(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'lksg-with-subcontracting-countries';
  newFixture.t.general.productionSpecific!.manufacturingCompany = YesNo.Yes;
  newFixture.t.general.productionSpecific!.productionViaSubcontracting = YesNo.Yes;
  newFixture.t.general.productionSpecific!.subcontractingCompaniesCountries = {
    DE: ['A', 'G'],
    GB: ['B'],
  };
  if (Object.keys(newFixture.t.general.productionSpecificOwnOperations!.procurementCategories ?? {}).length < 1) {
    throw Error(
      'The fixture should contain procurement categories as the undefined percentage was set to 0. But it does not!'
    );
  }
  return newFixture;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 * @returns the manipulated fixture data
 */
function generateFixtureForSixLksgDataSetsInDifferentYears(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  newFixture.companyInformation.companyName = 'six-lksg-data-sets-in-different-years';
  if (newFixture.t.general?.masterData?.dataDate) newFixture.t.general.masterData.dataDate = '2022-01-01';
  else console.error('fakeFixture created improperly: dataDate missing');
  newFixture.reportingPeriod = '2022';
  return newFixture;
}

/**
 * Sets the company name in the fixture data to a specific string, the field "employeeUnder18Apprentices" to "No", and
 * sets exactly two production sites for the "listOfProductionSites" field.
 * @returns the manipulated fixture data
 */
function generateFixtureForOneLksgDataSetWithProductionSites(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  const lksgGeneratorNoUndefined = new LksgGenerator(0);

  newFixture.companyInformation.companyName = 'one-lksg-data-set-with-two-production-sites';
  newFixture.reportingPeriod = '2024';

  newFixture.t.governance!.certificationsPoliciesAndResponsibilities!.codeOfConduct =
    lksgGeneratorNoUndefined.randomBaseDataPoint(YesNo.Yes);
  const twoProductionSites = [
    lksgGeneratorNoUndefined.generateLksgProductionSite(),
    lksgGeneratorNoUndefined.generateLksgProductionSite(),
  ];
  newFixture.t.general.productionSpecific!.manufacturingCompany = YesNo.Yes;
  newFixture.t.general.productionSpecific!.productionSites = YesNo.Yes;
  newFixture.t.general.productionSpecific!.listOfProductionSites = twoProductionSites;
  return newFixture;
}

/**
 * Sets the company name, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param date the date in the format "YYYY-MM-DD"
 * @returns the manipulated fixture data
 */
function generateFixtureForDate(date: string): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  newFixture.companyInformation.companyName = 'LkSG-date-' + date;
  newFixture.t.general.masterData.dataDate = date;
  newFixture.reportingPeriod = date.split('-')[0];
  return newFixture;
}

/**
 * Generates an LKSG dataset with the value null for some categories, subcategories and field values.
 * Datasets that were uploaded via the Dataland API can look like this in production.
 * @returns the dataset
 */
function generateOneLksgDatasetWithOnlyNulls(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  newFixture.companyInformation.companyName = 'lksg-almost-only-nulls';

  newFixture.t.governance = null;
  newFixture.t.social = null;
  newFixture.t.environmental = null;

  newFixture.t.general.masterData.dataDate = '1999-12-24';
  newFixture.t.general.masterData.headOfficeInGermany = null;
  newFixture.t.general.masterData.groupOfCompanies = null;
  newFixture.t.general.masterData.groupOfCompaniesName = null;
  newFixture.t.general.masterData.industry = null;
  newFixture.t.general.masterData.seasonalOrMigrantWorkers = null;
  newFixture.t.general.masterData.shareOfTemporaryWorkers = null;
  newFixture.t.general.masterData.annualTotalRevenue = null;
  newFixture.t.general.masterData.fixedAndWorkingCapital = null;
  newFixture.t.general.productionSpecific = null;
  newFixture.t.general.productionSpecificOwnOperations = null;
  return newFixture;
}

/**
 * Generates an LKSG dataset with a no-existing file reference.
 * @returns the dataset
 */
function generateFixtureWithBrokenFileReference(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 0)[0];
  const brokenFileReference = '123';
  newFixture.companyInformation.companyName = 'TestForBrokenFileReference';
  newFixture.t.governance!.certificationsPoliciesAndResponsibilities!.codeOfConduct!.dataSource!.fileReference =
    brokenFileReference;
  return newFixture;
}
