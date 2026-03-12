import CompanyInformationComponent from '@/components/pages/CompanyInformation.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type CompanyInformation, type VsmeData } from '@clients/backend';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { type StoredDataRequest } from '@clients/communitymanager';
import router from '@/router';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the company info sheet', function (): void {
  const dummyCompanyId = '550e8400-e29b-11d4-a716-446655440000';
  const dummyParentCompanyLei = 'dummyParentLei';
  const dummyParentCompanyId = 'dummyParentCompanyId';
  const dummyParentCompanyName = 'dummyParent Company';
  const dummyCompanyLei = 'dummyCompanyLei';

  let companyInformationForTest: CompanyInformation;
  let mockedStoredDataRequests: StoredDataRequest[];
  let vsmeFixtureForTest: FixtureData<VsmeData>;

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
    });
    cy.intercept(`**/community/requests/user`, {
      body: mockedStoredDataRequests,
    });

    cy.intercept(`**/api/companies/names?searchString=${dummyParentCompanyLei}**`, {
      body: [
        {
          companyId: dummyParentCompanyId,
          companyName: dummyParentCompanyName,
        },
      ],
    });
  }

  /**
   * Mounts the CompanyInformation component with a mocked authentication state.
   * @param authenticated - Whether the user is authenticated.
   */
  function mountComponentWithAuth(authenticated: boolean): void {
    const mountingFunction = getMountingFunction({
      keycloak: minimalKeycloakMock({ authenticated }),
      router,
    });

    mountingFunction(CompanyInformationComponent, {
      props: {
        companyId: dummyCompanyId,
      },
    });
  }

  it('Check visibility of company information', function () {
    mockRequestsOnMounted();
    cy.spy(router, 'push').as('routerPush');
    //@ts-ignore
    cy.mountWithPlugins(CompanyInformationComponent, {
      keycloak: minimalKeycloakMock({}),
      router,
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        companyId: dummyCompanyId,
      });
      cy.get('[data-test="lei-visible"]').should('have.text', companyInformationForTest.identifiers['Lei'][0]);
      cy.get('[data-test="headquarter-visible"]').should('have.text', companyInformationForTest.headquarters);
      cy.get('[data-test="sector-visible"]').should('have.text', companyInformationForTest.sector);
      cy.get('[data-test="parent-visible"]').should('have.text', dummyParentCompanyName).click();
      cy.get('@routerPush').should('have.been.calledWith', `/companies/${dummyParentCompanyId}`);
    });
  });

  it('should not show "Add to portfolio" button when user is not authenticated', function () {
    mockRequestsOnMounted();
    mountComponentWithAuth(false);
    cy.get('[data-test="addCompanyToPortfoliosButton"]').should('not.exist');
  });

  it('should show "Add to portfolio" button when user is authenticated', function () {
    mockRequestsOnMounted();
    mountComponentWithAuth(true);
    cy.get('[data-test="addCompanyToPortfoliosButton"]').should('exist');
  });
});
