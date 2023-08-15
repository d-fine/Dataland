import { NewEuTaxonomyDataForNonFinancials } from "@clients/backend";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import {
  generateNewEuTaxonomyDataForNonFinancials
} from "./NewEuTaxonomyDataForNonFinancialsFixtures";
import fs from "fs";
import {
  generateNewEuTaxonomyForNonFinancialsPreparedFixtures
} from "@e2e/fixtures/eutaxonomy/new-non-financials/NewEuTaxonomyDataForNonFinancialsPreparedFixtures";

/**
 * Generates and exports fake fixtures for the eutaxonomy-non-financials framework
 */
export function exportFixturesNewEuTaxonomyForNonFinancials(): void {
  const companyInformationWithNewEuTaxonomyDataForNonFinancials = generateFixtureDataset<NewEuTaxonomyDataForNonFinancials>(
    generateNewEuTaxonomyDataForNonFinancials,
    150,
  );
  companyInformationWithNewEuTaxonomyDataForNonFinancials[0].companyInformation.isTeaserCompany = true;
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithNewEuTaxonomyDataForNonFinancials.json",
    JSON.stringify(companyInformationWithNewEuTaxonomyDataForNonFinancials, null, "\t"),
  );
  const preparedFixtureNewEuTaxonomyDataForNonFinancials = generateNewEuTaxonomyForNonFinancialsPreparedFixtures();
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithNewEuTaxonomyDataForNonFinancialsPreparedFixtures.json",
    JSON.stringify(preparedFixtureNewEuTaxonomyDataForNonFinancials, null, "\t"),
  );
}
