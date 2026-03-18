import DatasetReviewComparisonTable from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { type CompanyReport, type DataMetaInformation, DataTypeEnum, QaStatus, type SfdrData } from '@clients/backend';
import {
  AcceptedDataPointSource,
  DatasetJudgementState,
  QaReportDataPointVerdict,
  type DatasetJudgementResponse,
} from '@clients/qaservice';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { computed } from 'vue';

describe('DatasetReviewComparisonTable component tests', () => {
  const dataId = 'test-data-id';
  const companyId = 'company-id';
  const reportingPeriod = '2023';
  const framework = DataTypeEnum.Sfdr;

  const mockMetaInformation: DataMetaInformation = {
    dataId: dataId,
    companyId: companyId,
    dataType: framework,
    uploadTime: Date.now(),
    reportingPeriod: reportingPeriod,
    currentlyActive: true,
    qaStatus: QaStatus.Pending,
    ref: null,
  };

  const baseSfdrData: SfdrData = {
    general: {
      general: {
        dataDate: '2024-01-01',
        fiscalYearDeviation: {
          value: 'Deviation',
        },
        fiscalYearEnd: {
          value: '2023-12-31',
        },
      },
    },
  } as unknown as SfdrData;

  const qaReporter1 = {
    reporterUserId: 'reporter-user-id-1',
    reporterUserName: 'reporter-user-1',
    reporterEmailAddress: 'user1@gmail.com',
    reporterCompanyId: 'reporter-company-id-1',
  };

  const qaReporter2 = {
    reporterUserId: 'reporter-user-id-2',
    reporterUserName: 'reporter-user-2',
    reporterEmailAddress: 'user2@gmail.com',
    reporterCompanyId: 'reporter-company-id-2',
  };

  const qaReporterUsers: DatasetJudgementResponse['qaReporters'] = [
    {
      reporterUserId: qaReporter1.reporterUserId,
      reporterUserName: qaReporter1.reporterUserName,
      reporterEmailAddress: qaReporter1.reporterEmailAddress,
    },
    {
      reporterUserId: qaReporter2.reporterUserId,
      reporterUserName: qaReporter2.reporterUserName,
      reporterEmailAddress: qaReporter2.reporterEmailAddress,
    },
  ];

  const baseDatasetReview: DatasetJudgementResponse = {
    dataSetJudgementId: 'review-id',
    datasetId: dataId,
    companyId: companyId,
    dataType: framework as DatasetJudgementResponse['dataType'],
    reportingPeriod: reportingPeriod,
    judgementState: DatasetJudgementState.Pending,
    qaReporters: qaReporterUsers,
    dataPoints: {
      plainDateSfdrDataDate: {
        dataPointType: 'plainDateSfdrDataDate',
        dataPointId: 'data-point-id-1',
        qaReports: [
          {
            dataPointId: 'data-point-id-1',
            dataPointType: 'type-id-1',
            qaReportId: 'qa-report-0',
            verdict: QaReportDataPointVerdict.QaAccepted,
            reporterUserId: qaReporter1.reporterUserId,
            uploadTime: 1234,
            active: true,
            comment: 'comment',
          },
        ],
        acceptedSource: AcceptedDataPointSource.Original,
      },
      extendedEnumFiscalYearDeviation: {
        dataPointType: 'extendedEnumFiscalYearDeviation',
        dataPointId: 'data-point-id-2',
        qaReports: [
          {
            dataPointId: 'data-point-id-2',
            dataPointType: 'type-id-2',
            qaReportId: 'qa-report-1',
            verdict: QaReportDataPointVerdict.QaAccepted,
            reporterUserId: qaReporter1.reporterUserId,
            uploadTime: 1233,
            active: true,
            comment: 'comment',
          },
          {
            dataPointId: 'data-point-id-2',
            dataPointType: 'type-id-2',
            qaReportId: 'qa-report-2',
            verdict: QaReportDataPointVerdict.QaRejected,
            correctedData: JSON.stringify({ value: 'No Deviation' }),
            reporterUserId: qaReporter2.reporterUserId,
            uploadTime: 9999,
            active: true,
            comment: 'comment',
          },
        ],
        acceptedSource: AcceptedDataPointSource.Qa,
        reporterUserIdOfAcceptedQaReport: qaReporter2.reporterUserId,
      },
      extendedDateFiscalYearEnd: {
        dataPointType: 'extendedDateFiscalYearEnd',
        dataPointId: 'data-point-id-3',
        qaReports: [
          {
            dataPointId: 'data-point-id-3',
            dataPointType: 'type-id-3',
            qaReportId: 'qa-report-3',
            verdict: QaReportDataPointVerdict.QaAccepted,
            reporterUserId: qaReporter1.reporterUserId,
            uploadTime: 9999,
            active: true,
            comment: 'comment',
          },
          {
            dataPointId: 'data-point-id-3',
            dataPointType: 'type-id-3',
            qaReportId: 'qa-report-4',
            verdict: QaReportDataPointVerdict.QaRejected,
            correctedData: JSON.stringify({ value: '2023-11-30' }),
            reporterUserId: qaReporter2.reporterUserId,
            uploadTime: 9999,
            active: true,
            comment: 'comment',
          },
        ],
        acceptedSource: AcceptedDataPointSource.Custom,
        customValue: '2023-12-15',
      },
    },
  };

  /**
   * Helper function to mount the component with default or overridden options.
   */
  function mountComponent(options?: {
    datasetReview?: DatasetJudgementResponse;
    searchQuery?: string;
    hideEmptyFields?: boolean;
    data?: SfdrData;
  }): void {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });

    cy.intercept('GET', '**/api/data/**', {
      statusCode: 200,
      body: {
        data: options?.data ?? baseSfdrData,
        meta: {},
        reportingPeriod: reportingPeriod,
      },
    }).as('getFrameworkData');

    const mount = getMountingFunction();
    const keycloakPromise = Promise.resolve(minimalKeycloakMock({}) as unknown as Keycloak);
    const apiClientProvider = new ApiClientProvider(keycloakPromise);

    mount(DatasetReviewComparisonTable, {
      props: {
        framework,
        dataId,
        searchQuery: options?.searchQuery ?? '',
        datasetReview: options?.datasetReview ?? baseDatasetReview,
        dataMetaInformation: mockMetaInformation,
        hideEmptyFields: options?.hideEmptyFields ?? false,
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

    cy.wait('@getFrameworkData');
  }

  it('renders headers and KPI labels for QA companies', () => {
    mountComponent();

    cy.get('table[aria-label="Dataset review comparison table"]').should('exist');
    cy.contains('th', 'Original Datapoint').should('be.visible');
    cy.contains('th', 'Corrected Datapoint').should('be.visible');
    cy.contains('span', qaReporter1.reporterUserName).should('be.visible');
    cy.contains('span', qaReporter2.reporterUserName).should('be.visible');
    cy.contains('th', 'Custom Datapoint').should('be.visible');
    cy.get('thead tr th').should('have.length', 5);
    cy.contains('span', 'Data Date').should('be.visible');
    cy.contains('span', 'Fiscal Year Deviation').should('be.visible');
  });

  it('shows accepted and rejected icons for original and QA sources', () => {
    mountComponent();

    cy.contains('span', 'Data Date')
      .closest('tr')
      .within(() => {
        cy.get('td').eq(1).find('.accepted-check').should('exist');
        cy.get('td').eq(1).find('.rejected-check').should('not.exist');
        cy.get('td').eq(2).find('.accepted-check').should('not.exist');
        cy.get('td').eq(2).find('.rejected-check').should('not.exist');
        cy.get('td').eq(3).find('.rejected-check').should('not.exist');
      });

    cy.contains('span', 'Fiscal Year Deviation')
      .closest('tr')
      .within(() => {
        cy.get('td').eq(1).find('.rejected-check').should('exist');
        cy.get('td').eq(2).find('.rejected-check').should('not.exist');
        cy.get('td').eq(3).find('.accepted-check').should('exist');
      });

    cy.contains('span', 'Fiscal Year End')
      .closest('tr')
      .within(() => {
        cy.get('td').eq(1).find('.rejected-check').should('exist');
        cy.get('td').eq(2).find('.rejected-check').should('not.exist');
        cy.get('td').eq(3).find('.rejected-check').should('exist');
        cy.get('td').eq(4).find('.accepted-check').should('exist');
      });
  });

  it('hides empty KPI rows when hideEmptyFields is true', () => {
    mountComponent({ hideEmptyFields: true });

    cy.contains('span', 'Scope 2 GHG emissions').should('not.exist');
    cy.contains('span', 'Data Date').should('be.visible');
  });

  it('shows empty KPI rows when hideEmptyFields is false', () => {
    mountComponent({ hideEmptyFields: false });

    cy.contains('span', 'Scope 2 GHG emissions').should('be.visible');
  });

  it('renders the company reports banner with referenced reports', () => {
    const reportName = 'Annual_Report_2024';
    const dataWithReports: SfdrData = {
      ...baseSfdrData,
      general: {
        ...baseSfdrData.general,
        general: {
          ...baseSfdrData.general?.general,
          referencedReports: {
            [reportName]: {} as CompanyReport,
          },
        },
      },
    } as SfdrData;

    mountComponent({ data: dataWithReports });

    cy.get('[data-test="multipleReportsBanner"]').should('be.visible');
    cy.get(`[data-test="report-link-${reportName}"]`).should('be.visible');
  });
});
