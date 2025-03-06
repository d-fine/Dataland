import { type CompanyInformation } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { type DocumentMetaInfoEntity } from '@clients/documentmanager';
import DocumentMetaDataDialog from '@/components/resources/documentPage/DocumentMetaDataDialog.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles.ts';
// import { truncatedDocumentName } from "@/utils/StringFormatter.ts";
import { dateStringFormatter } from '@/utils/DataFormatUtils.ts';
//import type {FixtureData} from "@sharedUtils/Fixtures.ts";
//import DocumentOverview from "@/components/pages/DocumentOverview.vue";

describe('Component test for the Document Meta Data Dialog', () => {
  const dummyDocumentId = '00000000-0000-0000-0000-000000000000';
  const sampleDocumentType = 'Pdf';
  const dummyDocumentName = 'dummy-document';
  const sampleDocumentCategory = 'AnnualReport';
  //const sampleDocumentCategoryHumanized = "Annual Report"; // sample document category as it is actually displayed
  const dummyCompanyId1 = '11111111-1111-1111-1111-111111111111';
  const dummyCompanyId2 = '22222222-2222-2222-2222-222222222222';
  const dummyUploaderId = '33333333-3333-3333-3333-333333333333';
  const sampleUploadTime = 1709565656858;
  const sampleUploadTimeHumanized = '04.03.2024 15:20'; // sample upload time as it is actually displayed
  const samplePublicationDate = '2024-01-01';
  const sampleReportingPeriod = '2023';
  const sampleQaStatus = 'Accepted';
  const dummyCompanyName1 = 'dummy-company-1';
  const dummyHeadquarters1 = 'dummy-city-1';
  const dummyCountryCode = 'DC';
  const dummyCompanyName2 = 'dummy-company-2';
  const dummyHeadquarters2 = 'dummy-city-2';

  const dummyDocumentMetaInfoEntity: DocumentMetaInfoEntity = {
    documentId: dummyDocumentId,
    documentType: sampleDocumentType,
    documentName: dummyDocumentName,
    documentCategory: sampleDocumentCategory,
    companyIds: new Set<string>([dummyCompanyId1, dummyCompanyId2]),
    uploaderId: dummyUploaderId,
    uploadTime: sampleUploadTime,
    publicationDate: samplePublicationDate,
    reportingPeriod: sampleReportingPeriod,
    qaStatus: sampleQaStatus,
  };
  const dummyCompanyInformation1: CompanyInformation = {
    companyName: dummyCompanyName1,
    headquarters: dummyHeadquarters1,
    identifiers: {},
    countryCode: dummyCountryCode,
  };
  const dummyCompanyInformation2: CompanyInformation = {
    companyName: dummyCompanyName2,
    headquarters: dummyHeadquarters2,
    identifiers: {},
    countryCode: dummyCountryCode,
  };

  /**
   * Mocks the requests that happen when the document metadata dialog page is being mounted
   */
  beforeEach(function () {
    cy.intercept('**/documents/**/metadata', {
      body: dummyDocumentMetaInfoEntity,
      //times: 1,
    }).as('fetchDocumentMetaInfoEntity');
    cy.intercept(`**/api/companies/${dummyCompanyId1}`, {
      body: dummyCompanyInformation1,
      //times: 1,
    }).as('fetchFirstCompanyInformation');
    cy.intercept(`**/api/companies/${dummyCompanyId2}`, {
      body: dummyCompanyInformation2,
      //times: 1,
    }).as('fetchSecondCompanyInformation');
  });

  /**
   * Waits for the three requests that happen when the document overview page is being mounted
   */
  function waitForRequestsOnMounted(): void {
    //cy.wait('@fetchDocumentMetaInfoEntity');
    //cy.wait('@fetchFirstCompanyInformation');
    //cy.wait('@fetchSecondCompanyInformation');
  }

  /**
   * Mounts the document metadata dialog page with a specific authentication
   * @returns the mounted component
   */
  function mountDocumentMetaDataDialogWithAuthentication(
    isLoggedIn: boolean,
    keycloakRoles?: string[]
  ): Cypress.Chainable {
    return getMountingFunction({
      keycloak: minimalKeycloakMock({
        authenticated: isLoggedIn,
        roles: keycloakRoles,
        userId: '44444444-4444-4444-4444-444444444444',
      }),
    })(DocumentMetaDataDialog, {
      props: {
        documentId: dummyDocumentId,
      },
    });
  }

  it('Check for all expected elements', () => {
    mountDocumentMetaDataDialogWithAuthentication(true, [KEYCLOAK_ROLE_UPLOADER]);
    waitForRequestsOnMounted();
    console.log(dummyDocumentMetaInfoEntity);

    cy.get("[data-test='document-link']").should('exist').and('contain', `.com/${dummyDocumentId}`);
    cy.get("[data-test='publication-date'")
      .should('exist')
      .and('contain', `${dateStringFormatter(samplePublicationDate)}`);
    cy.get("[data-test='document-type']")
      .should('exist')
      .and('contain', `${humanizeStringOrNumber(sampleDocumentCategory)}`);
    cy.get("[data-test='reporting-period']").should('exist').and('contain', `${sampleReportingPeriod}`);
    cy.get("[data-test='upload-time']").should('exist').and('contain', `${sampleUploadTimeHumanized}`);
  });
});
