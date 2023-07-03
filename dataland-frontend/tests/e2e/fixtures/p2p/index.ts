import { generateP2pFixture } from "@e2e/fixtures/p2p/P2pDataFixtures";
import fs from "fs";

/**
 * Generates and exports fake fixtures for the P2p framework
 */
export function exportFixturesP2p(): void {
  const companyInformationWithP2pData = generateP2pFixture(150);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithP2pData.json",
    JSON.stringify(companyInformationWithP2pData, null, "\t")
  );
}
