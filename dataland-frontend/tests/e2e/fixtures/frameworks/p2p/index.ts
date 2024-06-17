import { generateP2pFixtures } from '@e2e/fixtures/frameworks/p2p/P2pDataFixtures';
import fs from 'fs';
import { generateP2pPreparedFixtures } from '@e2e/fixtures/frameworks/p2p/P2pPreparedFixtures';
import { FAKE_FIXTURES_PER_FRAMEWORK } from '@e2e/fixtures/GenerateFakeFixtures';

/**
 * Generates and exports fake fixtures for the P2p framework
 */
function exportFixturesP2p(): void {
  const probability = 0.5;
  const companyInformationWithP2pData = generateP2pFixtures(FAKE_FIXTURES_PER_FRAMEWORK, probability);
  fs.writeFileSync(
    '../testing/data/CompanyInformationWithP2pData.json',
    JSON.stringify(companyInformationWithP2pData, null, '\t')
  );
  const preparedFixtureP2pDataForFinancials = generateP2pPreparedFixtures(probability, false);
  fs.writeFileSync(
    '../testing/data/CompanyInformationWithP2pPreparedFixtures.json',
    JSON.stringify(preparedFixtureP2pDataForFinancials, null, '\t')
  );
}

export default exportFixturesP2p;
