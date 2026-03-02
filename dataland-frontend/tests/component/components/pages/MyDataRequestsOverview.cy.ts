import MyDataRequestsOverview from '@/components/pages/MyDataRequestsOverview.vue';
import router from '@/router';
import { DataTypeEnum } from '@clients/backend';
import {
  RequestState,
  type DataSourcingEnhancedRequest,
  RequestPriority,
  DataSourcingState,
} from '@clients/datasourcingservice';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

const mockDataRequests: DataSourcingEnhancedRequest[] = [];
const expectedHeaders = [
  'COMPANY',
  'FRAMEWORK',
  'REPORTING PERIOD',
  'REQUESTED',
  'STATE',
  'NEXT DOCUMENT SOURCING ATTEMPT',
  'LAST UPDATED',
];
const dummyRequestId = 'dummyRequestId';
const dummyDate = '2024-11-01';

interface MockRequestParams {
  dataType: DataTypeEnum;
  reportingPeriod: string;
  companyName: string;
  companyId: string;
  state: RequestState;
  requestPriority: RequestPriority;
  dataSourcingState?: DataSourcingState;
  dateOfNextSourcingAttempt?: string;
}

/**
 * Builds an extended stored data request object and assures type-safety.
 * @param params - The parameters for building the request
 * @returns an extended stored data request object
 */
function buildExtendedStoredRequest(params: MockRequestParams): DataSourcingEnhancedRequest {
  return {
    id: dummyRequestId,
    userId: 'some-user-id',
    creationTimestamp: 1709204495770,
    dataType: params.dataType,
    reportingPeriod: params.reportingPeriod,
    companyId: params.companyId,
    companyName: params.companyName,
    lastModifiedDate: 1709204495770,
    state: params.state,
    dataSourcingDetails: {
      dataSourcingEntityId: 'entity-id',
      dataSourcingState: params.dataSourcingState,
      dateOfNextDocumentSourcingAttempt: params.dateOfNextSourcingAttempt,
      documentCollectorName: undefined,
      dataExtractorName: undefined,
    },
    requestPriority: params.requestPriority,
  };
}

before(function () {
  mockDataRequests.push(
    buildExtendedStoredRequest({
      dataType: DataTypeEnum.Lksg,
      reportingPeriod: '2020',
      companyName: 'companyProcessed',
      companyId: 'compA',
      state: RequestState.Processed,
      requestPriority: RequestPriority.Low,
      dataSourcingState: DataSourcingState.NonSourceable,
      dateOfNextSourcingAttempt: dummyDate,
    }),
    buildExtendedStoredRequest({
      dataType: DataTypeEnum.Sfdr,
      reportingPeriod: '2022',
      companyName: 'companyOpen',
      companyId: 'someId',
      state: RequestState.Open,
      requestPriority: RequestPriority.Low,
    }),
    buildExtendedStoredRequest({
      dataType: DataTypeEnum.Sfdr,
      reportingPeriod: '2022',
      companyName: 'companyProcessing',
      companyId: 'someId',
      state: RequestState.Processing,
      requestPriority: RequestPriority.Low,
      dataSourcingState: DataSourcingState.DataVerification,
    }),
    buildExtendedStoredRequest({
      dataType: DataTypeEnum.Sfdr,
      reportingPeriod: '9999',
      companyName: 'z-company-that-will-always-be-sorted-to-bottom',
      companyId: 'someId',
      state: RequestState.Withdrawn,
      requestPriority: RequestPriority.Low,
      dataSourcingState: DataSourcingState.DocumentSourcing,
    }),
    buildExtendedStoredRequest({
      dataType: DataTypeEnum.EutaxonomyNonFinancials,
      reportingPeriod: '2021',
      companyName: 'companyWithdrawn',
      companyId: 'someId',
      state: RequestState.Withdrawn,
      requestPriority: RequestPriority.Low,
    }),
    buildExtendedStoredRequest({
      dataType: DataTypeEnum.EutaxonomyFinancials,
      reportingPeriod: '1021',
      companyName: 'a-company-that-will-always-be-sorted-to-top',
      companyId: 'someId',
      state: RequestState.Open,
      requestPriority: RequestPriority.Low,
    })
  );
});
/**
 * Helper to intercept user data requests.
 * @param data - The data to return from the intercept (defaults to mockDataRequests)
 */
function interceptUserRequests({
  data = mockDataRequests,
}: {
  data?: DataSourcingEnhancedRequest[];
  alias?: string;
  matchPattern?: string;
} = {}): void {
  cy.intercept('**/data-sourcing/enhanced-requests', {
    body: data,
    status: 200,
  });
}

/**
 * Helper to mount MyDataRequestsOverview with plugins.
 * @param options - Additional options for mounting (e.g., router)
 */
function mountMyDataRequestsOverview(options: { router?: typeof router } = {}): Cypress.Chainable {
  return cy.mountWithPlugins(MyDataRequestsOverview, {
    keycloak: minimalKeycloakMock({}),
    ...options,
  });
}

