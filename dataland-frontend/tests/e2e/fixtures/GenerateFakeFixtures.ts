import { faker } from "@faker-js/faker";
import { exportFixturesEuTaxonomyFinancial } from "./eutaxonomy/financials";
import { exportFixturesEuTaxonomyNonFinancial } from "./eutaxonomy/non-financials";
import { exportFixturesLksg } from "./lksg";
import { exportFixturesSfdrData } from "./sfdr";
faker.locale = "de";

function main(): void {
  exportFixturesEuTaxonomyFinancial();
  exportFixturesEuTaxonomyNonFinancial();
  exportFixturesLksg();
  exportFixturesSfdrData();
}

main();
