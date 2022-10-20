import { generateFixtureDataset } from "../FixtureUtils";
import { generateLksgData } from "./LksgDataFixtures";
import fs from "fs";
import { LksgData } from "../../../../build/clients/backend";

export function exportFixturesLksg() {
  const companyInformationWithLksgData = generateFixtureDataset<LksgData>(generateLksgData, 150);
  companyInformationWithLksgData[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithLksgData.json",
    JSON.stringify(companyInformationWithLksgData, null, "\t")
  );
}
