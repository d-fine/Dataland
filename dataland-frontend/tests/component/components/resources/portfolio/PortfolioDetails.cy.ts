import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import { type EnrichedPortfolio } from '@clients/userservice';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE } from '@/utils/Constants.ts';

const userId = '9bba9b59-c1ab-48f2-be92-196c5ea83d5f';
const companyId = '43bc3dab-a612-4b1b-9cd7-4e304c7ba580';
const datalandMemberInheritedRoleMap = {
  [companyId]: ['DatalandMember'],
};

interface ConfigurationParameters {
  inheritedRoleMap: { [p: string]: string[] };
  keycloakRoles: string[];
  portfolioResponse: EnrichedPortfolio;
}

let nonMemberConfigurationParameters: ConfigurationParameters;
let memberConfigurationParametersWithoutMonitoring: ConfigurationParameters;
let memberConfigurationParametersWithMonitoring: ConfigurationParameters;
let adminConfigurationParametersWithoutMonitoring: ConfigurationParameters;
let adminConfigurationParametersWithMonitoring: ConfigurationParameters;
let largePortfolioConfigurationParameters: ConfigurationParameters;

describe('Check the portfolio details view', function (): void {
  let portfolioFixtureWithoutMonitoring: EnrichedPortfolio;
  let portfolioFixtureWithMonitoring: EnrichedPortfolio;
  let largePortfolioFixture: EnrichedPortfolio;

  /**
   * Intercepts the API calls for inherited roles and portfolio download, mounts the PortfolioDetails component,
   * and waits for the portfolio download to complete.
   * @param configurationParameters parameters for configuring the test scenario
   * @returns A Cypress.Chainable that resolves when the portfolio download is complete
   */
  function interceptApiCallsAndMountAndWaitForDownload(
    configurationParameters: ConfigurationParameters
  ): Cypress.Chainable {
    cy.intercept(`**/inherited-roles/${userId}`, configurationParameters.inheritedRoleMap).as(
      'inheritedRolesRetrieved'
    );
    cy.intercept('**/users/portfolios/*/enriched-portfolio', configurationParameters.portfolioResponse).as(
      'downloadComplete'
    );

    return (
      cy
        // @ts-ignore
        .mountWithPlugins(PortfolioDetails, {
          keycloak: minimalKeycloakMock({
            userId: userId,
          }),
          props: { portfolioId: configurationParameters.portfolioResponse.portfolioId },
        })
        .then(() => cy.wait('@downloadComplete'))
    );
  }

  before(function () {
    cy.fixture('enrichedPortfolio.json')
      .then(function (jsonContent) {
        portfolioFixtureWithoutMonitoring = jsonContent as EnrichedPortfolio;
      })
      .then(() => {
        nonMemberConfigurationParameters = {
          inheritedRoleMap: {},
          keycloakRoles: ['ROLE_USER'],
          portfolioResponse: portfolioFixtureWithoutMonitoring,
        };
        memberConfigurationParametersWithoutMonitoring = {
          inheritedRoleMap: datalandMemberInheritedRoleMap,
          keycloakRoles: ['ROLE_USER'],
          portfolioResponse: portfolioFixtureWithoutMonitoring,
        };
        portfolioFixtureWithMonitoring = {
          ...portfolioFixtureWithoutMonitoring,
          isMonitored: true,
          monitoredFrameworks: new Set(['sfdr', 'eutaxonomy']),
        } as EnrichedPortfolio;
        memberConfigurationParametersWithMonitoring = {
          inheritedRoleMap: datalandMemberInheritedRoleMap,
          keycloakRoles: ['ROLE_USER'],
          portfolioResponse: portfolioFixtureWithMonitoring,
        };
        adminConfigurationParametersWithoutMonitoring = {
          inheritedRoleMap: {},
          keycloakRoles: ['ROLE_ADMIN'],
          portfolioResponse: portfolioFixtureWithoutMonitoring,
        };

        adminConfigurationParametersWithMonitoring = {
          inheritedRoleMap: {},
          keycloakRoles: ['ROLE_ADMIN'],
          portfolioResponse: portfolioFixtureWithMonitoring,
        };
      });
    cy.fixture('largeEnrichedPortfolio.json')
      .then(function (jsonContent) {
        largePortfolioFixture = jsonContent as EnrichedPortfolio;
      })
      .then(() => {
        largePortfolioConfigurationParameters = {
          inheritedRoleMap: {},
          keycloakRoles: ['ROLE_USER'],
          portfolioResponse: largePortfolioFixture,
        };
      });
  });

  it('Check different frameworks', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(nonMemberConfigurationParameters).then(() => {
      const expectedFirstRow = [
        'Company Name',
        'Country',
        'Sector',
        'SFDR',
        'EU Taxonomy Financials',
        'EU Taxonomy Non-Financials',
        'EU Taxonomy Nuclear and Gas',
      ];
      const expectedSecondRow = [
        'Apricot Inc.',
        'Cayman Islands',
        'ROI',
        2024,
        'No data available',
        'No data available',
        'No data available',
      ];
      const expectedThirdRow = [
        'Banana LLC',
        'Bouvet Island',
        'channels',
        'No data available',
        2023,
        'No data available',
        'No data available',
      ];
      const expectedFourthRow = [
        'Cherry Co',
        'Germany',
        'models',
        2024,
        'No data available',
        'No data available',
        2023,
      ];
      const checkHeadersRow = [
        checkHeader,
        checkHeader,
        checkHeader,
        checkHeader,
        checkHeader,
        checkHeader,
        checkHeader,
      ];
      const nothingToCheckRow = [undefined, undefined, undefined, undefined, undefined, undefined, undefined];
      assertTable('table', [expectedFirstRow, expectedSecondRow, expectedThirdRow, expectedFourthRow]);
      assertTable('table', [checkHeadersRow, nothingToCheckRow, nothingToCheckRow, nothingToCheckRow]);
    });
  });

  it('Check sorting', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(nonMemberConfigurationParameters).then(() => {
      checkSort('first-child', 'Apricot Inc.', 'Cherry Co', true);
      checkSort('nth-child(2)', 'Banana LLC', 'Cherry Co');
      checkSort('nth-child(3)', 'Banana LLC', 'Apricot Inc.');
      checkSort('nth-child(4)', 'Apricot Inc.', 'Banana LL');
    });
  });

  it('Check filter', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(nonMemberConfigurationParameters).then(() => {
      checkFilter('first-child', 'companyNameFilter', 'b', 2);
      checkFilter('nth-child(2)', 'countryFilter', 'Germany', 2);
      checkFilter('nth-child(3)', 'sectorFilter', 'o', 2);
      checkFilter('nth-child(4)', 'sfdrAvailableReportingPeriodsFilter', '2024', 3);
      checkFilter('nth-child(5)', 'eutaxonomyFinancialsAvailableReportingPeriodsFilter', '2023', 2);
      checkFilter('nth-child(6)', 'eutaxonomyNonFinancialsAvailableReportingPeriodsFilter', 'No data available', 4);
      checkFilter('nth-child(7)', 'nuclearAndGasAvailableReportingPeriodsFilter', '2023', 2);
    });
  });

  it('Check Monitoring Button for non Dataland member', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(nonMemberConfigurationParameters).then(() => {
      cy.get('[data-test="monitor-portfolio"]').should('be.disabled').and('contain.text', 'ACTIVE MONITORING');
    });
  });

  const testModes = ['member', 'admin'];

  /**
   * Gets the configuration parameters based on the test mode and monitoring status
   * @param testMode whether the test is for Dataland members or admins
   * @param monitoringIsOn whether the test portfolio shall have monitoring activated
   * @returns The appropriate configuration parameters for the test scenario
   */
  function getTestModeConfigurationParameters(testMode: string, monitoringIsOn: boolean): ConfigurationParameters {
    if (testMode === 'member') {
      return monitoringIsOn
        ? memberConfigurationParametersWithMonitoring
        : memberConfigurationParametersWithoutMonitoring;
    } else {
      return monitoringIsOn
        ? adminConfigurationParametersWithMonitoring
        : adminConfigurationParametersWithoutMonitoring;
    }
  }

  for (const testMode of testModes) {
    it('Check Monitoring Button and Not Monitored Tag for Dataland ' + testMode, function (): void {
      const configurationParameters = getTestModeConfigurationParameters(testMode, false);

      interceptApiCallsAndMountAndWaitForDownload(configurationParameters).then(() => {
        cy.get('[data-test="monitor-portfolio"]').should('be.visible').and('contain.text', 'ACTIVE MONITORING');
        cy.get('[data-test="is-monitored-tag"]')
          .should('be.visible')
          .and('contain.text', 'Portfolio not actively monitored');
      });
    });

    it('Check Monitored Tag for Dataland ' + testMode, function (): void {
      const configurationParameters = getTestModeConfigurationParameters(testMode, true);

      interceptApiCallsAndMountAndWaitForDownload(configurationParameters).then(() => {
        cy.get('[data-test="is-monitored-tag"]')
          .should('be.visible')
          .and('contain.text', 'Portfolio actively monitored');
      });
    });
  }

  it('Check pagination for small portfolios', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(nonMemberConfigurationParameters).then(() => {
      cy.get('.p-datatable-paginator-bottom').should('not.exist');
    });
  });

  it('Check pagination for large portfolios', function (): void {
    interceptApiCallsAndMountAndWaitForDownload(largePortfolioConfigurationParameters).then(() => {
      cy.get('.p-datatable-paginator-bottom').should('be.visible');
      cy.get('table tr').should('have.length', MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE + 1); // +1 for header row
      cy.get('[data-pc-section="page"][aria-label="Page 2"]').click();
      cy.get('table tr:first-child td:first-child').should(
        'contain.text',
        `Company ${MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE + 1}`
      );
      cy.get('table tr:first-child th:first-child [data-pc-section="sort"]').click();
      cy.get('table tr:first-child td:first-child').should('contain.text', `Company 110`);
      cy.get('[data-pc-section="page"][aria-label="Page 1"]').should('have.attr', 'data-p-active', 'true');
    });
  });
});

