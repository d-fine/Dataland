import DatasetReviewComparisonTable from '@/components/resources/datasetReview/DatasetReviewComparisonTable.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query';
import { type DataMetaInformation, DataTypeEnum, QaStatus, type SfdrData } from '@clients/backend';
import {
  AcceptedDataPointSource,
  DatasetReviewState,
  QaReportDataPointVerdict,
  type DatasetReviewResponse,
} from '@clients/qaservice';

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

  const qaReporterCompanies: DatasetReviewResponse['qaReporterCompanies'] = [
    { reporterCompanyId: 'qa-company-1', reportCompanyName: 'QA Company 1' },
    { reporterCompanyId: 'qa-company-2', reportCompanyName: 'QA Company 2' },
  ];

  const baseDatasetReview: DatasetReviewResponse = {
    dataSetReviewId: 'review-id',
    datasetId: dataId,
    companyId: companyId,
    dataType: framework as DatasetReviewResponse['dataType'],
    reportingPeriod: reportingPeriod,
    reviewState: DatasetReviewState.Pending,
    qaReporterCompanies: qaReporterCompanies,
    dataPoints: {
      plainDateSfdrDataDate: {
        dataPointType: 'plainDateSfdrDataDate',
        qaReports: [
          {
            qaReportId: 'qa-report-0',
            verdict: QaReportDataPointVerdict.QaAccepted,
            reporterUserId: 'qa-user-1',
            reporterCompanyId: 'qa-company-1',
          },
        ],
        acceptedSource: AcceptedDataPointSource.Original,
      },
      extendedEnumFiscalYearDeviation: {
        dataPointType: 'extendedEnumFiscalYearDeviation',
        qaReports: [
          {
            qaReportId: 'qa-report-1',
            verdict: QaReportDataPointVerdict.QaAccepted,
            reporterUserId: 'qa-user-1',
            reporterCompanyId: 'qa-company-1',
          },
          {
            qaReportId: 'qa-report-2',
            verdict: QaReportDataPointVerdict.QaRejected,
            correctedData: JSON.stringify({ value: 'No Deviation' }),
            reporterUserId: 'qa-user-2',
            reporterCompanyId: 'qa-company-2',
          },
        ],
        acceptedSource: AcceptedDataPointSource.Qa,
        companyIdOfAcceptedQaReport: 'qa-company-2',
      },
      extendedDateFiscalYearEnd: {
        dataPointType: 'extendedDateFiscalYearEnd',
        qaReports: [
          {
            qaReportId: 'qa-report-3',
            verdict: QaReportDataPointVerdict.QaAccepted,
            reporterUserId: 'qa-user-1',
            reporterCompanyId: 'qa-company-1',
          },
          {
            qaReportId: 'qa-report-4',
            verdict: QaReportDataPointVerdict.QaRejected,
            correctedData: JSON.stringify({ value: '2023-11-30' }),
            reporterUserId: 'qa-user-2',
            reporterCompanyId: 'qa-company-2',
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
    datasetReview?: DatasetReviewResponse;
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
      },
    }).as('getFrameworkData');

    // @ts-ignore
    cy.mountWithPlugins(DatasetReviewComparisonTable, {
      keycloak: minimalKeycloakMock({}),
      props: {
        framework: framework,
        dataId: dataId,
        searchQuery: options?.searchQuery ?? '',
        datasetReview: options?.datasetReview ?? baseDatasetReview,
        dataMetaInformation: mockMetaInformation,
        hideEmptyFields: options?.hideEmptyFields ?? false,
      },
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    });

    cy.wait('@getFrameworkData');
  }

  it('renders headers and KPI labels for QA companies', () => {
    mountComponent();

    cy.get('table[aria-label="Dataset review comparison table"]').should('exist');
    cy.contains('th', 'Original Datapoint').should('be.visible');
    cy.contains('th', 'Corrected Datapoint').should('be.visible');
    cy.contains('span', 'QA Company 1').should('be.visible');
    cy.contains('span', 'QA Company 2').should('be.visible');
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
});
