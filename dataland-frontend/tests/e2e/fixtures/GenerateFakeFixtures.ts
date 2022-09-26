import { faker } from "@faker-js/faker";

import { CompanyInformation, EuTaxonomyDataForNonFinancials, EuTaxonomyDataForFinancials } from "@clients/backend";

import { generateCompanyInformation } from "./CompanyFixtures";
import {
  generateCSVDataForNonFinancials,
  generateEuTaxonomyDataForNonFinancials,
} from "./EuTaxonomyDataForNonFinancialsFixtures";
import {
  generateCSVDataForFinancials,
  generateEuTaxonomyDataForFinancials,
} from "./EuTaxonomyDataForFinancialsFixtures";

const fs = require("fs");

faker.locale = "de";

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
}

function generateFixtureDataset<T>(dataPoints: number, generator: () => T): Array<FixtureData<T>> {
  const fixtureDataset = [];
  for (let id = 1; id <= dataPoints; id++) {
    fixtureDataset.push({
      companyInformation: generateCompanyInformation(),
      t: generator(),
    });
  }
  return fixtureDataset;
}

function exportFixturesEuTaxonomyNonFinancial() {
  const companyInformationWithEuTaxonomyDataForNonFinancials = generateFixtureDataset<EuTaxonomyDataForNonFinancials>(150,
    generateEuTaxonomyDataForNonFinancials
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

function exportFixturesEuTaxonomyNonFinancialCornerCases() {
  const companyInformationWithEuTaxonomyDataForNonFinancials = generateFixtureDataset<EuTaxonomyDataForNonFinancials>(1,
      generateEuTaxonomyDataForNonFinancials
  );

  companyInformationWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName = "DALA-357";
  companyInformationWithEuTaxonomyDataForNonFinancials[0].t.reportingObligation = "No";
  companyInformationWithEuTaxonomyDataForNonFinancials[0].t.opex = { totalAmount: 5000, alignedPercentage: 2.1, eligiblePercentage: 0.5 }
  fs.writeFileSync(
      "../testing/data/csvTestEuTaxonomyDataForNonFinancialsPreviousIssues.csv",
      generateCSVDataForNonFinancials(companyInformationWithEuTaxonomyDataForNonFinancials)
  );
}

function exportFixturesEuTaxonomyFinancial() {
  const companyInformationWithEuTaxonomyDataForFinancials = generateFixtureDataset<EuTaxonomyDataForFinancials>(100,
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

function main() {
  exportFixturesEuTaxonomyNonFinancial();
  exportFixturesEuTaxonomyNonFinancialCornerCases();
  exportFixturesEuTaxonomyFinancial();
}

main();
