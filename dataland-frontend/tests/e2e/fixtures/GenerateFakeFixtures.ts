import { faker } from "@faker-js/faker";
import { exportFixturesEuTaxonomyFinancial } from "./eutaxonomy/financials";
import { exportFixturesEuTaxonomyNonFinancial } from "./eutaxonomy/non-financials";
faker.locale = "de";

function main() {
  exportFixturesEuTaxonomyFinancial();
  exportFixturesEuTaxonomyNonFinancial();
}

main();
