import { generateLksgFixture } from "@e2e/fixtures/lksg/LksgDataFixtures";
import fs from "fs";
import { generateLksgPreparedFixtures } from "./LksgPreparedFixtures";

/**
 * Generates and exports fake fixtures for the LKSG framework
 */
export async function exportFixturesLksg(): Promise<void> {
  const companyInformationWithLksgData = await generateLksgFixture(150);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgData.json",
    JSON.stringify(companyInformationWithLksgData, null, "\t")
  );
  const preparedFixtureLksgDataForFinancials = await generateLksgPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgPreparedFixtures.json",
    JSON.stringify(preparedFixtureLksgDataForFinancials, null, "\t")
  );
}
