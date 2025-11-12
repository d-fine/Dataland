import { type CompanyInformation } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils.ts';
import DocumentMetaDataDialog from '@/components/resources/documentPage/DocumentMetaDataDialog.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { type DocumentMetaInfoEntity } from '@clients/documentmanager';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles.ts';

describe('Component test for the Document Meta Data Dialog', () => {
  let documentMetaInfoEntityFromFixture: DocumentMetaInfoEntity;
  let dummyDocumentId: string;
  let sampleDocumentCategory: string;
  let dummyCompanyId1: string;
  let dummyCompanyId2: string;
  let dummyUploaderId: string;
  let samplePublicationDate: string;
  let sampleReportingPeriod: string;

  before(function () {
    cy.fixture('DummyDocumentMetaInfoEntity').then(function (jsonContent: DocumentMetaInfoEntity) {
      documentMetaInfoEntityFromFixture = jsonContent;
      dummyDocumentId = documentMetaInfoEntityFromFixture.documentId;
      sampleDocumentCategory = documentMetaInfoEntityFromFixture.documentCategory!;
      dummyCompanyId1 = Array.from(documentMetaInfoEntityFromFixture.companyIds)[0]!;
      dummyCompanyId2 = Array.from(documentMetaInfoEntityFromFixture.companyIds)[1]!;
      dummyUploaderId = documentMetaInfoEntityFromFixture.uploaderId;
      samplePublicationDate = documentMetaInfoEntityFromFixture.publicationDate!;
      sampleReportingPeriod = documentMetaInfoEntityFromFixture.reportingPeriod!;
    });
  });

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
    //@ts-ignore
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

  it('Check that edit mode is enabled for the uploader who uploaded the document', () => {
    //@ts-ignore
    cy.mountWithPlugins(DocumentMetaDataDialog, {
      keycloak: minimalKeycloakMock({ userId: dummyUploaderId, roles: [KEYCLOAK_ROLE_UPLOADER] }),
      props: {
        documentId: dummyDocumentId,
        isOpen: true,
      },
    }).then(() => {
      cy.get("[data-test='edit-icon']").should('exist').should('be.visible');
    });
  });

  it('Check that edit mode is disabled for an uploader who did not upload the document', () => {
    //@ts-ignore
    cy.mountWithPlugins(DocumentMetaDataDialog, {
      keycloak: minimalKeycloakMock({ userId: 'not-the-original-uploader', roles: [KEYCLOAK_ROLE_UPLOADER] }),
      props: {
        documentId: dummyDocumentId,
        isOpen: true,
      },
    }).then(() => {
      cy.get("[data-test='edit-icon']").should('not.exist');
    });
  });

  it('Check that edit mode is enabled for admins', () => {
    //@ts-ignore
    cy.mountWithPlugins(DocumentMetaDataDialog, {
      keycloak: minimalKeycloakMock({ roles: [KEYCLOAK_ROLE_ADMIN] }),
      props: {
        documentId: dummyDocumentId,
        isOpen: true,
      },
    }).then(() => {
      cy.get("[data-test='edit-icon']").should('exist').should('be.visible');
    });
  });
});
