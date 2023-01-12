import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { generateLksgData } from "./LksgDataFixtures";

type generatorFunction = (input: FixtureData<LksgData>) => FixtureData<LksgData>;

export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const creationFunctions: Array<generatorFunction> = [createCompanyToHaveTwoDataSets];
  const fixtureBase = generateFixtureDataset<LksgData>(
    generateLksgData,
    creationFunctions.length
  );
  const preparedFixtures = [];
  for (let i = 0; i < creationFunctions.length; i++) {
    preparedFixtures.push(creationFunctions[i](fixtureBase[i]));
  }
  return preparedFixtures;
}

function createCompanyToHaveTwoDataSets(
  input: FixtureData<LksgData>
): FixtureData<LksgData> {
  input.companyInformation.companyName = "two-lksg-data-sets";
  return input;
}
