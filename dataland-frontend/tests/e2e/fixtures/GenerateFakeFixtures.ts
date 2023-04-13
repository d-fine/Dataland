import { faker } from "@faker-js/faker";
import { exportFixturesEuTaxonomyFinancial } from "./eutaxonomy/financials";
import { exportFixturesEuTaxonomyNonFinancial } from "./eutaxonomy/non-financials";
import { exportFixturesLksg } from "./lksg";
import { exportFixturesSfdrData } from "./sfdr";
import { exportFixturesSme } from "./sme";
faker.locale = "de";

/**
 * The main entrypoint of the fake fixture generator
 */
function main(): void {
  exportFixturesEuTaxonomyFinancial();
  exportFixturesEuTaxonomyNonFinancial();
  exportFixturesLksg();
  exportFixturesSfdrData();
  exportFixturesSme();
}

main();
