import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import {
  generateCSVDataForNonFinancials,
  generateEuTaxonomyDataForNonFinancials,
} from "./EuTaxonomyDataForNonFinancialsFixtures";
import fs from "fs";
import { generateEuTaxonomyForNonFinancialsPreparedFixtures } from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsPreparedFixtures";

/**
 * Generates and exports fake fixtures for the eutaxonomy-non-financials framework
 */
export async function exportFixturesEuTaxonomyNonFinancial(): Promise<void> {
  const companyInformationWithEuTaxonomyDataForNonFinancials =
    await generateFixtureDataset<EuTaxonomyDataForNonFinancials>(generateEuTaxonomyDataForNonFinancials, 150);
  companyInformationWithEuTaxonomyDataForNonFinancials[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForNonFinancials, null, "\t")
  );
  fs.writeFileSync(
    "../testing/data/csvTestEuTaxonomyDataForNonFinancials.csv",
    generateCSVDataForNonFinancials(companyInformationWithEuTaxonomyDataForNonFinancials)
  );
  const preparedFixtureEuTaxonomyDataForNonFinancials = await generateEuTaxonomyForNonFinancialsPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures.json",
    JSON.stringify(preparedFixtureEuTaxonomyDataForNonFinancials, null, "\t")
  );
}
