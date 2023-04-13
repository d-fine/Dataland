import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { generateSmeData } from "@e2e/fixtures/sme/SmeDataFixtures";
import fs from "fs";
import { SmeData } from "@clients/backend";

/**
 * Generates and exports fake fixtures for the SME framework
 */
export function exportFixturesSme(): void {
  const companyInformationWithSmeData = generateFixtureDataset<SmeData>(generateSmeData, 150);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithSmeData.json",
    JSON.stringify(companyInformationWithSmeData, null, "\t")
  );
}
