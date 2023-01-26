import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { generateLksgData, generateProductionSite } from "./LksgDataFixtures";

type generatorFunction = (input: FixtureData<LksgData>) => FixtureData<LksgData>;

/**
 * Generates LkSG prepared fixtures by generating random LkSG datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 *
 * @returns the prepared fixtures
 */
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const manipulatorFunctions: Array<generatorFunction> = [
    manipulateFixtureForTwoLksgDataSetsInSameYear,
    manipulateFixtureForSixLksgDataSetsInDifferentYears,
    manipulateFixtureForOneLksgDataSetWithProductionSites,
    manipulateFixtureForTwoDifferentDataSetTypes,
  ];
  const preparedFixturesBeforeManipulation = generateFixtureDataset<LksgData>(
    generateLksgData,
    manipulatorFunctions.length
  );
  const preparedFixtures = [];
  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  return preparedFixtures;
}

/**
 * Sets the company name and the date in the fixture data to a specific string.
 *
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForTwoLksgDataSetsInSameYear(input: FixtureData<LksgData>): FixtureData<LksgData> {
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
function manipulateFixtureForSixLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
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
function manipulateFixtureForOneLksgDataSetWithProductionSites(input: FixtureData<LksgData>): FixtureData<LksgData> {
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
function manipulateFixtureForTwoDifferentDataSetTypes(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "two-different-data-set-types";
  return input;
}
