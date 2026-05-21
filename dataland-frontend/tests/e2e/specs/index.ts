const testGroupingDisabled = Cypress.expose('TEST_GROUP') == null || Number.isNaN(Number(Cypress.expose('TEST_GROUP')));

let cypressTestGroup: number | undefined;
if (!testGroupingDisabled) {
  cypressTestGroup = Number.parseInt(String(Cypress.expose('TEST_GROUP')));
}

const singlePopulate = !testGroupingDisabled && Cypress.expose('SINGLE_POPULATE') === true;
const runPrepopulation = Cypress.expose('RUN_PREPOPULATION') !== false;

if (testGroupingDisabled) {
  console.log('Test grouping disabled. Loading all tests...');
} else {
  console.log(`Test grouping enabled. Loading tests for group ${String(cypressTestGroup)}`);
}

/**
 * Test grouping overview
 * 1 - 8      : Traditional E2E-Tests
 * 101 - 102  : Restartability E2E-Tests
 */

require('./infrastructure');

if (runPrepopulation) {
  if (!singlePopulate || cypressTestGroup === 1 || cypressTestGroup === 101) {
    require('./prepopulation');
  } else {
    require('./prepopulation/AwaitPrepopulation');
  }
}

if (testGroupingDisabled || cypressTestGroup === 1) {
  require('./landing-page');
  require('./swagger-ui');
  require('./user-api-key');
  require('./sfdr');
  require('./lksg');
}

if (testGroupingDisabled || cypressTestGroup === 2) {
  require('./framework-view-and-upload-journey');
  require('./company-cockpit');
}

if (testGroupingDisabled || cypressTestGroup === 3) {
  /**
   * user-authentication and admin-tools both need the admin tunnel to be present.
   * That's why they live together.
   */

  require('./nuclear-and-gas');
  require('./user-authentication');
  require('./admin-tools');
}

if (testGroupingDisabled || cypressTestGroup === 4) {
  require('./search-company-framework-data');
}

if (testGroupingDisabled || cypressTestGroup === 5) {
  require('./quality-assurance');
}

if (testGroupingDisabled || cypressTestGroup === 6) {
  require('./data-download/DataDownload');
  require('./data-download/CompanyReportDownload');
}

if (testGroupingDisabled || cypressTestGroup === 7) {
  require('./data-download/DownloadMyPortfolios');
}

if (testGroupingDisabled || cypressTestGroup === 8) {
  require('./eu-taxonomy-financials');
  require('./eutaxonomy-financials-2026-73');
  require('./eu-taxonomy-non-financials');
  require('./eutaxonomy-non-financials-2026-73');
  require('./vsme');
  require('./pcaf');
  require('./company-ownership');
  require('./user-experience');
  require('./portfolios');
  require('./data-sourcing');
  require('./judgement');
}
