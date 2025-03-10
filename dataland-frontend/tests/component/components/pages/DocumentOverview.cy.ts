import DocumentOverview from '@/components/pages/DocumentOverview.vue'; // Update this path
import { type CompanyInformation, type HeimathafenData } from '@clients/backend';
import type { FixtureData } from '@sharedUtils/Fixtures.ts';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles.ts';
import { DocumentMetaInfoDocumentCategoryEnum, type DocumentMetaInfoResponse } from '@clients/documentmanager';
import { dateStringFormatter } from '@/utils/DataFormatUtils.ts';
import { humanizeStringOrNumber, truncatedDocumentName } from '@/utils/StringFormatter.ts';

describe('Component test for the Document Overview', () => {
  let companyInformationForTest: CompanyInformation;
  let mockFetchedDocuments: DocumentMetaInfoResponse[];
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyUserId = 'mock-user-id';
  const searchStringForApi = 'AnnualReport';

  before(function () {
    cy.fixture('CompanyInformationWithHeimathafenData').then(function (jsonContent): void {
      const heimathafenFixtures = jsonContent as Array<FixtureData<HeimathafenData>>;
      companyInformationForTest = heimathafenFixtures[0].companyInformation;
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
    cy.intercept(`**/`, {
      body: mockFetchedDocuments,
      times: 1,
    }).as('fetchDocumentsFiltered');

    cy.intercept(`**/?companyId=${dummyCompanyId}&documentCategories=${searchStringForApi}`, {
      body: mockFetchedDocuments.filter((document) => document.documentCategory === 'AnnualReport'),
      times: 1,
    }).as('fetchDocumentsFilteredDocumentType');
  }

  /**
   * Waits for the 5 requests that happen when the document overview page is being mounted
   */
  function waitForRequestsOnMounted(): void {
    cy.wait('@fetchCompanyInfo');
    cy.wait('@fetchCompanyOwnershipExistence');
    cy.wait('@fetchValidateCompanyRole');
    cy.wait('@fetchDocumentsFilteredCompanyId');
    cy.wait('@fetchDocumentsFiltered');
  }

  /**
   * Mounts the document overview page with a specific authentication
   * @param isLoggedIn determines if the mount shall happen from a logged-in users perspective
   * @param isMobile determines if the mount shall happen from a mobie-users perspective
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
    let myPublicationDateToTest: string = '1900-02-26';
    if (mockFetchedDocuments[0].publicationDate) {
      myPublicationDateToTest = mockFetchedDocuments[0].publicationDate;
    }

    cy.get("[data-test='sheet']").should('exist').and('contain', companyInformationForTest.companyName);
    cy.get("[data-test='documents-overview-table']")
      .should('exist')
      .within(() => {
        cy.get('tbody tr')
          .should('have.length', Object.keys(mockFetchedDocuments).length)
          .first() //make sure first object in testing/data/documents/CompanyDocumentsMock.json is the one with earliest publicationDate
          .within(() => {
            cy.get('td').eq(0).should('contain', mockFetchedDocuments[0].documentName);
            cy.get('td').eq(1).should('contain', humanizeStringOrNumber(mockFetchedDocuments[0].documentCategory));
            cy.get('td').eq(2).should('contain', dateStringFormatter(myPublicationDateToTest));
            cy.get('td').eq(3).should('contain', mockFetchedDocuments[0].reportingPeriod);
            cy.get('td').eq(4).should('contain', 'VIEW DETAILS');
            cy.get('td')
              .eq(5)
              .should(
                'contain',
                `${truncatedDocumentName(mockFetchedDocuments[0])} (${mockFetchedDocuments[0].publicationDate})`
              );
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
    cy.get('.d-framework-data-search-dropdown')
      .should('exist')
      .within(() => {
        cy.get('ul').children().should('have.length', numOfAllMultiSelectOptions);
        cy.contains('li', stringInMultiSelect).should('exist').click();
      });

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
