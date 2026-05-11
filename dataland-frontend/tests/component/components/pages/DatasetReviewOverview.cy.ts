import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_JUDGE } from '@/utils/KeycloakRoles';
import { type CompanyInformation, type DataMetaInformation, DataTypeEnum, QaStatus } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import DatasetReviewOverview from '@/components/pages/DatasetReviewOverview.vue';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { type DatasetJudgementResponse, DatasetJudgementState } from '@clients/qaservice';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { computed } from 'vue';

const dataId = 'test-data-id';
const datasetJudgementId = 'test-judgement-id';
const companyId = '9af067dc-8280-4172-8974-1ae363c56260';
const reportingPeriod = '2021';
const framework = DataTypeEnum.Sfdr;
const url = 'testUrl';

const mockMetaInfo: DataMetaInformation = {
  dataId: dataId,
  companyId: companyId,
  dataType: framework,
  uploadTime: Date.now(),
  reportingPeriod: reportingPeriod,
  currentlyActive: true,
  qaStatus: QaStatus.Pending,
  ref: url,
};
/**
 * Verifies that all links within a warning element have the expected href.
 * @param subject - The Cypress chainable of the warning element to check links within.
 */
function checkWarningLinks(subject: Cypress.Chainable<JQuery<HTMLElement>>): void {
  subject.within(() => {
    cy.get('a').each((link) => {
      cy.wrap(link).should('have.attr', 'href', mockMetaInfo.ref);
    });
  });
}

