import DocumentOverview from '@/components/pages/DocumentOverview.vue'; // Update this path
import { type CompanyInformation, type LksgData } from '@clients/backend';
import type { FixtureData } from '@sharedUtils/Fixtures.ts';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles.ts';
import { DocumentMetaInfoDocumentCategoryEnum, type DocumentMetaInfoResponse } from '@clients/documentmanager';
import { dateStringFormatter } from '@/utils/DataFormatUtils.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';

/**
 * Waits for the 5 requests that happen when the document overview page is being mounted
 */
function waitForRequestsOnMounted(): void {
  cy.wait('@fetchCompanyInfo');
  cy.wait('@fetchCompanyOwnershipExistence');
  cy.wait('@fetchValidateCompanyRole');
  cy.wait('@fetchDocumentsFilteredCompanyId');
}

describe('Component test for the Document Overview', () => {
  let companyInformationForTest: CompanyInformation;
  let mockFetchedDocuments: DocumentMetaInfoResponse[];
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyUserId = 'mock-user-id';
  const searchStringForApi = 'AnnualReport';

  before(function () {
    cy.fixture('CompanyInformationWithLksgData').then(function (jsonContent): void {
      const lksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      if (!lksgFixtures[0]) throw new Error(`Fixture for lksg not defined`);
      companyInformationForTest = lksgFixtures[0].companyInformation;
    });
    cy.fixture('CompanyDocumentsMock').then(function (jsonContent) {
      mockFetchedDocuments = jsonContent as DocumentMetaInfoResponse[];
    });
  });

  /**
   * Mocks the requests that happen when the document overview page is being mounted
   * @param hasCompanyAtLeastOneOwner has the company at least one company owner
   */
  function mockRequestsOnMounted(hasCompanyAtLeastOneOwner: boolean): void {
    cy.intercept(`**/api/companies/*/info`, {
      body: companyInformationForTest,
      times: 1,
    }).as('fetchCompanyInfo');
    const hasCompanyAtLeastOneOwnerStatusCode = hasCompanyAtLeastOneOwner ? 200 : 404;
    cy.intercept(`**/community/company-ownership/${dummyCompanyId}`, {
      statusCode: hasCompanyAtLeastOneOwnerStatusCode,
    }).as('fetchCompanyOwnershipExistence');
    cy.intercept(`**/community/company-role-assignments/CompanyOwner/${dummyCompanyId}/mock-user-id`, {
      statusCode: 200,
    }).as('fetchValidateCompanyRole');
    cy.intercept(`**/?companyId=${dummyCompanyId}`, {
      body: mockFetchedDocuments,
      times: 1,
    }).as('fetchDocumentsFilteredCompanyId');
    cy.intercept(`**/?companyId=${dummyCompanyId}&documentCategories=${searchStringForApi}`, {
      body: mockFetchedDocuments.filter((document) => document.documentCategory === 'AnnualReport'),
      times: 1,
    }).as('fetchDocumentsFilteredDocumentType');
  }

  /**
   * Mounts the document overview page with a specific authentication
   * @param isLoggedIn determines if the mount shall happen from a logged-in users perspective
   * @param keycloakRoles defines the keycloak roles of the user if the mount happens from a logged-in users perspective
   * @returns the mounted component
   */
  function mountDocumentOverviewWithAuthentication(isLoggedIn: boolean, keycloakRoles?: string[]): Cypress.Chainable {
    return getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: isLoggedIn,
        roles: keycloakRoles,
        userId: dummyUserId,
      }),
    })(DocumentOverview, {
      props: {
        companyId: dummyCompanyId,
      },
    });
  }

  it('Check for all expected elements for a logged-in uploader-user', () => {
    const hasCompanyAtLeastOneOwner = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountDocumentOverviewWithAuthentication(true, [KEYCLOAK_ROLE_UPLOADER]);
    waitForRequestsOnMounted();
    cy.get("[data-test='company-info-sheet']").should('exist').and('contain', companyInformationForTest.companyName);
    cy.get("[data-test='documents-overview-table']").should('exist');
    cy.get("[data-test='documents-overview-table'] tbody tr")
      .should('have.length', Object.keys(mockFetchedDocuments).length)
      .each(($el, index) => {
        const document = mockFetchedDocuments[index]!;
        cy.wrap($el).within(() => {
          cy.get('td').eq(0).should('have.text', document.documentName);
          cy.get('td').eq(1).should('have.text', humanizeStringOrNumber(document.documentCategory));
          // Check that a null publication date is not converted to Jan 1, 1970.
          cy.get('td').eq(2).should('not.contain', '1970');
          cy.get('td').eq(2).should('have.text', dateStringFormatter(document.publicationDate));
          cy.get('td').eq(3).should('have.text', document.reportingPeriod);
          cy.get('td').eq(4).should('contain', 'VIEW DETAILS');
          cy.get('td').eq(5).should('have.text', 'DOWNLOAD');
        });
      });
  });

  it('Checks if filter function shows only results with selected DocumentCategory and if reset button resets filter to show all results', () => {
    const hasCompanyAtLeastOneOwner = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountDocumentOverviewWithAuthentication(true, [KEYCLOAK_ROLE_UPLOADER]);
    waitForRequestsOnMounted();
    const stringInMultiSelect = 'Annual Report';
    let numOfFilteredOptions = 0;
    for (const document of mockFetchedDocuments) {
      if (document.documentCategory == 'AnnualReport') numOfFilteredOptions++;
    }
    const numOfAllMultiSelectOptions = Object.keys(DocumentMetaInfoDocumentCategoryEnum).length;

    cy.get("[data-test='document-type-picker']").should('exist').click();
    cy.get('#document-type-filter_list')
      .should('exist')
      .children()
      .should('have.length', numOfAllMultiSelectOptions)
      .invoke('attr', 'style', 'position: relative; z-index: 1'); //fixing an apparent cypress bug
    cy.contains('li', stringInMultiSelect).should('exist').click();

    cy.get("[data-test='documents-overview-table']")
      .should('exist')
      .within(() => {
        cy.get('tbody tr').should('have.length', numOfFilteredOptions);
      });

    cy.get("[data-test='reset-filter']").click();
    cy.get("[data-test='documents-overview-table']")
      .should('exist')
      .within(() => {
        cy.get('tbody tr').should('have.length', Object.keys(mockFetchedDocuments).length);
      });
  });

  it('Check if view details button opens DocumentMetaDataDialog', () => {
    const hasCompanyAtLeastOneOwner = true;
    mockRequestsOnMounted(hasCompanyAtLeastOneOwner);
    mountDocumentOverviewWithAuthentication(true, [KEYCLOAK_ROLE_UPLOADER]);
    waitForRequestsOnMounted();

    cy.get("[data-test='documents-overview-table']").within(() => {
      cy.get('tbody tr')
        .first()
        .within(() => {
          cy.get('td').eq(4).click();
        });
    });
    cy.get("[data-test='document-details-modal']").should('exist');
  });
});
