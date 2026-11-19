import DatasetOverview from '@/components/pages/DatasetOverview.vue';
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum, QaStatus, type DataMetaInformationForMyDatasets } from '@clients/backend';

const dummyUserId = 'mock-user-id';
const userUploadsUrl = `**/api/users/${dummyUserId}/uploads`;

const uploadedDataset: DataMetaInformationForMyDatasets = {
  companyId: 'mock-company-id',
  dataId: 'mock-data-id',
  companyName: 'Imaginary-Corporate',
  dataType: DataTypeEnum.Lksg,
  reportingPeriod: '2023',
  qualityStatus: QaStatus.Accepted,
  currentlyActive: false,
  uploadTime: 1672527600000,
};

/**
 * Mounts the DatasetOverview page for a logged-in user
 * @param roles the Keycloak roles of the user
 */
function mountDatasetOverview(roles: string[] = [KEYCLOAK_ROLE_USER]): void {
  cy.mountWithPlugins(DatasetOverview, {
    keycloak: minimalKeycloakMock({ userId: dummyUserId, roles }),
  });
}

describe('Component tests for the DatasetOverview page', () => {
  it('Should not display the New Dataset button to non-uploader users', () => {
    cy.intercept(userUploadsUrl, []);
    mountDatasetOverview();
    cy.get('h1[data-test=noDatasetUploadedText]').should('be.visible');
    cy.get('div[data-test=datasetOverviewTable]').should('not.be.visible');
    cy.get('a[data-test=newDatasetButton]').should('not.exist');
  });

  it('Should display the New Dataset button to uploaders', () => {
    cy.intercept(userUploadsUrl, []);
    mountDatasetOverview([KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER]);
    cy.get('h1[data-test=noDatasetUploadedText]').should('be.visible');
    cy.get('div[data-test=datasetOverviewTable]').should('not.be.visible');
    cy.get('[data-test=newDatasetButton]').should('be.visible');
  });

  it('Shows a loading indicator until the datasets of the user are loaded', () => {
    cy.intercept(userUploadsUrl, { delay: 500, body: [uploadedDataset] }).as('fetchUserUploads');
    mountDatasetOverview();
    cy.contains('Loading datasets...').should('be.visible');
    cy.get('h1[data-test=noDatasetUploadedText]').should('not.exist');
    cy.wait('@fetchUserUploads');
    cy.contains('Loading datasets...').should('not.exist');
    cy.get('div[data-test=datasetOverviewTable]').should('be.visible');
    cy.get('td').contains(uploadedDataset.companyName).should('be.visible');
    cy.get('[data-test=qa-status]').should('contain.text', 'Superseded');
  });

  it('Shows an error message instead of a spinner if the datasets cannot be loaded', () => {
    cy.intercept(userUploadsUrl, { statusCode: 500 }).as('fetchUserUploads');
    mountDatasetOverview();
    cy.wait('@fetchUserUploads');
    cy.get('[data-test=datasetsErrorText]').should('be.visible').and('contain.text', 'could not be loaded');
    cy.contains('Loading datasets...').should('not.exist');
    cy.get('h1[data-test=noDatasetUploadedText]').should('not.exist');
  });
});
