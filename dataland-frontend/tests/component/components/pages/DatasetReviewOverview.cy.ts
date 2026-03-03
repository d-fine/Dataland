import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';
import { type DataMetaInformation, QaStatus, type StoredCompany } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import DatasetReviewOverview from '@/components/pages/DatasetReviewOverview.vue';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import PrimeVue from 'primevue/config';

describe('DatasetReviewOverview page', () => {
  const keycloakMockWithReviewer = minimalKeycloakMock({
    roles: [KEYCLOAK_ROLE_REVIEWER],
  });

  const dataId = 'test-data-id';
  const companyId = '9af067dc-8280-4172-8974-1ae363c56260';

  const mockMetaInfo: DataMetaInformation = {
    dataId: dataId,
    companyId: companyId,
    dataType: 'sfdr' as any,
    uploadTime: Date.now(),
    reportingPeriod: '2021',
    currentlyActive: true,
    qaStatus: QaStatus.Pending,
  };

  const mockCompanyInfo: StoredCompany = {
    companyId: companyId,
    companyInformation: {
      companyName: 'd-clare',
      headquarters: 'Frankfurt',
      identifiers: { Lei: ['1234567890'] },
      sector: 'Imaginary Sector',
      countryCode: 'DE',
    },
    dataRegisteredByDataland: [],
  };

  function mountPage() {
    cy.intercept('GET', `**/api/companies/${companyId}`, mockCompanyInfo);
    cy.intercept('GET', `**/api/metadata/${dataId}`, mockMetaInfo);

    cy.intercept('GET', '**/api/data/**', { statusCode: 200, body: { data: {}, meta: {} } });
    cy.intercept('GET', `**/api/companies/${companyId}/info`, {
      statusCode: 200,
      body: {},
    });
    cy.intercept('GET', '**/community/company-role-assignments*', { statusCode: 200, body: [] });
    cy.intercept('GET', '**/api/company-rights/**', { statusCode: 200, body: [] });
    cy.intercept('HEAD', `**/community/company-ownership/${companyId}`, { statusCode: 200, body: [] });
    cy.intercept('HEAD', '**/community/company-role-assignments/CompanyOwner/**', { statusCode: 200, body: [] });

    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });

    const mount = getMountingFunction({ keycloak: keycloakMockWithReviewer });

    mount(DatasetReviewOverview, {
      props: { dataId },
      global: {
        plugins: [[VueQueryPlugin, { queryClient }], PrimeVue],
      },
    });
  }

  // LOADING AND ERROR BANNERS
  it('Shows and then hides the loading banner during the initial load', () => {
    mountPage();
    cy.contains(/Loading Company Information/i).should('exist');
    cy.contains(/Loading Company Information/i, { timeout: 10000 }).should('not.exist');
    cy.get('[data-test="companyInformationBanner"]').should('be.visible');
  });

  it('shows an error when company data fails to load', () => {
    mountPage();
    cy.intercept('GET', `**/api/companies/${companyId}`, { statusCode: 500 });
    cy.contains('Failed to load dataset review or company information', { timeout: 10000 }).should('be.visible');
  });

  it('shows an error when dataset review fails to load', () => {
    cy.intercept('GET', '**/qa/dataset-reviews/**', {
      statusCode: 500,
    });

    mountPage();
    cy.contains('Failed to load dataset review or company information', { timeout: 10000 }).should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('not.exist');
  });

  // COMPANY INFORMATION BANNER
  it('Company Information banner is visible', () => {
    mountPage();
    cy.contains(/Loading Company Information/i, { timeout: 10000 }).should('not.exist');
    cy.get('[data-test="companyInformationBanner"]').should('be.visible');
  });

  // TABLE HEADER
  it('Table header renders the framework name', () => {
    mountPage();
    cy.contains(/Loading Company Information/i, { timeout: 10000 }).should('not.exist');

    cy.contains(/sfdr/i).should('exist');
  });

  it('hide empty fields toggle is checked by default and can be toggled', () => {
    mountPage();
    cy.contains(/Loading Company Information/i, { timeout: 10000 }).should('not.exist');

    cy.get('#hideEmptyDataToggleButton').should('be.checked');
    cy.get('#hideEmptyDataToggleButton').click();
    cy.get('#hideEmptyDataToggleButton').should('not.be.checked');
  });

  // BUTTONS
  it('allows assigning the review to the current user', () => {
    mountPage();
    cy.contains(/Loading Company Information/i, { timeout: 10000 }).should('not.exist');

    cy.contains('ASSIGN YOURSELF').should('exist');
    cy.contains('Currently assigned to:').should('exist');
    cy.contains('Assigned to you').should('not.exist');

    cy.contains('ASSIGN YOURSELF').click();

    cy.contains('ASSIGN YOURSELF').should('not.exist');
    cy.contains('Assigned to you').should('exist');
    cy.contains('Currently assigned to:').should('not.exist');
  });

  it('wires reject and finish review buttons', () => {
    // Note from Florian: These will fail once the button functionality is implemented, but that is intended to alert the developer that the test needs to be updated to reflect the actual logic instead of the placeholder alert logic
    mountPage();
    cy.contains(/Loading Company Information/i, { timeout: 10000 }).should('not.exist');

    cy.window().then((win) => {
      cy.stub(win, 'alert').as('alert');
    });

    cy.contains('REJECT DATASET').click();
    cy.get('@alert').should('have.been.calledWith', 'Reject logic here');

    cy.contains('FINISH REVIEW').click();
    cy.get('@alert').should('have.been.calledWith', 'Finish review logic here');
  });

  // COMPARISON TABLE CONTENTS
  it('Renders the comparison table with the correct (number of) headers', () => {
    mountPage();
    cy.wait('@getCompanyInfo');
    cy.wait('@getMetaInfo');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"] thead tr th').should('have.length', 5);
  });
});
