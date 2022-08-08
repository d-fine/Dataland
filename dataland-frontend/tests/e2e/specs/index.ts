require("./infrastructure");
require("./prepopulation");

const testGroupingDisabled = isNaN(Cypress.env("TEST_GROUP"));
let cypressTestGroup = undefined;
if (!testGroupingDisabled) {
  cypressTestGroup = parseInt(Cypress.env("TEST_GROUP"));
}

if (testGroupingDisabled) {
  console.log("Test grouping disabled. Loading all tests...");
} else {
  console.log(`Test grouping enabled. Loading tests for group ${cypressTestGroup}`);
}

if (testGroupingDisabled || cypressTestGroup === 1) {
  require("./company-metadata");
}

if (testGroupingDisabled || cypressTestGroup === 2) {
  require("./eu-taxonomy");
}

if (testGroupingDisabled || cypressTestGroup === 3) {
  require("./user-authentication");
}

if (testGroupingDisabled || cypressTestGroup === 4) {
  require("./landing-page");
  require("./skyminder-search");
  require("./swagger-ui");
}
