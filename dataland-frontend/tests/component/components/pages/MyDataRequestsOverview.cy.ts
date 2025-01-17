import {
  AccessStatus,
  type ExtendedStoredDataRequest,
  RequestPriority,
  RequestStatus,
} from '@clients/communitymanager';
import MyDataRequestsOverview from '@/components/pages/MyDataRequestsOverview.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';
import router from '@/router';

const mockDataRequests: ExtendedStoredDataRequest[] = [];
const expectedHeaders = ['COMPANY', 'REPORTING PERIOD', 'FRAMEWORK', 'REQUESTED', 'LAST UPDATED', 'STATUS'];
const dummyRequestId = 'dummyRequestId';

before(function () {
  /**
   * Builds an extended stored data request object and assures type-safety.
   * @param dataType to include in the data request
   * @param reportingPeriod to include in the data request
   * @param companyName to include in the data request
   * @param companyId to include in the data request
   * @param requestStatus to set in the data request
   * @param accessStatus to set in the data request
   * @param requestPriority to set in the data request
   * @returns an extended sorted data request object
   */
  function buildExtendedStoredDataRequest(
    dataType: DataTypeEnum,
    reportingPeriod: string,
    companyName: string,
    companyId: string,
    requestStatus: RequestStatus,
    accessStatus: AccessStatus,
    requestPriority: RequestPriority
  ): ExtendedStoredDataRequest {
    return {
      dataRequestId: dummyRequestId,
      userId: 'some-user-id',
      creationTimestamp: 1709204495770,
      dataType: dataType,
      reportingPeriod: reportingPeriod,
      datalandCompanyId: companyId,
      companyName: companyName,
      lastModifiedDate: 1709204495770,
      requestStatus: requestStatus,
      accessStatus: accessStatus,
      requestPriority: requestPriority,
    };
  }

  mockDataRequests.push(
    buildExtendedStoredDataRequest(
      DataTypeEnum.P2p,
      '2020',
      'companyAnswered',
      'compA',
      RequestStatus.Answered,
      AccessStatus.Pending,
      RequestPriority.Low
    )
  );

  mockDataRequests.push(
    buildExtendedStoredDataRequest(
      DataTypeEnum.Sfdr,
      '2022',
      'companyNotAnsweredSfdr',
      'someId',
      RequestStatus.Open,
      AccessStatus.Pending,
      RequestPriority.Low
    )
  );

  mockDataRequests.push(
    buildExtendedStoredDataRequest(
      DataTypeEnum.EutaxonomyFinancials,
      '3021',
      'z-company-that-will-always-be-sorted-to-bottom',
      'someId',
      RequestStatus.Resolved,
      AccessStatus.Pending,
      RequestPriority.Low
    )
  );

  mockDataRequests.push(
    buildExtendedStoredDataRequest(
      DataTypeEnum.EutaxonomyNonFinancials,
      '2021',
      'companyNotAnsweredEU',
      'someId',
      RequestStatus.Open,
      AccessStatus.Pending,
      RequestPriority.Low
    )
  );

  mockDataRequests.push(
    buildExtendedStoredDataRequest(
      DataTypeEnum.EsgDatenkatalog,
      '1021',
      'a-company-that-will-always-be-sorted-to-top',
      'someId',
      RequestStatus.Answered,
      AccessStatus.Pending,
      RequestPriority.Low
    )
  );
});
describe('Component tests for the data requests search page', function (): void {
  it('Check sorting', function (): void {
    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');
    cy.mountWithPlugins(MyDataRequestsOverview, {
      keycloak: minimalKeycloakMock({}),
    });
    const sortingColumHeader = ['COMPANY', 'REPORTING PERIOD', 'REQUESTED', 'REQUEST STATUS', 'ACCESS STATUS'];
    sortingColumHeader.forEach((value) => {
      cy.get(`table th:contains(${value})`).should('exist').click();
      cy.get('[data-test="requested-Datasets-table"]')
        .find('tr')
        .find('td')
        .contains('a-company-that-will-always-be-sorted-to-top')
        .parent()
        .invoke('index')
        .should('eq', 0);
      cy.get(`table th:contains(${value})`).should('exist').click();
      cy.get('[data-test="requested-Datasets-table"]')
        .find('tr')
        .find('td')
        .contains('z-company-that-will-always-be-sorted-to-bottom')
        .parent()
        .invoke('index')
        .should('eq', 0);
    });
  });

  it('Check page when there are no requested datasets', function (): void {
    cy.intercept('**community/requests/user', {
      body: [],
      status: 200,
    }).as('UserRequests');
    cy.spy(router, 'push').as('routerPush');
    cy.mountWithPlugins(MyDataRequestsOverview, {
      keycloak: minimalKeycloakMock({}),
      router: router,
    }).then(() => {
      cy.get('[data-test="requested-Datasets-table"]').should('not.exist');
      cy.get('[data-test="bulkDataRequestButton"]').should('exist').should('be.visible').click();
      cy.get('@routerPush').should('have.been.calledWith', '/bulkdatarequest');
    });
  });

  it('Check static layout of the search page', function () {
    const placeholder = 'Search by company name';
    const inputValue = 'A company name';

    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');
    cy.mountWithPlugins(MyDataRequestsOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.get('[data-test="requested-Datasets-table"]').should('exist');
    expectedHeaders.forEach((value) => {
      cy.get(`table th:contains(${value})`).should('exist');
    });
    cy.get('[data-test="requested-Datasets-searchbar"]')
      .should('exist')
      .should('not.be.disabled')
      .type(inputValue)
      .should('have.value', inputValue)
      .invoke('attr', 'placeholder')
      .should('contain', placeholder);
    cy.get('[data-test="requested-Datasets-frameworks"]').should('exist');
  });

  it('Check the content of the data table', function (): void {
    const expectedCompanys = [
      'companyAnswered',
      'companyNotAnsweredSfdr',
      'companyNotAnsweredEU',
      'z-company-that-will-always-be-sorted-to-bottom',
      'a-company-that-will-always-be-sorted-to-top',
    ];
    const expectedReportingPeriods = ['2020', '2021', '2022'];

    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');

    cy.mountWithPlugins(MyDataRequestsOverview, {
      keycloak: minimalKeycloakMock({}),
    });

    expectedCompanys.forEach((value) => {
      cy.get('[data-test="requested-Datasets-table"]').find('tr').find('td').contains(value).should('exist');
    });
    cy.get('[data-test="requested-Datasets-table"]').find('tr').find('td').contains('DummyName').should('not.exist');
    expectedReportingPeriods.forEach((value) => {
      cy.get('[data-test="requested-Datasets-table"]').find('tr').find('td').contains(value).should('exist');
    });
    cy.get('[data-test="requested-Datasets-table"]').find('tr').find('td').contains('2019').should('not.exist');
  });

  it('Check existence and functionality of searchbar and resolve button', function (): void {
    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');
    cy.spy(router, 'push').as('routerPush');

    cy.mountWithPlugins(MyDataRequestsOverview, {
      keycloak: minimalKeycloakMock({}),
      router: router,
    }).then(() => {
      cy.get('[data-test="requested-Datasets-searchbar"]')
        .should('exist')
        .should('not.be.disabled')
        .clear()
        .type('companyNotAnswered');
      cy.get('[data-test="requested-Datasets-Resolve"]').should('not.exist');
      cy.get('[data-test="requested-Datasets-searchbar"]')
        .should('exist')
        .should('not.be.disabled')
        .clear()
        .type('companyAnswered');
      cy.get('[data-test="requested-Datasets-Resolve"]').should('exist').should('be.visible').click();
      cy.get('@routerPush').should('have.been.calledWith', '/companies/compA/frameworks/p2p');
    });
  });

  it('Check filter functionality and reset button', function (): void {
    const expectedFrameworks = [
      'WWF',
      'SFDR',
      'EU Taxonomy',
      'Pathways to Paris',
      'for financial companies',
      'for non-financial companies',
      'ESG Datenkatalog',
      'für Corporate Schuldscheindarlehen',
    ];

    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');

    cy.mountWithPlugins(MyDataRequestsOverview, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      void mounted.wrapper.setData({
        selectedFrameworks: [],
      });
      expectedFrameworks.forEach((value) => {
        cy.get(`table tbody:contains(${value})`).should('not.exist');
      });
      cy.get('[data-test=reset-filter]').should('exist').click();
      expectedFrameworks.forEach((value) => {
        cy.get(`table tbody:contains(${value})`).should('exist');
      });
      cy.get(`table tbody:contains("SME")`).should('not.exist');
    });
  });
  it('Check the functionality of rowClick event', function (): void {
    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');
    cy.spy(router, 'push').as('routerPush');
    cy.mountWithPlugins(MyDataRequestsOverview, {
      keycloak: minimalKeycloakMock({}),
      router: router,
    }).then(() => {
      cy.get('[data-test="requested-Datasets-table"]').within(() => {
        cy.get('tr:last').click();
      });
      cy.get('@routerPush').should('have.been.calledWith', `/requests/${dummyRequestId}`);
    });
  });
});
