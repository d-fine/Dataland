import { generateLksgFixture } from "@e2e/fixtures/lksg/LksgDataFixtures";
import fs from "fs";
import { generateLksgPreparedFixtures } from "./LksgPreparedFixtures";

/**
 * Generates and exports fake fixtures for the LKSG framework
 */
export function exportFixturesLksg(): void {
  const companyInformationWithLksgData = generateLksgFixture(150);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgData.json",
    JSON.stringify(companyInformationWithLksgData, null, "\t")
  );
  const preparedFixtureLksgDataForFinancials = generateLksgPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgPreparedFixtures.json",
    JSON.stringify(preparedFixtureLksgDataForFinancials, null, "\t")
  );
}