describe('Component tests for the data requests search page', function (): void {
  it('Check sorting', function (): void {
    interceptUserRequests();
    mountMyDataRequestsOverview();
    const sortingColumnHeader = ['COMPANY', 'REPORTING PERIOD', 'STATE', 'FRAMEWORK'];
    for (const value of sortingColumnHeader) {
      cy.get(`table th:contains(${value})`).should('exist').click();
      cy.get('[data-test="requested-datasets-table"]')
        .find('tr')
        .find('td')
        .contains('a-company-that-will-always-be-sorted-to-top')
        .parent()
        .invoke('index')
        .should('eq', 0);
      cy.get(`table th:contains(${value})`).should('exist').click();
      cy.get('[data-test="requested-datasets-table"]')
        .find('tr')
        .find('td')
        .contains('z-company-that-will-always-be-sorted-to-bottom')
        .parent()
        .invoke('index')
        .should('eq', 0);
    }
  });

  it('Check page when there are no requested datasets', function (): void {
    interceptUserRequests({ data: [] });
    cy.spy(router, 'push').as('routerPush');
    mountMyDataRequestsOverview({ router });
    cy.get('[data-test="requested-datasets-table"]').should('not.exist');
    cy.get('[data-test="myPortfoliosButton"]').should('exist').should('be.visible').click();
    cy.get('@routerPush').should('have.been.calledWith', '/portfolios');
  });

  it('Check static layout of the search page', function () {
    const placeholder = 'Search by Company Name';
    const inputValue = 'A company name';
    interceptUserRequests();
    mountMyDataRequestsOverview();
    cy.get('[data-test="requested-datasets-table"]').should('exist');
    for (const value of expectedHeaders) {
      cy.get(`table th:contains(${value})`).should('exist');
    }
    cy.get('[data-test="requested-datasets-searchbar"]')
      .should('exist')
      .should('not.be.disabled')
      .type(inputValue)
      .should('have.value', inputValue)
      .invoke('attr', 'placeholder')
      .should('contain', placeholder);
    cy.get('[data-test="requested-datasets-frameworks"]').should('exist');
  });

  it('Check the content of the data table', function (): void {
    const expectedCompanys = [
      'companyProcessed',
      'companyOpen',
      'companyWithdrawn',
      'companyProcessing',
      'z-company-that-will-always-be-sorted-to-bottom',
      'a-company-that-will-always-be-sorted-to-top',
    ];
    const expectedReportingPeriods = ['2020', '2021', '2022'];
    interceptUserRequests();
    mountMyDataRequestsOverview();
    for (const value of expectedCompanys) {
      cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains(value).should('exist');
    }
    cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains('DummyName').should('not.exist');
    for (const value of expectedReportingPeriods) {
      cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains(value).should('exist');
    }
    cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains('2019').should('not.exist');
    cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains('1 Nov 2024').should('be.visible');
  });

  it('Check existence and functionality of searchbar', function (): void {
    interceptUserRequests();
    cy.spy(router, 'push').as('routerPush');
    mountMyDataRequestsOverview({ router });
    cy.get('[data-test="requested-datasets-searchbar"]')
      .should('exist')
      .should('not.be.disabled')
      .clear()
      .type('companyOpen');
    cy.get('[data-test="requested-datasets-searchbar"]')
      .should('exist')
      .should('not.be.disabled')
      .clear()
      .type('companyProcessed');
  });

  it('Check filter functionality for framework', function (): void {
    const expectedFrameworkNameSubstrings = [
      'SFDR',
      'EU Taxonomy',
      'for financial companies',
      'for non-financial companies',
    ];
    interceptUserRequests();
    mountMyDataRequestsOverview();
    cy.get('[data-test="requested-datasets-frameworks"]').click().get('.p-multiselect-option').contains('LkSG').click();
    cy.get('[data-test="requested-datasets-frameworks"]').click();
    for (const value of expectedFrameworkNameSubstrings) {
      cy.get(`table tbody:contains(${value})`).should('not.exist');
    }
    cy.get('[data-test="reset-filter"]').should('exist').click();
    for (const value of expectedFrameworkNameSubstrings) {
      cy.get(`table tbody:contains(${value})`).should('exist');
    }
    cy.get(`table tbody:contains("SME")`).should('not.exist');
  });

  it('Check filter functionality for state', function (): void {
    interceptUserRequests();
    mountMyDataRequestsOverview();
    cy.get('[data-test="requested-datasets-state"]')
      .click()
      .get('.p-multiselect-option')
      .contains('Data Verification')
      .click();
    cy.get('[data-test="requested-datasets-state"]').click();
    cy.get('table tbody').find('tr').should('have.length', 1);
    cy.get('table tbody').find('tr').first().find('td').contains('Data Verification').should('exist');
    cy.get('[data-test="reset-filter"]').should('exist').click();
    cy.get('table tbody').find('tr').should('have.length', mockDataRequests.length);
  });

  it('Check the functionality of rowClick event', function (): void {
    interceptUserRequests();
    cy.spy(router, 'push').as('routerPush');
    mountMyDataRequestsOverview({ router });
    cy.get('[data-test="requested-datasets-table"]').within(() => {
      cy.get('tr:last').click();
    });
    cy.get('@routerPush').should('have.been.calledWith', `/requests/${dummyRequestId}`);
  });
});
