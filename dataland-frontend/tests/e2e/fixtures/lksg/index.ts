import { generateFixtureDataset } from "../FixtureUtils";
import { generateLKSGData } from "../lksg/LKSGdataFixtures";
import fs from "fs";
import { LksgData } from "../../../../build/clients/backend";

export function exportFixturesLKSG() {
  const companyInformationWithLKSGData = generateFixtureDataset<LksgData>(generateLKSGData, 150);
  companyInformationWithLKSGData[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLKSGData.json",
    JSON.stringify(companyInformationWithLKSGData, null, "\t")
  );
}
