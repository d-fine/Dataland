const testGroupingDisabled = isNaN(Cypress.env("TEST_GROUP") as number);
let cypressTestGroup = undefined;
if (!testGroupingDisabled) {
  cypressTestGroup = parseInt(Cypress.env("TEST_GROUP") as string);
}

const singlePopulate = !testGroupingDisabled && Cypress.env("SINGLE_POPULATE") === true;
const runPrepopulation = Cypress.env("RUN_PREPOPULATION") !== false;

if (testGroupingDisabled) {
  console.log("Test grouping disabled. Loading all tests...");
} else {
  console.log(`Test grouping enabled. Loading tests for group ${String(cypressTestGroup)}`);
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
  require("./landing-page");
  require("./swagger-ui");
  require("./company-metadata/SearchCompaniesForFrameworkDataDropdownFilter");
  require("./company-metadata/CompaniesOnlySearch");
  require("./user-api-key");
}

if (cypressTestGroup === 102) {
  require("./company-metadata/CompaniesOnlySearch");
}

if (testGroupingDisabled || cypressTestGroup === 2) {
  require("./frameworks");
  require("./eu-taxonomy-non-financials");
  require("./eu-taxonomy-financials");
  require("./lksg");
}

if (testGroupingDisabled || cypressTestGroup === 3) {
  /**
   * user-authentication and admin-tools both need the admin tunnel to be present.
   * That's why they live together.
   */
  require("./user-authentication");
  require("./admin-tools");
}

if (testGroupingDisabled || cypressTestGroup === 4) {
  require("./company-metadata/SearchPagination");
  require("./company-metadata/CompanyUpload");
  require("./company-metadata/FrameworkDataUpload");
  require("./company-metadata/SearchCompaniesForFrameworkData");
  require("./data-request/DataRequestProcess");
}
