import CompanyInformationComponent from "@/components/pages/CompanyInformation.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { DataTypeEnum } from "@clients/backend";
import { type DataMetaInformation } from "@clients/backend";
import { type CompanyInformation } from "@clients/backend";
import { type SmeData } from "@clients/backend";
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
  function mockRequestsOnMounted(hasCompanyDataOwner: boolean = false): void {
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

    cy.intercept("**/api/companies/*/data-owners/mock-data-owner-id", {
      status: 200,
    }).as("fetchUserIsDataOwnerTrue");
    if (hasCompanyDataOwner) {
      cy.intercept("**/api/companies/*/data-owners", {
        body: ["company-owner-id"],
      }).as("fetchHasCompanyDataOwnersFalse");
    }
  }
  it("Check visibility of review request buttons", function () {
    mockRequestsOnMounted(false);
    cy.mountWithPlugins(CompanyInformationComponent, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        companyId: dummyCompanyId,
        showSingleDataRequestButton: true,
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
});
