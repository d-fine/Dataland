import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import { KEYCLOAK_ROLE_PREMIUM_USER } from '@/utils/KeycloakRoles.ts';
import { type EnrichedPortfolio } from '@clients/userservice';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Check the portfolio details view', function (): void {
  let portfolioFixture: EnrichedPortfolio;

  before(function () {
    cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
      portfolioFixture = jsonContent as EnrichedPortfolio;
    });
  });

  it('Check different frameworks', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDetails, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
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
      cy.wait('@downloadComplete').then(() => {
        assertTable('table', [expectedFirstRow, expectedSecondRow, expectedThirdRow, expectedFourthRow]);
        assertTable('table', [checkHeadersRow, nothingToCheckRow, nothingToCheckRow, nothingToCheckRow]);
      });
    });
  });

  it('Check sorting', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDetails, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        checkSort('first-child', 'Apricot Inc.', 'Cherry Co', true);
        checkSort('nth-child(2)', 'Banana LLC', 'Cherry Co');
        checkSort('nth-child(3)', 'Banana LLC', 'Apricot Inc.');
        checkSort('nth-child(4)', 'Apricot Inc.', 'Banana LL');
      });
    });
  });

  it('Check filter', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDetails, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        checkFilter('first-child', 'companyNameFilter', 'b', 2);
        checkFilter('nth-child(2)', 'countryFilter', 'Germany', 2);
        checkFilter('nth-child(3)', 'sectorFilter', 'o', 2);
        checkFilter('nth-child(4)', 'sfdrAvailableReportingPeriodsFilter', '2024', 3);
        checkFilter('nth-child(5)', 'eutaxonomyFinancialsAvailableReportingPeriodsFilter', '2023', 2);
        checkFilter('nth-child(6)', 'eutaxonomyNonFinancialsAvailableReportingPeriodsFilter', 'No data available', 4);
        checkFilter('nth-child(7)', 'nuclearAndGasAvailableReportingPeriodsFilter', '2023', 2);
      });
    });
  });

  it('Check Monitoring Button for non premium user', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDetails, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        cy.get('[data-test="monitor-portfolio"]').should('be.disabled').and('contain.text', 'Activate Monitoring');
      });
    });
  });
  it('Check Monitoring Button and Not Monitored Badge for premium user', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('downloadComplete');
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDetails, {
      keycloak: minimalKeycloakMock({
        roles: [KEYCLOAK_ROLE_PREMIUM_USER],
      }),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        cy.get('[data-test="monitor-portfolio"]').should('be.visible').and('contain.text', 'Active Monitoring');
        cy.get('[data-test="is-not-monitored-badge"]')
          .should('be.visible')
          .and('contain.text', 'Portfolio not actively monitored');
        cy.get('[data-test="is-monitored-badge"]').should('not.exist');
      });
    });
  });
  it('Check Monitored Badge for premium user', function (): void {
    cy.intercept('**/users/portfolios/*/enriched-portfolio', {
      ...portfolioFixture,
      isMonitored: true,
      startingMonitoringPeiod: '2024',
      monitoredFrameworks: new Set('sfdr'),
    }).as('downloadComplete');
    // @ts-ignore
    cy.mountWithPlugins(PortfolioDetails, {
      keycloak: minimalKeycloakMock({
        roles: [KEYCLOAK_ROLE_PREMIUM_USER],
      }),
      props: { portfolioId: portfolioFixture.portfolioId },
    }).then(() => {
      cy.wait('@downloadComplete').then(() => {
        cy.get('[data-test="is-monitored-tag"]')
          .should('be.visible')
          .and('contain.text', 'Portfolio actively monitored');
      });
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
        const comparator = expected[rowIndex][cellIndex];
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
