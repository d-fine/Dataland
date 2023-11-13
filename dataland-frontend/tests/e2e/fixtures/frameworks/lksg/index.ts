import { generateLksgFixtures } from "@e2e/fixtures/frameworks/lksg/LksgDataFixtures";
import fs from "fs";
import { generateLksgPreparedFixtures } from "./LksgPreparedFixtures";
import { FAKE_FIXTURES_PER_FRAMEWORK } from "@e2e/fixtures/GenerateFakeFixtures";

/**
 * Generates and exports fake fixtures for the LKSG framework
 */
function exportFixturesLksg(): void {
  const companyInformationWithLksgData = generateLksgFixtures(FAKE_FIXTURES_PER_FRAMEWORK);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgData.json",
    JSON.stringify(companyInformationWithLksgData, null, "\t"),
  );
  const preparedFixtureLksgDataForFinancials = generateLksgPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgPreparedFixtures.json",
    JSON.stringify(preparedFixtureLksgDataForFinancials, null, "\t"),
  );
}

export default exportFixturesLksg;
