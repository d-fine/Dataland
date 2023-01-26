import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { generateLksgData, generateProductionSite } from "./LksgDataFixtures";

type generatorFunction = (input: FixtureData<LksgData>) => FixtureData<LksgData>;

// TODO not fully happy with the namings here.  those aren't generatorFunctions, they are setterFunctions
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const creationFunctions: Array<generatorFunction> = [
    createCompanyToHaveTwoLksgDataSetsInSameYear,
    createCompanyToHaveSixLksgDataSetsInDifferentYears,
    createCompanyToHaveOneLksgDataSetsInDifferentYears,
    createCompanyToHaveTwoDifferentDataSetTypes,
  ];
  const fixtureBase = generateFixtureDataset<LksgData>(generateLksgData, creationFunctions.length);
  const preparedFixtures = [];
  for (let i = 0; i < creationFunctions.length; i++) {
    preparedFixtures.push(creationFunctions[i](fixtureBase[i]));
  }
  return preparedFixtures;
}

function createCompanyToHaveTwoLksgDataSetsInSameYear(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "two-lksg-data-sets-in-same-year";
  input.t.social!.general!.dataDate = "2022-01-01";
  return input;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 *
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function createCompanyToHaveSixLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "six-lksg-data-sets-in-different-years";
  input.t.social!.general!.dataDate = "2022-01-01";
  return input;
}

/**
 * Sets the company name in the fixture data to a specific string, the field "employeeUnder18Apprentices" to "No", and
 * sets exactly two production sites for the "listOfProductionSites" field.
 *
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function createCompanyToHaveOneLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "one-lksg-data-set";
  input.t.social!.childLabour!.employeeUnder18Apprentices = "No";
  input.t.social!.general!.listOfProductionSites = [generateProductionSite(), generateProductionSite()];
  return input;
}

/**
 * Sets the company name in the fixture data to a specific string
 *
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function createCompanyToHaveTwoDifferentDataSetTypes(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "two-different-data-set-types";
  return input;
}

// TODO I suggest renaming all the functions in this class, and also the paramNames.  It's strange to read.
