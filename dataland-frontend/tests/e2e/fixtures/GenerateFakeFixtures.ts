import { faker } from "@faker-js/faker";
import { exportFixturesEuTaxonomyFinancial } from "./eutaxonomy/financials";
import { exportFixturesEuTaxonomyNonFinancial } from "./eutaxonomy/non-financials";
import { exportFixturesLKSG } from "./lksg";
faker.locale = "de";

function main() {
  exportFixturesEuTaxonomyFinancial();
  exportFixturesEuTaxonomyNonFinancial();
  exportFixturesLKSG();
}

main();
