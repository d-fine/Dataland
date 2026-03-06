import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import { type DataMetaInformation, QaStatus, type StoredCompany } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import DatasetReviewOverview from '@/components/pages/DatasetReviewOverview.vue';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { type DatasetReviewResponse, DatasetReviewState } from '@clients/qaservice';

describe('DatasetReviewOverview page details', () => {
  const keycloakMockWithReviewer = minimalKeycloakMock({
    userId: 'current-reviewer-id',
    roles: [KEYCLOAK_ROLE_ADMIN],
  });

  const dataId = 'test-data-id';
  const datasetReviewId = 'test-review-id';
  const companyId = '9af067dc-8280-4172-8974-1ae363c56260';
  const reportingPeriod = '2021';
  const framework = 'sfdr';

  const mockMetaInfo: DataMetaInformation = {
    dataId: dataId,
    companyId: companyId,
    dataType: framework,
    uploadTime: Date.now(),
    reportingPeriod: reportingPeriod,
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

  const baseDatasetReview: DatasetReviewResponse = {
    dataSetReviewId: datasetReviewId,
    datasetId: dataId,
    companyId: companyId,
    reportingPeriod: reportingPeriod,
    dataType: framework,
    reviewState: DatasetReviewState.Pending,
    qaJudgeUserId: 'assigned-reviewer-id',
    qaJudgeUserName: 'Assigned Reviewer',
    qaReporterCompanies: [
      { reporterCompanyId: 'reporter-company-1', reportCompanyName: 'Reporter Company 1' },
      { reporterCompanyId: 'reporter-company-2', reportCompanyName: 'Reporter Company 2' },
    ],
    dataPoints: {
      datapoint1: {
        dataPointType: 'datapoint1',
        qaReports: [],
        acceptedSource: null,
      },
      datapoint2: {
        dataPointType: 'datapoint2',
        qaReports: [],
        acceptedSource: 'Qa',
      },
      datapoint3: {
        dataPointType: 'datapoint3',
        qaReports: [],
        acceptedSource: null,
      },
    } as unknown as DatasetReviewResponse['dataPoints'],
  };

  /**
   * Mounts the DatasetReviewOverview page pre-configured for tests.
   * @returns {void} No return value; the function performs side-effects
   *   (network stubs and mounting) necessary for the tests.
   */
  function mountPage(options?: {
    datasetReviewResponse?: DatasetReviewResponse | null;
    datasetReviewStatusCode?: number;
  }): void {
    const datasetReviewResponse = options?.datasetReviewResponse ?? baseDatasetReview;

    cy.intercept('GET', `**/api/companies/${companyId}/info`, mockCompanyInfo).as('getCompanyInfo');
    cy.intercept('GET', `**/api/metadata/${dataId}`, mockMetaInfo).as('getMetaInfo');

    const detailReviewUrl = `**/qa/dataset-reviews/${datasetReviewId}`;
    const listReviewUrlMatcher = /\/qa\/dataset-reviews\?.*/;

    cy.intercept('GET', detailReviewUrl, (req) => {
      if (options?.datasetReviewStatusCode != null) {
        req.reply({ statusCode: options.datasetReviewStatusCode });
        return;
      }
      if (datasetReviewResponse === null) {
        req.reply({ statusCode: 200, body: null as unknown as object });
        return;
      }
      req.reply({ statusCode: 200, body: datasetReviewResponse });
    }).as('getDatasetReview');

    cy.intercept('GET', listReviewUrlMatcher, (req) => {
      if (options?.datasetReviewStatusCode != null) {
        req.reply({ statusCode: options.datasetReviewStatusCode });
        return;
      }
      if (datasetReviewResponse === null) {
        req.reply({ statusCode: 200, body: [] });
        return;
      }
      req.reply({ statusCode: 200, body: [datasetReviewResponse] });
    }).as('getDatasetReviewList');

    cy.intercept('GET', '**/api/data/**', { statusCode: 200, body: { data: {}, meta: {} } });
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
      props: { dataId, datasetReviewId },
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    });
  }

  it('displays the correct information', () => {
    mountPage();
    cy.wait('@getDatasetReview');

    cy.contains('SFDR').should('be.visible');
    cy.contains('2 / 3 data points to review').should('be.visible');
    cy.contains('Data extracted from:').should('be.visible');
    cy.contains('Annual_Report_2024').should('be.visible');
    cy.contains('All documents').should('be.visible');
    cy.get('[data-test="companyInformationBanner"]').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"] thead tr th').should('have.length', 5);
  });

  it('shows assignment button when not assigned to the current user', () => {
    mountPage();
    cy.wait('@getDatasetReview');

    cy.contains('ASSIGN YOURSELF').should('be.visible');
    cy.contains('Currently assigned to:').should('be.visible');
    cy.contains('Assigned Reviewer').should('be.visible');
    cy.contains('REJECT DATASET').should('not.exist');
    cy.contains('FINISH REVIEW').should('not.exist');
  });

  it('shows reviewer action buttons when assigned to the current user', () => {
    mountPage({
      datasetReviewResponse: {
        ...baseDatasetReview,
        qaJudgeUserId: keycloakMockWithReviewer.idTokenParsed?.sub ?? 'current-reviewer-id',
        qaJudgeUserName: 'Current Reviewer',
      },
    });
    cy.wait('@getDatasetReview');

    cy.contains('Assigned to you').should('be.visible');
    cy.contains('REJECT DATASET').should('be.visible');
    cy.contains('FINISH REVIEW').should('be.visible');
    cy.contains('ASSIGN YOURSELF').should('not.exist');
  });

  it('defaults the hide empty fields toggle to on and allows toggling', () => {
    mountPage();
    cy.wait('@getDatasetReview');

    cy.get('#hideEmptyDataToggleButton').should('be.checked');
    cy.contains('Hide empty fields').should('be.visible');
    cy.get('#hideEmptyDataToggleButton').click();
    cy.get('#hideEmptyDataToggleButton').should('not.be.checked');
  });

  it('shows an error message when loading the dataset review fails', () => {
    mountPage({ datasetReviewStatusCode: 500 });
    cy.wait('@getDatasetReview');

    cy.contains('Failed to load dataset review or company information').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('not.exist');
  });

  it('shows a fallback message when no dataset review is found', () => {
    mountPage({ datasetReviewResponse: null });
    cy.wait('@getDatasetReview');

    cy.contains('No dataset review found for this dataset.').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('not.exist');
  });

  it('opens and confirms the assign-to-me modal', () => {
    mountPage();
    cy.wait('@getDatasetReview');

    cy.intercept({ method: 'PATCH', url: '**/qa/dataset-reviews/**/reviewer' }, { statusCode: 200, body: {} }).as(
      'setReviewer'
    );

    cy.contains('ASSIGN YOURSELF').click();
    cy.contains('Assign Yourself').should('be.visible');
    cy.contains('Are you sure you want to assign this dataset review to yourself?').should('be.visible');
    cy.contains('CONFIRM').click();

    cy.wait('@setReviewer').then((interception) => {
      expect(interception.request.method).to.eq('PATCH');
      expect(interception.request.url).to.contain(`/qa/dataset-reviews/${baseDatasetReview.dataSetReviewId}/reviewer`);
    });
  });

  it('opens the reject dataset modal when assigned', () => {
    mountPage({
      datasetReviewResponse: {
        ...baseDatasetReview,
        qaJudgeUserId: keycloakMockWithReviewer.idTokenParsed?.sub ?? 'current-reviewer-id',
        qaJudgeUserName: 'Current Reviewer',
      },
    });
    cy.wait('@getDatasetReview');

    cy.intercept({ method: 'PATCH', url: '**/qa/dataset-reviews/**/state**' }, { statusCode: 200, body: {} }).as(
      'setReviewState'
    );

    cy.contains('REJECT DATASET').click();
    cy.contains('Reject Dataset').should('be.visible');
    cy.contains('Are you sure you want to reject this dataset review?').should('be.visible');
    cy.contains('CONFIRM').click();

    cy.wait('@setReviewState').then((interception) => {
      expect(interception.request.method).to.eq('PATCH');
      expect(interception.request.url).to.contain(`/qa/dataset-reviews/${baseDatasetReview.dataSetReviewId}/state`);
      expect(interception.request.url).to.contain('datasetReviewState=Aborted');
    });
  });

  it('opens the finish review modal when assigned', () => {
    mountPage({
      datasetReviewResponse: {
        ...baseDatasetReview,
        qaJudgeUserId: keycloakMockWithReviewer.idTokenParsed?.sub ?? 'current-reviewer-id',
        qaJudgeUserName: 'Current Reviewer',
      },
    });
    cy.wait('@getDatasetReview');

    cy.intercept({ method: 'PATCH', url: '**/qa/dataset-reviews/**/state**' }, { statusCode: 200, body: {} }).as(
      'finishReview'
    );

    cy.contains('FINISH REVIEW').click();
    cy.contains('Finish Review').should('be.visible');
    cy.contains('Are you sure you want to mark this dataset review as finished?').should('be.visible');
    cy.contains('CONFIRM').click();

    cy.wait('@finishReview').then((interception) => {
      expect(interception.request.method).to.eq('PATCH');
      expect(interception.request.url).to.contain(`/qa/dataset-reviews/${baseDatasetReview.dataSetReviewId}/state`);
      expect(interception.request.url).to.contain('datasetReviewState=Finished');
    });
  });
});
