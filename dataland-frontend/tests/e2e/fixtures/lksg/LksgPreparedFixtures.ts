import { type FixtureData } from "@sharedUtils/Fixtures";
import { type LksgData, YesNo } from "@clients/backend";
import { generateLksgFixture, generateProductionSite, LksgGenerator } from "./LksgDataFixtures";
import { generateReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";

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
  preparedFixtures.push(manipulateFixtureForLksgDatasetWithLotsOfNulls(generateOneLksgFixtureWithManyNulls()));
  preparedFixtures.push(manipulateFixtureToContainProcurementCategories(generateLksgFixture(1, 0)[0]));
  preparedFixtures.push(manipulateFixtureToNotBeAManufacturingCompany(generateLksgFixture(1, 0)[0]));
  return preparedFixtures;
}

/**
 * Ensures that the fixture contains production sites but is not a manufacturing company (to test show-if)
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureToNotBeAManufacturingCompany(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "lksg-not-a-manufacturing-company-but-has-production-sites";
  const twoProductionSites = [generateProductionSite(), generateProductionSite()];

  input.t.general.productionSpecific!.manufacturingCompany = YesNo.No;
  input.t.general.productionSpecific!.productionSites = YesNo.No;
  input.t.general.productionSpecific!.listOfProductionSites = twoProductionSites;

  return input;
}

/**
 * Ensures that the fixture contains procurement categories that are displayed (respecting show-if)
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureToContainProcurementCategories(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "lksg-with-procurement-categories";
  input.t.general.productionSpecific!.manufacturingCompany = YesNo.Yes;
  if (
    Object.keys(input.t.general.productionSpecificOwnOperations!.productsServicesCategoriesPurchased ?? {}).length < 1
  ) {
    throw Error(
      "The fixture should contain procurement categories as the undefined percentage was set to 0. But it does not!",
    );
  }
  return input;
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
  const lksgGeneratorNoUndefined = new LksgGenerator(0);

  input.companyInformation.companyName = "one-lksg-data-set-with-two-production-sites";

  input.t.governance!.certificationsPoliciesAndResponsibilities!.sa8000Certification =
    lksgGeneratorNoUndefined.randomBaseDataPoint(YesNo.Yes);

  input.t.general.productionSpecific!.manufacturingCompany = YesNo.Yes;
  input.t.general.productionSpecific!.productionSites = YesNo.Yes;
  input.t.general.productionSpecific!.listOfProductionSites = twoProductionSites;
  return input;
}

/**
 * Sets the company name, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @param date the date in the format "YYYY-MM-DD"
 * @returns the manipulated fixture data
 */
function manipulateFixtureForDate(input: FixtureData<LksgData>, date: string): FixtureData<LksgData> {
  input.companyInformation.companyName = "LkSG-date-" + date;
  input.t.general.masterData.dataDate = date;
  input.reportingPeriod = date.split("-")[0];
  return input;
}

/**
 * Sets the company name, production sites, and list of production sites of a Lksg fixture dataset to
 * specific values needed for tests.
 * @param fixture Fixture data to be manipulated
 * @returns the manipulated input
 */
function manipulateFixtureForAllFields(fixture: FixtureData<LksgData>): FixtureData<LksgData> {
  fixture.companyInformation.companyName = "lksg-all-fields";
  fixture.t.general.productionSpecific!.productionSites = "Yes";
  fixture.t.general.productionSpecific!.listOfProductionSites = [generateProductionSite(0), generateProductionSite(0)];
  return fixture;
}

/**
 * Sets the company name of a Lksg fixture dataset to a specific given name
 * @param fixture Fixture data to be manipulated
 * @returns the manipulated input
 */
function manipulateFixtureForLksgDatasetWithLotsOfNulls(fixture: FixtureData<LksgData>): FixtureData<LksgData> {
  fixture.companyInformation.companyName = "lksg-a-lot-of-nulls";
  return fixture;
}

/**
 * Generates a Lksg fixture with a dataset with many null values for categories, subcategories and field values
 * @returns the fixture
 */
function generateOneLksgFixtureWithManyNulls(): FixtureData<LksgData> {
  return generateFixtureDataset<LksgData>(
    () => generateOneLksgDatasetWithManyNulls(),
    1,
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || generateReportingPeriod(),
  )[0];
}

/**
 * Generates an LKSG dataset with the value null for some categories, subcategories and field values.
 * Datasets that were uploaded via the Dataland API can look like this in production.
 * @returns the dataset
 */
function generateOneLksgDatasetWithManyNulls(): LksgData {
  return {
    general: {
      masterData: {
        dataDate: "1999-12-24",
        headOfficeInGermany: null!,
        groupOfCompanies: null!,
        groupOfCompaniesName: null!,
        industry: null!,
        numberOfEmployees: null!,
        seasonalOrMigrantWorkers: null!,
        shareOfTemporaryWorkers: null!,
        totalRevenueCurrency: null!,
        annualTotalRevenue: null!,
        fixedAndWorkingCapital: null!,
      },
      productionSpecific: null!,
      productionSpecificOwnOperations: null!,
    },
    governance: null!,
    social: null!,
    environmental: null!,
  };
}
