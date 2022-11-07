import { SfdrData } from "@clients/backend";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import fs from "fs";
import { generateSfdrData } from "./SfdrDataFixtures";

export function exportFixturesSfdrData(): void {
  const companyInformationWithSfdrData = generateFixtureDataset<SfdrData>(generateSfdrData, 100);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithSfdrData.json",
    JSON.stringify(companyInformationWithSfdrData, null, "\t")
  );
}
