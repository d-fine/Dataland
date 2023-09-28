import { exportFixturesEuTaxonomyFinancial } from "./eutaxonomy/financials";
import { exportFixturesEuTaxonomyNonFinancial } from "./eutaxonomy/non-financials";
import { exportFixturesLksg } from "./lksg";
import { exportFixturesSfdrData } from "./sfdr";
import { exportFixturesSme } from "./sme";
import { exportFixturesP2p } from "@e2e/fixtures/p2p";
import { exportServerResponses } from "@e2e/fixtures/backend_responses";

/**
 * The main entrypoint of the fake fixture generator
 */
function main(): void {
  exportFixturesEuTaxonomyFinancial();
  exportFixturesEuTaxonomyNonFinancial();
  exportFixturesLksg();
  exportFixturesSfdrData();
  exportFixturesSme();
  exportFixturesP2p();

  exportServerResponses();
}

main();
