import DatasetOverview from '@/components/pages/DatasetOverview.vue';
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Component tests for the DatasetOverview page', () => {
  it('Should not display the New Dataset button to non-uploader users', () => {
    const keycloakMock = minimalKeycloakMock({});
    cy.intercept('**/api/users/**', []);
    cy.mountWithPlugins(DatasetOverview, {
      keycloak: keycloakMock,
    });
    cy.get('h1[data-test=noDatasetUploadedText]').should('be.visible');
    cy.get('div[data-test=datasetOverviewTable]').should('not.be.visible');
    cy.get('a[data-test=newDatasetButton]').should('not.exist');
  });

  it('Should display the New Dataset button to uploaders', () => {
    const keycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER],
    });
    cy.intercept('**/api/users/**', []);
    cy.mountWithPlugins(DatasetOverview, {
      keycloak: keycloakMock,
    });
    cy.get('h1[data-test=noDatasetUploadedText]').should('be.visible');
    cy.get('div[data-test=datasetOverviewTable]').should('not.be.visible');
    cy.get('[data-test=newDatasetButton]').should('be.visible');
  });
});
