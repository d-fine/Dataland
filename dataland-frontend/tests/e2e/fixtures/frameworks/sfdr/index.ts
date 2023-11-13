import { type SfdrData } from "@clients/backend";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import fs from "fs";
import { generateSfdrData } from "./SfdrDataFixtures";
import { generateSfdrPreparedFixtures } from "./SfdrPreparedFixtures";
import { FAKE_FIXTURES_PER_FRAMEWORK } from "@e2e/fixtures/GenerateFakeFixtures";

/**
 * Generates and exports fake fixtures for the SFDR framework
 */
function exportFixturesSfdrData(): void {
  const companyInformationWithSfdrData = generateFixtureDataset<SfdrData>(
    generateSfdrData,
    FAKE_FIXTURES_PER_FRAMEWORK,
  );
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithSfdrData.json",
    JSON.stringify(companyInformationWithSfdrData, null, "\t"),
  );
  const preparedFixtureSfdrData = generateSfdrPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithSfdrPreparedFixtures.json",
    JSON.stringify(preparedFixtureSfdrData, null, "\t"),
  );
}

export default exportFixturesSfdrData;
