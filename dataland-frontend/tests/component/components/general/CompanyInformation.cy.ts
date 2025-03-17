// @ts-nocheck
import CompanyInformationComponent from '@/components/pages/CompanyInformation.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type CompanyInformation, type DataMetaInformation, DataTypeEnum, type VsmeData } from '@clients/backend';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { type StoredDataRequest } from '@clients/communitymanager';
let vsmeFixtureForTest: FixtureData<VsmeData>;
import router from '@/router';

describe('Component tests for the company info sheet', function (): void {
  let companyInformationForTest: CompanyInformation;
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyParentCompanyLei = 'dummyParentLei';
  const dummyParentCompanyId = 'dummyParentCompanyId';
  const dummyParentCompanyName = 'dummyParent Company';
  const dummyCompanyLei = 'dummyCompanyLei';
  let mockedStoredDataRequests: StoredDataRequest[];
  before(function () {
    cy.fixture('CompanyInformationWithVsmePreparedFixtures').then(function (jsonContent) {
      const preparedFixturesSme = jsonContent as Array<FixtureData<VsmeData>>;
      vsmeFixtureForTest = getPreparedFixture('Vsme-dataset-with-no-null-fields', preparedFixturesSme);

      companyInformationForTest = vsmeFixtureForTest.companyInformation;
      companyInformationForTest.parentCompanyLei = dummyParentCompanyLei;
      companyInformationForTest.identifiers = {
        Lei: [dummyCompanyLei],
      };
    });
    cy.fixture('DataRequestsMock').then(function (jsonContent) {
      mockedStoredDataRequests = jsonContent as Array<StoredDataRequest>;
    });
  });
  /**
   *  Mocks the desired requests
   */
  function mockRequestsOnMounted(): void {
    cy.intercept(`**/api/companies/${dummyCompanyId}/info`, {
      body: companyInformationForTest,
      times: 1,
    }).as('fetchCompanyInfo');
    cy.intercept(`**/community/requests/user`, {
      body: mockedStoredDataRequests,
    }).as('fetchUserRequests');

    cy.intercept(`**/api/companies/names?searchString=${dummyParentCompanyLei}**`, {
      body: [
        {
          companyId: dummyParentCompanyId,
          companyName: dummyParentCompanyName,
        },
      ],
    }).as('getParentCompanyId');
  }

  it('Check visibility of company information', function () {
    mockRequestsOnMounted();
    cy.spy(router, 'push').as('routerPush');
    cy.mountWithPlugins(CompanyInformationComponent, {
      keycloak: minimalKeycloakMock({}),
      router: router,
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        companyId: dummyCompanyId,
      });
      cy.wait('@getParentCompanyId');
      cy.get('[data-test="lei-visible"]').should('have.text', companyInformationForTest.identifiers['Lei'][0]);
      cy.get('[data-test="headquarter-visible"]').should('have.text', companyInformationForTest.headquarters);
      cy.get('[data-test="sector-visible"]').should('have.text', companyInformationForTest.sector);
      cy.get('[data-test="parent-visible"]').should('have.text', dummyParentCompanyName).click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyParentCompanyId}`);
    });
  });

  it('Check visibility of review request buttons', function () {
    mockRequestsOnMounted();
    cy.mountWithPlugins(CompanyInformationComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        companyId: dummyCompanyId,
        framework: DataTypeEnum.EutaxonomyNonFinancials,
        mapOfReportingPeriodToActiveDataset: new Map<string, DataMetaInformation>([
          ['1996', {} as DataMetaInformation],
          ['1997', {} as DataMetaInformation],
        ]),
      });
    });
    cy.get('[data-test="reOpenRequestButton"]').should('exist');
    cy.get('[data-test="resolveRequestButton"]').should('exist');
  });
  it('Check non-visibility of review request buttons', function () {
    mockRequestsOnMounted();
    cy.mountWithPlugins(CompanyInformationComponent, {
      keycloak: minimalKeycloakMock({}),

      // @ts-ignore
      props: {
        companyId: dummyCompanyId,
      },
    });
    cy.get('[data-test="reOpenRequestButton"]').should('not.exist');
    cy.get('[data-test="resolveRequestButton"]').should('not.exist');
  });
});
