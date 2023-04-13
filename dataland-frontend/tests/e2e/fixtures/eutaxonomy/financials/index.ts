import { EuTaxonomyDataForFinancials } from "@clients/backend";
import {
  generateCSVDataForFinancials,
  generateEuTaxonomyDataForFinancials,
} from "./EuTaxonomyDataForFinancialsFixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import fs from "fs";
import { generateEuTaxonomyForFinancialsPreparedFixtures } from "./EuTaxonomyDataForFinancialsPreparedFixtures";

/**
 * Generates and exports fake fixtures for the eutaxonomy-financials framework
 */
export function exportFixturesEuTaxonomyFinancial(): void {
  const companyInformationWithEuTaxonomyDataForFinancials = generateFixtureDataset<EuTaxonomyDataForFinancials>(
    generateEuTaxonomyDataForFinancials,
    100
  );
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForFinancials, null, "\t")
  );
  fs.writeFileSync(
    "../testing/data/csvTestEuTaxonomyDataForFinancials.csv",
    generateCSVDataForFinancials(companyInformationWithEuTaxonomyDataForFinancials)
  );
  const preparedFixtureEuTaxonomyDataForFinancials = generateEuTaxonomyForFinancialsPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures.json",
    JSON.stringify(preparedFixtureEuTaxonomyDataForFinancials, null, "\t")
  );
}
