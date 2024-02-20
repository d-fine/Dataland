import { type FixtureData } from "@sharedUtils/Fixtures";
import { type LksgData, YesNo } from "@clients/backend";
import { generateLksgFixtures } from "./LksgDataFixtures";
import { LksgGenerator } from "@e2e/fixtures/frameworks/lksg/LksgGenerator";

/**
 * Generates lksg prepared fixtures by generating random lksg datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<(input: FixtureData<LksgData>) => FixtureData<LksgData>> = [
    generateFixutreWithNoNullFields,
    generateFixtureWithALotOfNullFields,
    generateFixtureToNotBeAManufacturingCompany,
    generateFixtureToHaveNoChildLaborUnder18AndChildLaborUnder15,
    generateFixtureToContainProcurementCategories,
    generateFixtureForSixLksgDataSetsInDifferentYears,
  ];
  const preparedFixturesBeforeManipulation = generateLksgFixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}

/**
 * Generates a lksg fixture with no null values
 * @returns the fixture
 */
function generateFixutreWithNoNullFields(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = "lksg-all-fields";
  return newFixture;
}

/**
 * Generates a lksg fixture with a lot of null fields
 * @returns the fixture
 */
function generateFixtureWithALotOfNullFields(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1, 80)[0];
  newFixture.companyInformation.companyName = "lksg-a-lot-of-nulls";
  return newFixture;
}

/**
 * Ensures that the fixture contains production sites but is not a manufacturing company (to test show-if)
 * @returns the manipulated fixture data
 */
function generateFixtureToNotBeAManufacturingCompany(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  newFixture.companyInformation.companyName = "lksg-not-a-manufacturing-company-but-has-production-sites";
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
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function generateFixtureToHaveNoChildLaborUnder18AndChildLaborUnder15(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];

  newFixture.companyInformation.companyName = "lksg-with-nulls-and-no-child-labor-under-18";
  newFixture.t.social!.childLabor!.employeeSUnder18 = YesNo.No;
  newFixture.t.social!.childLabor!.employeeSUnder15 = YesNo.Yes;
  newFixture.t.general.masterData.numberOfEmployees = null;
  return newFixture;
}

/**
 * Ensures that the fixture contains procurement categories that are displayed (respecting show-if)
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function generateFixtureToContainProcurementCategories(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  newFixture.companyInformation.companyName = "lksg-with-procurement-categories";
  newFixture.t.general.productionSpecific!.manufacturingCompany = YesNo.Yes;
  if (Object.keys(newFixture.t.general.productionSpecificOwnOperations!.procurementCategories ?? {}).length < 1) {
    throw Error(
      "The fixture should contain procurement categories as the undefined percentage was set to 0. But it does not!",
    );
  }
  return newFixture;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function generateFixtureForSixLksgDataSetsInDifferentYears(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  newFixture.companyInformation.companyName = "six-lksg-data-sets-in-different-years";
  if (newFixture.t.general?.masterData?.dataDate) newFixture.t.general.masterData.dataDate = "2022-01-01";
  else console.error("fakeFixture created improperly: dataDate missing");
  newFixture.reportingPeriod = "2022";
  return newFixture;
}

/**
 * Sets the company name in the fixture data to a specific string, the field "employeeUnder18Apprentices" to "No", and
 * sets exactly two production sites for the "listOfProductionSites" field.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function generateFixtureForOneLksgDataSetWithProductionSites(): FixtureData<LksgData> {
  const newFixture = generateLksgFixtures(1)[0];
  const lksgGeneratorNoUndefined = new LksgGenerator(0);

  newFixture.companyInformation.companyName = "one-lksg-data-set-with-two-production-sites";

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
