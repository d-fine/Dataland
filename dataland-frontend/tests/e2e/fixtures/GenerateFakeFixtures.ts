import { faker } from "@faker-js/faker";

import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
} from "../../../build/clients/backend/api";

import { generateCompanyInformation } from "./CompanyFixtures";
import {
  generateCSVDataForNonFinancials,
  generateEuTaxonomyDataForNonFinancials,
} from "./EuTaxonomyDataForNonFinancialsFixtures";
import {
  generateCSVDataForFinancials,
  generateEuTaxonomyDataForFinancials,
} from "./EuTaxonomyDataForFinancialsFixtures";

const { parse } = require("json2csv");
const fs = require("fs");

faker.locale = "de";

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
}

function generateFixtureDataset<T>(generator: () => T): Array<FixtureData<T>> {
  const fixtureDataset = [];
  for (let id = 1; id <= 250; id++) {
    fixtureDataset.push({
      companyInformation: generateCompanyInformation(),
      t: generator(),
    });
  }
  return fixtureDataset;
}

function exportFixturesEuTaxonomyNonFinancial() {
  const companyInformationWithEuTaxonomyDataForNonFinancials = generateFixtureDataset<EuTaxonomyDataForNonFinancials>(
    generateEuTaxonomyDataForNonFinancials
  );
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForNonFinancials, null, "\t")
  );
  fs.writeFileSync(
    "../testing/data/csvTestEuTaxonomyDataForNonFinancials.csv",
    generateCSVDataForNonFinancials(companyInformationWithEuTaxonomyDataForNonFinancials)
  );
}

function exportFixturesEuTaxonomyFinancial() {
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

function main() {
  exportFixturesEuTaxonomyNonFinancial();
  exportFixturesEuTaxonomyFinancial();
}

main();
