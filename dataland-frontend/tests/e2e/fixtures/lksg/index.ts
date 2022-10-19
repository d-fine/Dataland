import { LKSGData } from "../../../../build/clients/backend";
import { generateFixtureDataset } from "../FixtureUtils";
import { generateLKSGData } from "../lksg/LKSGdataFixtures";
import fs from "fs";

export function exportFixturesLKSG() {
  const companyInformationWithLKSGData = generateFixtureDataset<LKSGData>(generateLKSGData, 150);
  companyInformationWithLKSGData[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLKSGData.json",
    JSON.stringify(companyInformationWithLKSGData, null, "\t")
  );
}
