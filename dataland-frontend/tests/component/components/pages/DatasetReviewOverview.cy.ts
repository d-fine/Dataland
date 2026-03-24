import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_JUDGE } from '@/utils/KeycloakRoles';
import { type DataMetaInformation, QaStatus, type StoredCompany } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import DatasetReviewOverview from '@/components/pages/DatasetReviewOverview.vue';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { type DatasetJudgementResponse, DatasetJudgementState } from '@clients/qaservice';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { computed } from 'vue';
import type Keycloak from 'keycloak-js';

describe('DatasetReviewOverview page details', () => {
  const keycloakMockWithJudge = minimalKeycloakMock({
    userId: 'current-judge-id',
    roles: [KEYCLOAK_ROLE_JUDGE],
  });

  const dataId = 'test-data-id';
  const datasetJudgementId = 'test-judgement-id';
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

  const baseDatasetJudgement: DatasetJudgementResponse = {
    dataSetJudgementId: datasetJudgementId,
    datasetId: dataId,
    companyId: companyId,
    reportingPeriod: reportingPeriod,
    dataType: framework,
    judgementState: DatasetJudgementState.Pending,
    qaJudgeUserId: 'assigned-judge-id',
    qaJudgeUserName: 'Assigned Judge',
    qaReporters: [
      {
        reporterUserId: 'reporter-user-id-1',
        reporterUserName: 'reporter-user-1',
        reporterEmailAddress: 'user1@gmail.com',
      },
      {
        reporterUserId: 'reporter-user-id-2',
        reporterUserName: 'reporter-user-2',
        reporterEmailAddress: 'user2@gmail.com',
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
    } as unknown as DatasetJudgementResponse['dataPoints'],
  };

  /**
   * Mounts the DatasetReviewOverview page pre-configured for tests.
   * @returns {void} No return value; the function performs side-effects
   *   (network stubs and mounting) necessary for the tests.
   */
  function mountPage(options?: {
    datasetJudgementResponse?: DatasetJudgementResponse | null;
    datasetJudgementStatusCode?: number;
    datasetJudgementNetworkError?: boolean;
    forceDatasetJudgementError?: boolean;
  }): void {
    const datasetJudgementResponse =
      options?.datasetJudgementResponse === undefined ? baseDatasetJudgement : options.datasetJudgementResponse;

    cy.intercept('GET', `**/api/companies/${companyId}/info`, mockCompanyInfo);
    cy.intercept('GET', `**/api/metadata/${dataId}`, mockMetaInfo);

    const detailJudgementUrl = `**/qa/dataset-judgements/${datasetJudgementId}`;
    const listJudgeUrlMatcher = /\/qa\/dataset-judgements\?.*/;

    cy.intercept('GET', detailJudgementUrl, (req) => {
      if (options?.datasetJudgementNetworkError) {
        req.reply({ forceNetworkError: true });
        return;
      }
      if (options?.datasetJudgementStatusCode != null) {
        req.reply({ statusCode: options.datasetJudgementStatusCode });
        return;
      }
      if (datasetJudgementResponse === null) {
        req.reply({ statusCode: 200, body: null as unknown as object });
        return;
      }
      req.reply({ statusCode: 200, body: datasetJudgementResponse });
    }).as('getDatasetJudgement');

    cy.intercept('GET', listJudgeUrlMatcher, (req) => {
      if (options?.datasetJudgementNetworkError) {
        req.reply({ forceNetworkError: true });
        return;
      }
      if (options?.datasetJudgementStatusCode != null) {
        req.reply({ statusCode: options.datasetJudgementStatusCode });
        return;
      }
      if (datasetJudgementResponse === null) {
        req.reply({ statusCode: 200, body: [] });
        return;
      }
      req.reply({ statusCode: 200, body: [datasetJudgementResponse] });
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
    const keycloakPromise = Promise.resolve(keycloakMockWithJudge as unknown as Keycloak);
    const apiClientProvider = new ApiClientProvider(keycloakPromise);

    mount(DatasetReviewOverview, {
      props: {
        datasetJudgementId: datasetJudgementId,
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
    cy.wait('@getDatasetJudgement');

    cy.contains('SFDR').should('be.visible');
    cy.contains('2 / 3 data points to review').should('be.visible');
    cy.get('[data-test="companyInformationBanner"]').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"] thead tr th').should('have.length', 5);
  });

  it('shows assignment button when not assigned to the current user', () => {
    mountPage();
    cy.wait('@getDatasetJudgement');

    cy.contains('ASSIGN YOURSELF').should('be.visible');
    cy.contains('Currently assigned to:').should('be.visible');
    cy.contains('Assigned Judge').should('be.visible');
    cy.contains('REJECT DATASET').should('not.exist');
    cy.contains('FINISH REVIEW').should('not.exist');
  });

  it('shows judge action buttons when assigned to the current user', () => {
    mountPage({
      datasetJudgementResponse: {
        ...baseDatasetJudgement,
        qaJudgeUserId: keycloakMockWithJudge.idTokenParsed?.sub ?? 'current-judge-id',
        qaJudgeUserName: 'Current Judge',
      },
    });
    cy.wait('@getDatasetJudgement');

    cy.contains('Assigned to you').should('be.visible');
    cy.contains('REJECT DATASET').should('be.visible');
    cy.contains('FINISH REVIEW').should('be.visible');
    cy.contains('ASSIGN YOURSELF').should('not.exist');
  });

  it('defaults the hide empty fields toggle to on and allows toggling', () => {
    mountPage();
    cy.wait('@getDatasetJudgement');

    cy.get('#hideEmptyDataToggleButton').should('be.checked');
    cy.contains('Hide empty fields').should('be.visible');
    cy.get('#hideEmptyDataToggleButton').click();
    cy.get('#hideEmptyDataToggleButton').should('not.be.checked');
  });

  it('shows an error message when loading the dataset judgement fails', () => {
    mountPage({ datasetJudgementNetworkError: true });
    cy.wait('@getDatasetJudgement');

    cy.contains('Loading Judgement Information...').should('not.exist');
    cy.contains('Failed to load dataset review or company information').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('not.exist');
  });

  it('shows a fallback message when no dataset judgement is found', () => {
    mountPage({ datasetJudgementResponse: null });
    cy.wait('@getDatasetJudgement');

    cy.contains('No dataset review found for this dataset.').should('be.visible');
    cy.get('[data-test="datasetReviewComparisonTable"]').should('not.exist');
  });

  /**
   * Mounts the DatasetReviewOverview page with the dataset judgement assigned to the current user.
   * @returns {void} No return value;
   */
  function mountPageAssignedToCurrentUser(): void {
    mountPage({
      datasetJudgementResponse: {
        ...baseDatasetJudgement,
        qaJudgeUserId: keycloakMockWithJudge.idTokenParsed?.sub ?? 'current-judgement-id',
        qaJudgeUserName: 'Current Judge',
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
    cy.wait('@getDatasetJudgement');
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
      interceptUrl: '**/qa/dataset-judgements/**/judge',
      interceptAlias: 'setJudge',
      triggerButtonText: 'ASSIGN YOURSELF',
      modalTitle: 'Assign Yourself',
      modalBody: 'Are you sure you want to assign this dataset review to yourself?',
      expectedUrlSuffix: `/qa/dataset-judgements/${baseDatasetJudgement.dataSetJudgementId}/judge`,
    });
  });

  it('opens the reject dataset modal when assigned and performs correct API call', () => {
    testButtonAndModalFlow({
      mountAssignedToCurrentUser: true,
      interceptUrl: '**/qa/dataset-judgements/**/state**',
      interceptAlias: 'rejectReview',
      triggerButtonText: 'REJECT DATASET',
      modalTitle: 'Reject Dataset',
      modalBody: 'Are you sure you want to reject this dataset review?',
      expectedUrlSuffix: `/qa/dataset-judgements/${baseDatasetJudgement.dataSetJudgementId}/state`,
      expectedStateParam: 'datasetJudgementState=Aborted',
    });
  });

  it('opens the finish review modal when assigned and performs correct API call', () => {
    testButtonAndModalFlow({
      mountAssignedToCurrentUser: true,
      interceptUrl: '**/qa/dataset-judgements/**/state**',
      interceptAlias: 'finishReview',
      triggerButtonText: 'FINISH REVIEW',
      modalTitle: 'Finish Review',
      modalBody: 'Are you sure you want to mark this dataset review as finished?',
      expectedUrlSuffix: `/qa/dataset-judgements/${baseDatasetJudgement.dataSetJudgementId}/state`,
      expectedStateParam: 'datasetJudgementState=Finished',
    });
  });
});
