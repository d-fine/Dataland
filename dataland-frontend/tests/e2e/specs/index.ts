const testGroupingDisabled = isNaN(Cypress.env("TEST_GROUP"));
let cypressTestGroup = undefined;
if (!testGroupingDisabled) {
  cypressTestGroup = parseInt(Cypress.env("TEST_GROUP"));
}

const singlePopulate = !testGroupingDisabled && Cypress.env("SINGLE_POPULATE") === true;
const runPrepopulation = Cypress.env("RUN_PREPOPULATION") !== false;

if (testGroupingDisabled) {
  console.log("Test grouping disabled. Loading all tests...");
} else {
  console.log(`Test grouping enabled. Loading tests for group ${cypressTestGroup}`);
}

/**
 * Test grouping overview
 * 1 - 4      : Traditional E2E-Tests
 * 101 - 102  : Restartability E2E-Tests
 */

require("./infrastructure");

if (runPrepopulation) {
  if (!singlePopulate || cypressTestGroup === 1 || cypressTestGroup === 101) {
    require("./prepopulation");
  } else {
    require("./prepopulation/AwaitPrepopulation");
  }
}

if (testGroupingDisabled || cypressTestGroup === 1) {
  require("./company-metadata");
}

if (testGroupingDisabled || cypressTestGroup === 2) {
  require("./eu-taxonomy-non-financials");
  require("./eu-taxonomy-financials");
}

if (testGroupingDisabled || cypressTestGroup === 3) {
  require("./user-authentication");
  require("./admin-tools");
}

if (testGroupingDisabled || cypressTestGroup === 4) {
  require("./landing-page");
  require("./skyminder-search");
  require("./swagger-ui");
}
