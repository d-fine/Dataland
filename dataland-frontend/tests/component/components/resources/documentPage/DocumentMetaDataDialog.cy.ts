import { type CompanyInformation } from '@clients/backend';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils.ts';
import DocumentMetaDataDialog from '@/components/resources/documentPage/DocumentMetaDataDialog.vue';

describe('Component test for the Document Meta Data Dialog', () => {
  const dummyDocumentId: string = '00000000-0000-0000-0000-000000000000';
  const sampleDocumentCategory: string = 'AnnualReport';
  const dummyCompanyId1: string = '11111111-1111-1111-1111-111111111111';
  const dummyCompanyId2: string = '22222222-2222-2222-2222-222222222222';
  const samplePublicationDate: string = '2024-01-01';
  const sampleReportingPeriod: string = '2023';
  const dummyCompanyName1: string = 'dummy-company-1';
  const dummyHeadquarters1: string = 'dummy-city-1';
  const dummyCountryCode: string = 'DC';
  const dummyCompanyName2: string = 'dummy-company-2';
  const dummyHeadquarters2: string = 'dummy-city-2';

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
    cy.intercept('**/documents/*/metadata', { fixture: 'DummyDocumentMetaInfoEntity' }).as(
      'fetchDocumentMetaInfoEntity'
    );
    cy.intercept(`**/api/companies/${dummyCompanyId1}/info`, { body: dummyCompanyInformation1 }).as(
      'fetchFirstCompanyInformation'
    );
    cy.intercept(`**/api/companies/${dummyCompanyId2}/info`, { body: dummyCompanyInformation2 }).as(
      'fetchSecondCompanyInformation'
    );
  });

  it('Check if all expected elements are displayed correctly', () => {
    cy.mountWithPlugins(DocumentMetaDataDialog, {
      keycloak: minimalKeycloakMock({}),
      props: {
        documentId: dummyDocumentId,
        isOpen: true,
      },
    }).then(() => {
      cy.get("[data-test='document-details-modal']").should('exist');
      cy.get("[data-test='document-link']").should('exist').and('contain', `dummy-document`);
      cy.get("[data-test='publication-date']")
        .should('exist')
        .and('contain', `${dateStringFormatter(samplePublicationDate)}`);
      cy.get("[data-test='document-type']")
        .should('exist')
        .and('contain', `${humanizeStringOrNumber(sampleDocumentCategory)}`);
      cy.get("[data-test='reporting-period']").should('exist').and('contain', `${sampleReportingPeriod}`);
      cy.get("[data-test='upload-time']")
        .should('exist')
        .and('contain', `${convertUnixTimeInMsToDateString(1709565656858)}`);
    });
  });
});