/**
 * Checks of the sort- and column filter-icons are present in the table header
 */
function checkHeader(): void {
  cy.get('[data-pc-section="sort"]').should('be.visible');
  cy.get('[data-pc-section="filter"]').should('be.visible');
}

/**
 * Checks the sorting functionality of a column
 * @param selector CSS selector to find the element for switching selection to up/down/off
 * @param companyUp First company in the table when sorting is UP
 * @param companyDown First company in the table when sorting is DOWN
 * @param isAlreadySorted Checks if pre-sorting is already in place
 */
function checkSort(selector: string, companyUp: string, companyDown: string, isAlreadySorted: boolean = false): void {
  const firstCellSelector = 'table tr:first-child td:first-child';
  const sortSelector = `table tr:first-child th:${selector} [data-pc-section="sort"]`;

  if (!isAlreadySorted) {
    cy.get(firstCellSelector).contains('Apricot Inc.');
    cy.get(sortSelector).click();
  }
  cy.get(firstCellSelector).contains(companyUp);
  cy.get(sortSelector).click();
  cy.get(firstCellSelector).contains(companyDown);
  cy.get(sortSelector).click();
  cy.get(firstCellSelector).contains('Apricot Inc.');
}

/**
 * Checks the filter functionality of a column
 * @param columnSelector CSS selector to find the element for opening the filter popup
 * @param inputSelector CSS selector to find the filter input element
 * @param needle For what we filter
 * @param matches How many rows we have left after filtering
 */
