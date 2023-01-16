import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { generateLksgData } from "./LksgDataFixtures";

type generatorFunction = (input: FixtureData<LksgData>) => FixtureData<LksgData>;

export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const creationFunctions: Array<generatorFunction> = [
    createCompanyToHaveTwoLksgDataSetsInSameYear,
    createCompanyToHaveTwoLksgDataSetsInDifferentYears,
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
  input.t.social!!.general!!.dataDate = "2022-01-01";
  return input;
}

function createCompanyToHaveTwoLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "two-lksg-data-sets-in-different-years";
  input.t.social!!.general!!.dataDate = "2022-01-01";
  return input;
}

function createCompanyToHaveSixLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "six-lksg-data-sets-in-different-years";
  input.t.social!!.general!!.dataDate = "2022-01-01";
  return input;
}

function createCompanyToHaveOneLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "one-lksg-data-set";
  return input;
}

function createCompanyToHaveTwoDifferentDataSetTypes(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "two-different-data-set-types";
  return input;
}
