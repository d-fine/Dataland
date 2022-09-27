import { EuTaxonomyDataForFinancials } from "@clients/backend";
import {
  generateCSVDataForFinancials,
  generateEuTaxonomyDataForFinancials,
} from "./EuTaxonomyDataForFinancialsFixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import fs from "fs";

export function exportFixturesEuTaxonomyFinancial() {
  const companyInformationWithEuTaxonomyDataForFinancials = generateFixtureDataset<EuTaxonomyDataForFinancials>(
    generateEuTaxonomyDataForFinancials
  );
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForFinancials, null, "\t")
  );
  fs.writeFileSync(
    "../testing/data/csvTestEuTaxonomyDataForFinancials.csv",
    generateCSVDataForFinancials(companyInformationWithEuTaxonomyDataForFinancials)
  );
}
