// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
import fs from "fs";
import { generateVsmeFixtures } from "@e2e/fixtures/frameworks/vsme/VsmeDataFixtures";
import { generateVsmePreparedFixtures } from "@e2e/fixtures/frameworks/vsme/VsmePreparedFixtures";
import { FAKE_FIXTURES_PER_FRAMEWORK } from "@e2e/fixtures/GenerateFakeFixtures";

/**
 * Generates and exports fake fixtures for the vsme framework
 */
function exportFixturesVsme(): void {
  const companyInformationWithVsmeData = generateVsmeFixtures(FAKE_FIXTURES_PER_FRAMEWORK);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithVsmeData.json",
    JSON.stringify(companyInformationWithVsmeData, null, "\t"),
  );
  const preparedFixtureVsmeData = generateVsmePreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithVsmePreparedFixtures.json",
    JSON.stringify(preparedFixtureVsmeData, null, "\t"),
  );
}

export default exportFixturesVsme;