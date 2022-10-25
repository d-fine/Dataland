import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import fs from "fs";
import { LksgData } from "@clients/backend";

export function exportFixturesLksg(): void {
  const companyInformationWithLksgData = generateFixtureDataset<LksgData>(generateLksgData, 150);
  companyInformationWithLksgData[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgData.json",
    JSON.stringify(companyInformationWithLksgData, null, "\t")
  );
}
