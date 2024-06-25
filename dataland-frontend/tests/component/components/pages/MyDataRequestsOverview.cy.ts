import { type ExtendedStoredDataRequest, RequestStatus } from '@clients/communitymanager';
import RequestedDatasetsPage from '@/components/pages/MyDataRequestsOverview.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';

const mockDataRequests: ExtendedStoredDataRequest[] = [];
const expectedHeaders = ['COMPANY', 'REPORTING PERIOD', 'FRAMEWORK', 'REQUESTED', 'LAST UPDATED', 'STATUS'];
const dummyRequestId = 'dummyRequestId';

before(function () {
  mockDataRequests.push({
    dataRequestId: dummyRequestId,
    datalandCompanyId: 'compA',
    companyName: 'companyAnswered',
    dataType: DataTypeEnum.P2p,
    reportingPeriod: '2020',
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Answered,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: dummyRequestId,
    companyName: 'companyNotAnsweredSfdr',
    dataType: DataTypeEnum.Sfdr,
    reportingPeriod: '2022',
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Open,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: dummyRequestId,
    companyName: 'z-company-that-will-always-be-sorted-to-bottom',
    dataType: DataTypeEnum.EutaxonomyFinancials,
    reportingPeriod: '3021',
    creationTimestamp: 1809204495770,
    lastModifiedDate: 1609204495770,
    requestStatus: RequestStatus.Resolved,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: dummyRequestId,
    companyName: 'companyNotAnsweredEU',
    dataType: DataTypeEnum.EutaxonomyNonFinancials,
    reportingPeriod: '2021',
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Open,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: dummyRequestId,
    companyName: 'a-company-that-will-always-be-sorted-to-top',
    dataType: DataTypeEnum.EsgQuestionnaire,
    reportingPeriod: '1021',
    creationTimestamp: 1609204495770,
    lastModifiedDate: 1809204495770,
    requestStatus: RequestStatus.Answered,
  } as ExtendedStoredDataRequest);
});
describe('Component tests for the data requests search page', function (): void {
  it('Check sorting', function (): void {
    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');
    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    });
    const sortingColumHeader = ['COMPANY', 'REPORTING PERIOD', 'REQUESTED', 'STATUS'];
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
    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      cy.get('[data-test="requested-Datasets-table"]').should('not.exist');
      cy.get('[data-test="bulkDataRequestButton"]').should('exist').should('be.visible').click();
      cy.wrap(mounted.component).its('$route.path').should('eq', '/bulkdatarequest');
    });
  });

  it('Check static layout of the search page', function () {
    const placeholder = 'Search by company name';
    const inputValue = 'A company name';

    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');
    cy.mountWithPlugins(RequestedDatasetsPage, {
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

    cy.mountWithPlugins(RequestedDatasetsPage, {
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

    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
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
      cy.wrap(mounted.component).its('$route.path').should('eq', '/companies/compA/frameworks/p2p');
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
      'ESG Questionnaire',
      'für Corporate Schuldscheindarlehen',
    ];

    cy.intercept('**community/requests/user', {
      body: mockDataRequests,
      status: 200,
    }).as('UserRequests');

    cy.mountWithPlugins(RequestedDatasetsPage, {
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
    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      cy.get('[data-test="requested-Datasets-table"]').within(() => {
        cy.get('tr:last').click();
      });
      cy.wrap(mounted.component).its('$route.path').should('eq', `/requests/${dummyRequestId}`);
    });
  });
});
