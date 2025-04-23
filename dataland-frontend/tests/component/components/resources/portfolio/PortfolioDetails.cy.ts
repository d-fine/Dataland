import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import { type EnrichedPortfolio } from '@clients/userservice';

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
      cy.wait('@downloadComplete').then(() => {
        assertTable('table', [
          ['Company Name', 'Country', 'Sector', 'Last Reporting Period'],
          ['Boyle, Aufderhar and Smitham', 'Cayman Islands', 'ROI', 2024],
          ['Beahan LLC', 'Bouvet Island', 'channels', 'No data available'],
          ['Aufderhar, Herzog and King', 'South Georgia and the South Sandwich Islands', 'models', 2024],
        ]);
        cy.get('[data-test="framework-dropdown"]').click();
        cy.get('[data-pc-section="item"]:contains(SFDR)').click();
        assertTable('table', [
          [checkHeader, checkHeader, checkHeader, checkHeader],
          ['Boyle, Aufderhar and Smitham', 'Cayman Islands', 'ROI', 'No data available'],
          ['Beahan LLC', 'Bouvet Island', 'channels', 'No data available'],
          ['Aufderhar, Herzog and King', 'South Georgia and the South Sandwich Islands', 'models', 'No data available'],
        ]);
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
        checkSort('first-child', 'Aufderhar, Herzog and King', 'Boyle, Aufderhar and Smitham');
        checkSort('nth-child(2)', 'Beahan LLC', 'Aufderhar, Herzog and King');
        checkSort('nth-child(3)', 'Beahan LLC', 'Boyle, Aufderhar and Smitham');
        checkSort('nth-child(4)', 'Boyle, Aufderhar and Smitham', 'Beahan LL');
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
        checkFilter('first-child', 'companyNameFilterValue', 'b', 3);
        checkFilter('nth-child(2)', 'countryCodeFilterValue', 'South Georgia', 2);
        checkFilter('nth-child(3)', 'sectorFilterValue', 'o', 3);
        checkFilter('nth-child(4)', 'latestReportingPeriodeFilterValue', '2024', 3);
      });
    });
  });
});

/**
 * Checks of the sort- and column-filter-icons are present in the table header
 */
function checkHeader(): void {
  cy.get('[data-pc-section="sort"]').should('be.visible');
  cy.get('[data-pc-section="columnfilter"]').should('be.visible');
}

/**
 * Checks the sorting functionality of a column
 * @param selector CSS selector to find the element for switching selection to up/down/off
 * @param companyUp First company in the table when sorting is UP
 * @param copmanyDown First company in the table when sorting is DOWN
 */
function checkSort(selector: string, companyUp: string, copmanyDown: string): void {
  const firstCellSelector = 'table tr:first-child td:first-child';
  const sortSelector = `table tr:first-child th:${selector} [data-pc-section="sort"]`;
  cy.get(firstCellSelector).contains('Boyle, Aufderhar and Smitham');
  cy.get(sortSelector).click();
  cy.get(firstCellSelector).contains(companyUp);
  cy.get(sortSelector).click();
  cy.get(firstCellSelector).contains(copmanyDown);
  cy.get(sortSelector).click();
  cy.get(firstCellSelector).contains('Boyle, Aufderhar and Smitham');
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
  const filterButtonSelector = `table tr:first-child th:${columnSelector} [data-pc-section="filtermenubutton"]`;
  cy.get(rowsSelector).should('have.length', 4);
  cy.get(filterButtonSelector).click();

  if (inputSelector == 'latestReportingPeriodeFilterValue') {
    cy.get(`[data-test="${inputSelector}"]`).first().click();
  } else {
    cy.get(`[data-test="${inputSelector}"]`).type(needle);
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
function assertTable(tableSelector: string, expected: (string | number | null | (() => void))[][]): void {
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
