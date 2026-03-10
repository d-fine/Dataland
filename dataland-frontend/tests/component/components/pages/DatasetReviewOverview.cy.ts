import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import { type DataMetaInformation, QaStatus, type StoredCompany } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import DatasetReviewOverview from '@/components/pages/DatasetReviewOverview.vue';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { type DatasetReviewResponse, DatasetReviewState } from '@clients/qaservice';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { computed } from 'vue';
import type Keycloak from 'keycloak-js';

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
    qaReporters: [
      {
        reporterUserId: 'reporter-user-id-1',
        reporterUserName: 'reporter-user-1',
        reporterEmailAddress: 'user1@gmail.com',
        reporterCompanyId: 'reporter-company-id-1',
      },
      {
        reporterUserId: 'reporter-user-id-2',
        reporterUserName: 'reporter-user-2',
        reporterEmailAddress: 'user2@gmail.com',
        reporterCompanyId: 'reporter-company-id-2',
      },
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
    datasetReviewNetworkError?: boolean;
    forceDatasetReviewError?: boolean;
  }): void {
    const datasetReviewResponse =
      options?.datasetReviewResponse === undefined ? baseDatasetReview : options.datasetReviewResponse;

    cy.intercept('GET', `**/api/companies/${companyId}/info`, mockCompanyInfo);
    cy.intercept('GET', `**/api/metadata/${dataId}`, mockMetaInfo);

    const detailReviewUrl = `**/qa/dataset-reviews/${datasetReviewId}`;
    const listReviewUrlMatcher = /\/qa\/dataset-reviews\?.*/;

    cy.intercept('GET', detailReviewUrl, (req) => {
      if (options?.datasetReviewNetworkError) {
        req.reply({ forceNetworkError: true });
        return;
      }
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
      if (options?.datasetReviewNetworkError) {
        req.reply({ forceNetworkError: true });
        return;
      }
      if (options?.datasetReviewStatusCode != null) {
        req.reply({ statusCode: options.datasetReviewStatusCode });
        return;
      }
      if (datasetReviewResponse === null) {
        req.reply({ statusCode: 200, body: [] });
        return;
      }
      req.reply({ statusCode: 200, body: [datasetReviewResponse] });
    });

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

    const mount = getMountingFunction();
    const keycloakPromise = Promise.resolve(keycloakMockWithReviewer as unknown as Keycloak);
    const apiClientProvider = new ApiClientProvider(keycloakPromise);

    mount(DatasetReviewOverview, {
      props: {
        datasetReviewId,
      },
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        provide: {
          getKeycloakPromise: () => keycloakPromise,
          authenticated: computed(() => true),
          apiClientProvider: computed(() => apiClientProvider),
        },
      },
    });
  }

  it('displays the correct information', () => {
    mountPage();
    cy.wait('@getDatasetReview');

    cy.contains('SFDR').should('be.visible');
    cy.contains('2 / 3 data points to review').should('be.visible');
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
    mountPage({ datasetReviewNetworkError: true });
    cy.wait('@getDatasetReview');

    cy.contains('Loading Review Information...').should('not.exist');
    cy.contains('Failed to load dataset review or company information').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('not.exist');
  });

  it('shows a fallback message when no dataset review is found', () => {
    mountPage({ datasetReviewResponse: null });
    cy.wait('@getDatasetReview');

    cy.contains('No dataset review found for this dataset.').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('not.exist');
  });

  /**
   * Mounts the DatasetReviewOverview page with the dataset review assigned to the current user.
   * @returns {void} No return value;
   */
  function mountPageAssignedToCurrentUser(): void {
    mountPage({
      datasetReviewResponse: {
        ...baseDatasetReview,
        qaJudgeUserId: keycloakMockWithReviewer.idTokenParsed?.sub ?? 'current-reviewer-id',
        qaJudgeUserName: 'Current Reviewer',
      },
    });
  }

  interface ButtonAndModalTestConfig {
    mountAssignedToCurrentUser: boolean;
    interceptUrl: string;
    interceptAlias: string;
    triggerButtonText: string;
    modalTitle: string;
    modalBody: string;
    expectedUrlSuffix: string;
    expectedStateParam?: string;
  }

  /**
   * Executes a test flow: Clicking a button opens a modal, confirming the action, and checking the API call.
   * @returns {void} No return value;
   */
  function testButtonAndModalFlow(config: ButtonAndModalTestConfig): void {
    if (config.mountAssignedToCurrentUser) {
      mountPageAssignedToCurrentUser();
    } else {
      mountPage();
    }
    cy.wait('@getDatasetReview');
    cy.intercept({ method: 'PATCH', url: config.interceptUrl }, { statusCode: 200, body: {} }).as(
      config.interceptAlias
    );

    cy.contains(config.triggerButtonText).click();
    cy.contains(config.modalTitle).should('be.visible');
    cy.contains(config.modalBody).should('be.visible');
    cy.contains('CONFIRM').click();

    cy.wait(`@${config.interceptAlias}`).then((interception) => {
      expect(interception.request.method).to.eq('PATCH');
      expect(interception.request.url).to.contain(config.expectedUrlSuffix);
      if (config.expectedStateParam != null) {
        expect(interception.request.url).to.contain(config.expectedStateParam);
      }
    });
  }

  it('opens and confirms the assign-to-me modal', () => {
    testButtonAndModalFlow({
      mountAssignedToCurrentUser: false,
      interceptUrl: '**/qa/dataset-reviews/**/reviewer',
      interceptAlias: 'setReviewer',
      triggerButtonText: 'ASSIGN YOURSELF',
      modalTitle: 'Assign Yourself',
      modalBody: 'Are you sure you want to assign this dataset review to yourself?',
      expectedUrlSuffix: `/qa/dataset-reviews/${baseDatasetReview.dataSetReviewId}/reviewer`,
    });
  });

  it('opens the reject dataset modal when assigned and performs correct API call', () => {
    testButtonAndModalFlow({
      mountAssignedToCurrentUser: true,
      interceptUrl: '**/qa/dataset-reviews/**/state**',
      interceptAlias: 'rejectReview',
      triggerButtonText: 'REJECT DATASET',
      modalTitle: 'Reject Dataset',
      modalBody: 'Are you sure you want to reject this dataset review?',
      expectedUrlSuffix: `/qa/dataset-reviews/${baseDatasetReview.dataSetReviewId}/state`,
      expectedStateParam: 'datasetReviewState=Aborted',
    });
  });

  it('opens the finish review modal when assigned and performs correct API call', () => {
    testButtonAndModalFlow({
      mountAssignedToCurrentUser: true,
      interceptUrl: '**/qa/dataset-reviews/**/state**',
      interceptAlias: 'finishReview',
      triggerButtonText: 'FINISH REVIEW',
      modalTitle: 'Finish Review',
      modalBody: 'Are you sure you want to mark this dataset review as finished?',
      expectedUrlSuffix: `/qa/dataset-reviews/${baseDatasetReview.dataSetReviewId}/state`,
      expectedStateParam: 'datasetReviewState=Finished',
    });
  });
});