function checkFilter(columnSelector: string, inputSelector: string, needle: string, matches: number): void {
  const rowsSelector = 'table tr';
  const filterButtonSelector = `[data-pc-section="headerrow"] th:${columnSelector} [data-pc-section="filtermenuicon"]`;
  cy.get(rowsSelector).should('have.length', 4);
  cy.get(filterButtonSelector).click();

  if (inputSelector == 'companyNameFilter') {
    cy.get(`[data-test="${inputSelector}Value"]`).type(needle);
  } else {
    cy.get(`[data-test="${inputSelector}Overlay"]`).contains(needle).click();
  }
  cy.get(filterButtonSelector).click();
  cy.get(rowsSelector).should('have.length', matches);
  cy.get('[data-test="reset-filter"]').click();
  cy.get(rowsSelector).should('have.length', 4);
}

/**
 * Checks an arbitrary table
 * @param tableSelector CSS selector to find the table
 * @param expected 2 dimensional array that contains an element for each table cell. Each element can contain
 *  - A string or number: The content of the table cell must contain them.
 *  - A function: The function is evaluated within the table cell.
 *  - null: No check is performed on that cell
 */
function assertTable(tableSelector: string, expected: (string | number | undefined | (() => void))[][]): void {
  cy.get(tableSelector + ' tr').each((row, rowIndex) => {
    cy.wrap(row).within(() => {
      cy.get('th, td').each((cell, cellIndex) => {
        const element = cy.wrap(cell);
        const comparator = expected[rowIndex]![cellIndex];
        switch (typeof comparator) {
          case 'string':
          case 'number':
            element.should('contain', comparator);
            break;
          case 'function':
            element.within(comparator);
            break;
          case 'undefined':
            break;
          default:
            throw new Error(
              `Row ${rowIndex + 1}, Column ${cellIndex + 1}: Unsupported comparator type '${typeof comparator}'`
            );
        }
      });
    });
  });
}
