import { type EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { generateEuTaxonomyDataForNonFinancials } from "./EuTaxonomyDataForNonFinancialsFixtures";
import fs from "fs";
import { generateEuTaxonomyForNonFinancialsPreparedFixtures } from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsPreparedFixtures";
import { FAKE_FIXTURES_PER_FRAMEWORK } from "@e2e/fixtures/GenerateFakeFixtures";

/**
 * Generates and exports fake fixtures for the eutaxonomy-non-financials framework
 */
export function exportFixturesEuTaxonomyNonFinancial(): void {
  const companyInformationWithEuTaxonomyDataForNonFinancials = generateFixtureDataset<EuTaxonomyDataForNonFinancials>(
    generateEuTaxonomyDataForNonFinancials,
    FAKE_FIXTURES_PER_FRAMEWORK,
  );
  companyInformationWithEuTaxonomyDataForNonFinancials[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForNonFinancials, null, "\t"),
  );
  const preparedFixtureEuTaxonomyDataForNonFinancials = generateEuTaxonomyForNonFinancialsPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures.json",
    JSON.stringify(preparedFixtureEuTaxonomyDataForNonFinancials, null, "\t"),
  );
}