describe('DatasetReviewOverview page details', () => {
  const keycloakMockWithJudge = minimalKeycloakMock({
    userId: 'current-judge-id',
    roles: [KEYCLOAK_ROLE_JUDGE],
  });

  const mockCompanyInfo: CompanyInformation = {
    companyName: 'd-clare',
    headquarters: 'Frankfurt',
    identifiers: { Lei: ['1234567890'] },
    sector: 'Imaginary Sector',
    countryCode: 'DE',
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
    companyInfo?: CompanyInformation;
    requestCount?: number;
    metaData?: DataMetaInformation[];
  }): void {
    const datasetJudgementResponse =
      options?.datasetJudgementResponse === undefined ? baseDatasetJudgement : options.datasetJudgementResponse;
    cy.intercept('POST', '**/data-sourcing/enhanced-requests/search/count', {
      statusCode: 200,
      body: options?.requestCount ?? 1,
    });
    cy.intercept('GET', `**/api/companies/${companyId}/info`, options?.companyInfo ?? mockCompanyInfo);
    cy.intercept('GET', `**/api/metadata/${dataId}`, mockMetaInfo);
    cy.intercept('POST', '**/api/metadata/filters', {
      statusCode: 200,
      body: options?.metaData ?? [mockMetaInfo],
    }).as('getMetaDataFilters');

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
    const keycloakPromise = Promise.resolve(keycloakMockWithJudge);
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
    allDataPointsReviewed?: boolean;
  }

  /**
   * Executes a test flow: Clicking a button opens a modal, confirming the action, and checking the API call.
   * @returns {void} No return value;
   */
  function testButtonAndModalFlow(config: ButtonAndModalTestConfig): void {
    if (config.mountAssignedToCurrentUser) {
      if (config.allDataPointsReviewed) {
        const fullyReviewedDataPoints = Object.fromEntries(
          Object.entries(baseDatasetJudgement.dataPoints).map(([key, dataPoint]) => [
            key,
            { ...dataPoint, acceptedSource: dataPoint.acceptedSource ?? 'Qa' },
          ])
        ) as DatasetJudgementResponse['dataPoints'];

        mountPage({
          datasetJudgementResponse: {
            ...baseDatasetJudgement,
            qaJudgeUserId: keycloakMockWithJudge.idTokenParsed?.sub ?? 'current-judgement-id',
            qaJudgeUserName: 'Current Judge',
            dataPoints: fullyReviewedDataPoints,
          },
        });
      } else {
        mountPageAssignedToCurrentUser();
      }
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
      modalBody:
        'Are you sure you want to reject the dataset and all ' +
        'underlying data points? This action will finish the review and cannot be undone.',
      expectedUrlSuffix: `/qa/dataset-judgements/${baseDatasetJudgement.dataSetJudgementId}/state`,
      expectedStateParam: 'datasetJudgementState=FinishedWithDatasetRejection',
    });
  });

  it('Finish Review button is disabled when not all data points are reviewed', () => {
    mountPageAssignedToCurrentUser();
    cy.wait('@getDatasetJudgement');
    cy.contains('FINISH REVIEW').should('be.visible').and('be.disabled');
  });

  it('opens the finish review modal when assigned and performs correct API call', () => {
    testButtonAndModalFlow({
      mountAssignedToCurrentUser: true,
      allDataPointsReviewed: true,
      interceptUrl: '**/qa/dataset-judgements/**/state**',
      interceptAlias: 'finishReview',
      triggerButtonText: 'FINISH REVIEW',
      modalTitle: 'Finish Review',
      modalBody: 'Are you sure you want to mark this dataset review as finished?',
      expectedUrlSuffix: `/qa/dataset-judgements/${baseDatasetJudgement.dataSetJudgementId}/state`,
      expectedStateParam: 'datasetJudgementState=Finished',
    });
  });

  describe('QARG pre-check warnings', () => {
    const viewedDataEntry = { ...mockMetaInfo, uploadTime: 2000 };
    const olderDataEntry = { ...mockMetaInfo, dataId: 'older-data-id', uploadTime: 1000 };
    const newerDataEntry = { ...mockMetaInfo, dataId: 'newer-data-id', uploadTime: 3000 };
    const acceptedDataEntry = {
      ...mockMetaInfo,
      dataId: 'accepted-data-id',
      qaStatus: QaStatus.Accepted,
    };

    it('shows an error warning when there is no related data request with status Open or Processing', () => {
      mountPage({ requestCount: 0 });
      cy.wait('@getDatasetJudgement');

      cy.get('[data-test="review-warning-invalid-request-state"]').should('be.visible');
    });

    it('shows a warning when the company has no assigned sector', () => {
      const companyWithoutSector = {
        ...mockCompanyInfo,
        sector: undefined,
      };
      mountPage({ companyInfo: companyWithoutSector });
      cy.wait('@getDatasetJudgement');

      cy.get('[data-test="review-warning-missing-sector"]').should('be.visible');
    });

    it('shows a warning when there is already an accepted dataset for the same period and framework', () => {
      mountPage({ metaData: [viewedDataEntry, acceptedDataEntry] });
      cy.wait('@getDatasetJudgement');
      cy.wait('@getMetaDataFilters');

      const warning = cy.get('[data-test="review-warning-accepted-duplicate"]').should('be.visible');
      checkWarningLinks(warning);
    });

    it('shows an info message when there are multiple pending datasets and the current one is the newest', () => {
      mountPage({ metaData: [viewedDataEntry, olderDataEntry] });
      cy.wait('@getDatasetJudgement');
      cy.wait('@getMetaDataFilters');

      const warning = cy.get('[data-test="review-warning-pending-duplicate"]').should('be.visible');
      checkWarningLinks(warning);
    });

    it('shows an error when there are multiple pending datasets and the current one is not the newest', () => {
      mountPage({ metaData: [viewedDataEntry, newerDataEntry] });
      cy.wait('@getDatasetJudgement');
      cy.wait('@getMetaDataFilters');

      const warning = cy.get('[data-test="review-warning-not-newest-pending"]').should('be.visible');
      checkWarningLinks(warning);
    });

    it('shows no warnings when everything is fine', () => {
      mountPage({ metaData: [viewedDataEntry] });
      cy.wait('@getDatasetJudgement');
      cy.wait('@getMetaDataFilters');

      cy.get('[data-test^="review-warning-"]').should('not.exist');
    });

    describe('EU taxonomy framework family', () => {
      const euTaxonomyFramework = DataTypeEnum.EutaxonomyFinancials;
      const otherEuTaxonomyFramework = DataTypeEnum.EutaxonomyFinancials202673;

      const euTaxonomyDatasetJudgement: DatasetJudgementResponse = {
        ...baseDatasetJudgement,
        dataType: euTaxonomyFramework,
      };

      const acceptedOtherEuTaxonomyEntry = {
        ...acceptedDataEntry,
        dataId: 'other-accepted-data-id',
        dataType: otherEuTaxonomyFramework,
      };
      const viewedEuTaxonomyEntry = { ...viewedDataEntry, dataType: euTaxonomyFramework };
      const olderOtherEuTaxonomyEntry = {
        ...olderDataEntry,
        dataId: 'other-pending-data-id',
        dataType: otherEuTaxonomyFramework,
      };

      it('shows a warning when there is an accepted dataset of another EU taxonomy framework', () => {
        mountPage({
          metaData: [viewedEuTaxonomyEntry, acceptedOtherEuTaxonomyEntry],
          datasetJudgementResponse: euTaxonomyDatasetJudgement,
        });
        cy.wait('@getDatasetJudgement');
        cy.wait('@getMetaDataFilters');

        const warning = cy.get('[data-test="review-warning-accepted-duplicate"]').should('be.visible');
        checkWarningLinks(warning);
      });

      it('shows a warning when there are multiple pending EU taxonomy datasets across different types', () => {
        mountPage({
          metaData: [viewedEuTaxonomyEntry, olderOtherEuTaxonomyEntry],
          datasetJudgementResponse: euTaxonomyDatasetJudgement,
        });
        cy.wait('@getDatasetJudgement');
        cy.wait('@getMetaDataFilters');

        const warning = cy.get('[data-test="review-warning-pending-duplicate"]').should('be.visible');
        checkWarningLinks(warning);
      });
    });
  });
});
