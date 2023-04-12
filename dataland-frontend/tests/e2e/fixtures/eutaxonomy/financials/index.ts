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
export async function exportFixturesEuTaxonomyFinancial(): Promise<void> {
  const companyInformationWithEuTaxonomyDataForFinancials = await generateFixtureDataset<EuTaxonomyDataForFinancials>(
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
  const preparedFixtureEuTaxonomyDataForFinancials = await generateEuTaxonomyForFinancialsPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures.json",
    JSON.stringify(preparedFixtureEuTaxonomyDataForFinancials, null, "\t")
  );
}
