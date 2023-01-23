import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import fs from "fs";
import { LksgData } from "@clients/backend";

/**
 * Generates and exports fake fixtures for the LKSG framework
 */
export function exportFixturesLksg(): void {
  const companyInformationWithLksgData = generateFixtureDataset<LksgData>(generateLksgData, 150);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgData.json",
    JSON.stringify(companyInformationWithLksgData, null, "\t")
  );
}
