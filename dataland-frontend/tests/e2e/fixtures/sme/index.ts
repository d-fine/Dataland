import { generateSmeFixtures } from "@e2e/fixtures/sme/SmeDataFixtures";
import fs from "fs";
import { generateSmePreparedFixtures } from "@e2e/fixtures/sme/SmePreparedFixtures";
import { FAKE_FIXTURES_PER_FRAMEWORK } from "@e2e/fixtures/GenerateFakeFixtures";

/**
 * Generates and exports fake fixtures for the SME framework
 */
export function exportFixturesSme(): void {
  const companyInformationWithSmeData = generateSmeFixtures(FAKE_FIXTURES_PER_FRAMEWORK);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithSmeData.json",
    JSON.stringify(companyInformationWithSmeData, null, "\t"),
  );
  const preparedSmeFixtureData = generateSmePreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithSmePreparedFixtures.json",
    JSON.stringify(preparedSmeFixtureData, null, "\t"),
  );
}
