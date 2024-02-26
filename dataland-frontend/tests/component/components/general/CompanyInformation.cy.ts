import CompanyInformationComponent from "@/components/pages/CompanyInformation.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type CompanyInformation, type SmeData, type DataMetaInformation, DataTypeEnum } from "@clients/backend";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
describe("Component tests for the company info sheet", function (): void {
  let companyInformationForTest: CompanyInformation;
  const dummyCompanyId = "550e8400-e29b-11d4-a716-446655440000";
  before(function () {
    cy.fixture("CompanyInformationWithSmeData").then(function (jsonContent) {
      const smeFixtures = jsonContent as Array<FixtureData<SmeData>>;
      companyInformationForTest = smeFixtures[0].companyInformation;
    });
  });
  /**
   *  Mocks the desired requests
   */
  function mockRequestsOnMounted(): void {
    cy.intercept(`**/api/companies/${dummyCompanyId}/info`, {
      body: companyInformationForTest,
      times: 1,
    }).as("fetchCompanyInfo");
    cy.intercept(`**/community/requests/user`, {
      body: [
        {
          dataType: DataTypeEnum.EutaxonomyNonFinancials,
          dataRequestCompanyIdentifierValue: dummyCompanyId,
          reportingPeriod: "1996",
          requestStatus: RequestStatus.Answered,
        } as StoredDataRequest,
      ],
    }).as("fetchUserRequests");
  }
  it("Check visibility of review request buttons", function () {
    mockRequestsOnMounted();
    cy.mountWithPlugins(CompanyInformationComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        companyId: dummyCompanyId,
        framework: DataTypeEnum.EutaxonomyNonFinancials,
        mapOfReportingPeriodToActiveDataset: new Map<string, DataMetaInformation>([
          ["1996", {} as DataMetaInformation],
          ["1997", {} as DataMetaInformation],
        ]),
      });
    });
    cy.get('[data-test="reOpenRequestButton"]').should("exist");
    cy.get('[data-test="closeRequestButton"]').should("exist");
  });
  it("Check non-visibility of review request buttons", function () {
    mockRequestsOnMounted();
    cy.mountWithPlugins(CompanyInformationComponent, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyId: dummyCompanyId,
      },
    });
    cy.get('[data-test="reOpenRequestButton"]').should("not.exist");
    cy.get('[data-test="closeRequestButton"]').should("not.exist");
  });
});
