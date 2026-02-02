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

before(function () {
  /**
   * Builds an extended stored data request object and assures type-safety.
   * @param dataType to include in the data request
   * @param reportingPeriod to include in the data request
   * @param companyName to include in the data request
   * @param companyId to include in the data request
   * @param state to set in the data request
   * @param requestPriority to set in the data request
   * @param dataSourcingState to set in the data request
   * @param dateOfNextSourcingAttempt to set in the data request
   * @returns an extended sorted data request object
   */
  function buildExtendedStoredRequest(
    dataType: DataTypeEnum,
    reportingPeriod: string,
    companyName: string,
    companyId: string,
    state: RequestState,
    requestPriority: RequestPriority,
    dataSourcingState: DataSourcingState | undefined,
    dateOfNextSourcingAttempt: string | undefined
  ): DataSourcingEnhancedRequest {
    return {
      id: dummyRequestId,
      userId: 'some-user-id',
      creationTimestamp: 1709204495770,
      dataType: dataType,
      reportingPeriod: reportingPeriod,
      companyId: companyId,
      companyName: companyName,
      lastModifiedDate: 1709204495770,
      state: state,
      dataSourcingDetails: {
        dataSourcingEntityId: 'entity-id',
        dataSourcingState: dataSourcingState,
        dateOfNextDocumentSourcingAttempt: dateOfNextSourcingAttempt,
        documentCollectorName: undefined,
        dataExtractorName: undefined,
      },
      requestPriority: requestPriority,
    };
  }

  mockDataRequests.push(
    buildExtendedStoredRequest(
      DataTypeEnum.Lksg,
      '2020',
      'companyProcessed',
      'compA',
      RequestState.Processed,
      RequestPriority.Low,
      DataSourcingState.NonSourceable,
      dummyDate
    ),
    buildExtendedStoredRequest(
      DataTypeEnum.Sfdr,
      '2022',
      'companyOpen',
      'someId',
      RequestState.Open,
      RequestPriority.Low,
      undefined,
      undefined
    ),
    buildExtendedStoredRequest(
      DataTypeEnum.Sfdr,
      '2022',
      'companyProcessing',
      'someId',
      RequestState.Processing,
      RequestPriority.Low,
      DataSourcingState.DataVerification,
      undefined
    ),
    buildExtendedStoredRequest(
      DataTypeEnum.Sfdr,
      '9999',
      'z-company-that-will-always-be-sorted-to-bottom',
      'someId',
      RequestState.Withdrawn,
      RequestPriority.Low,
      DataSourcingState.DocumentSourcing,
      undefined
    ),
    buildExtendedStoredRequest(
      DataTypeEnum.EutaxonomyNonFinancials,
      '2021',
      'companyWithdrawn',
      'someId',
      RequestState.Withdrawn,
      RequestPriority.Low,
      undefined,
      undefined
    ),
    buildExtendedStoredRequest(
      DataTypeEnum.EutaxonomyFinancials,
      '1021',
      'a-company-that-will-always-be-sorted-to-top',
      'someId',
      RequestState.Open,
      RequestPriority.Low,
      undefined,
      undefined
    )
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
    cy.get('[data-test="requested-datasets-table"]').find('tr').find('td').contains(dummyDate).should('be.visible');
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

  it('Check filter functionality and reset button', function (): void {
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
