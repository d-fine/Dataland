import { generateP2pFixture } from "@e2e/fixtures/p2p/P2pDataFixtures";
import fs from "fs";
import { generateP2pPreparedFixtures } from "@e2e/fixtures/p2p/P2pPreparedFixtures";

/**
 * Generates and exports fake fixtures for the P2p framework
 */
export function exportFixturesP2p(): void {
  const probability = 0.5;
  const companyInformationWithP2pData = generateP2pFixture(150, probability);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithP2pData.json",
    JSON.stringify(companyInformationWithP2pData, null, "\t")
  );
  const preparedFixtureP2pDataForFinancials = generateP2pPreparedFixtures(probability, false);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithP2pPreparedFixtures.json",
    JSON.stringify(preparedFixtureP2pDataForFinancials, null, "\t")
  );
}
