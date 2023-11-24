import fs from "fs";
import { generateGdvFixtures } from "@e2e/fixtures/frameworks/gdv/GdvDataFixtures";
import { generateGdvPreparedFixtures } from "@e2e/fixtures/frameworks/gdv/GdvPreparedFixtures";
import { FAKE_FIXTURES_PER_FRAMEWORK } from "@e2e/fixtures/GenerateFakeFixtures";

/**
 * Generates and exports fake fixtures for the gdv framework
 */
function exportFixturesGdv(): void {
  const companyInformationWithGdvData = generateGdvFixtures(FAKE_FIXTURES_PER_FRAMEWORK);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithGdvData.json",
    JSON.stringify(companyInformationWithGdvData, null, "\t"),
  );
  const preparedFixtureGdvDataForFinancials = generateGdvPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithGdvPreparedFixtures.json",
    JSON.stringify(preparedFixtureGdvDataForFinancials, null, "\t"),
  );
}

export default exportFixturesGdv;
