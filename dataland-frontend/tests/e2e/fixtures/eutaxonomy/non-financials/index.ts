import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import {
  generateCSVDataForNonFinancials,
  generateEuTaxonomyDataForNonFinancials,
} from "./EuTaxonomyDataForNonFinancialsFixtures";
import fs from "fs";
import { generateEuTaxonomyForNonFinancialsPreparedFixtures } from "../non-financials/EuTaxonomyDataForNonFinancialsPreparedFixtures";

export function exportFixturesEuTaxonomyNonFinancial(): void {
  const companyInformationWithEuTaxonomyDataForNonFinancials = generateFixtureDataset<EuTaxonomyDataForNonFinancials>(
    generateEuTaxonomyDataForNonFinancials,
    150
  );
  companyInformationWithEuTaxonomyDataForNonFinancials[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForNonFinancials, null, "\t")
  );
  fs.writeFileSync(
    "../testing/data/csvTestEuTaxonomyDataForNonFinancials.csv",
    generateCSVDataForNonFinancials(companyInformationWithEuTaxonomyDataForNonFinancials)
  );
  const preparedFixtureEuTaxonomyDataForNonFinancials = generateEuTaxonomyForNonFinancialsPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures.json",
    JSON.stringify(preparedFixtureEuTaxonomyDataForNonFinancials, null, "\t")
  );
}
