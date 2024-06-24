import { type EuTaxonomyDataForFinancials } from '@clients/backend';
import { generateEuTaxonomyDataForFinancials } from './EuTaxonomyDataForFinancialsFixtures';
import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import fs from 'fs';
import { generateEuTaxonomyForFinancialsPreparedFixtures } from './EuTaxonomyDataForFinancialsPreparedFixtures';
import { FAKE_FIXTURES_PER_FRAMEWORK } from '@e2e/fixtures/GenerateFakeFixtures';

/**
 * Generates and exports fake fixtures for the eutaxonomy-financials framework
 */
function exportFixturesEuTaxonomyFinancial(): void {
  const companyInformationWithEuTaxonomyDataForFinancials = generateFixtureDataset<EuTaxonomyDataForFinancials>(
    generateEuTaxonomyDataForFinancials,
    FAKE_FIXTURES_PER_FRAMEWORK
  );
  fs.writeFileSync(
    '../testing/data/CompanyInformationWithEuTaxonomyDataForFinancials.json',
    JSON.stringify(companyInformationWithEuTaxonomyDataForFinancials, null, '\t')
  );
  const preparedFixtureEuTaxonomyDataForFinancials = generateEuTaxonomyForFinancialsPreparedFixtures();
  fs.writeFileSync(
    '../testing/data/CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures.json',
    JSON.stringify(preparedFixtureEuTaxonomyDataForFinancials, null, '\t')
  );
}

export default exportFixturesEuTaxonomyFinancial;
