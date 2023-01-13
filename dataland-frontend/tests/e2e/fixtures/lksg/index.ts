import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import fs from "fs";
import { LksgData } from "@clients/backend";
import { generateLksgPreparedFixtures } from "./LksgPreparedFixtures";

export function exportFixturesLksg(): void {
  const companyInformationWithLksgData = generateFixtureDataset<LksgData>(generateLksgData, 150);
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
