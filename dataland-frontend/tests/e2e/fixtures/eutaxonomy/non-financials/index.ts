import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import {
  generateCSVDataForNonFinancials,
  generateEuTaxonomyDataForNonFinancials,
} from "./EuTaxonomyDataForNonFinancialsFixtures";
import fs from "fs";

export function exportFixturesEuTaxonomyNonFinancial() {
  const companyInformationWithEuTaxonomyDataForNonFinancials = generateFixtureDataset<EuTaxonomyDataForNonFinancials>(
    generateEuTaxonomyDataForNonFinancials,
    250
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
}
