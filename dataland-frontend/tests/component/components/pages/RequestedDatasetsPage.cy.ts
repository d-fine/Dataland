import { type ExtendedStoredDataRequest, RequestStatus } from "@clients/communitymanager";
import RequestedDatasetsPage from "@/components/pages/RequestedDatasetsPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { DataTypeEnum } from "@clients/backend";

const mockDataRequests: ExtendedStoredDataRequest[] = [];
before(function () {
  mockDataRequests.push({
    dataRequestId: "dummyId",
    datalandCompanyId: "compA",
    companyName: "companyAnswered",
    dataType: DataTypeEnum.P2p,
    reportingPeriod: "2020",
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Answered,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: "dummyId",
    datalandCompanyId: "compO",
    companyName: "companyNotAnswered",
    dataType: DataTypeEnum.Sme,
    reportingPeriod: "2022",
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Open,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: "dummyId",
    datalandCompanyId: "compC",
    companyName: "companyNotAnswered",
    dataType: DataTypeEnum.EsgQuestionnaire,
    reportingPeriod: "2021",
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Closed,
  } as ExtendedStoredDataRequest);
});
describe("Component tests for the data requests search page", function (): void {
  it("Check static layout of the search page", function () {
    cy.intercept("**community/requests/user", {
      body: [],
      status: 200,
    }).as("UserRequests");
    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    });
    const placeholder = "Search by company name";
    const inputValue = "A company name";
    cy.get('[data-test="requested-Datasets-table"]').should("exist");
    cy.get('[data-test="requested-Datasets-searchbar"]')
      .should("exist")
      .should("not.be.disabled")
      .type(inputValue)
      .should("have.value", inputValue)
      .invoke("attr", "placeholder")
      .should("contain", placeholder);
    cy.get('[data-test="requested-Datasets-frameworks"]').should("exist");
  });

  it("Check existence and functionality of searchbar and resolve button", function (): void {
    cy.intercept("**community/requests/user", {
      body: mockDataRequests,
      status: 200,
    }).as("UserRequests");
    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      cy.get('[data-test="requested-Datasets-searchbar"]')
        .should("exist")
        .should("not.be.disabled")
        .clear()
        .type("companyNotAnswered");
      cy.get('[data-test="requested-Datasets-Resolve"]').should("not.exist");
      cy.get('[data-test="requested-Datasets-searchbar"]')
        .should("exist")
        .should("not.be.disabled")
        .clear()
        .type("companyAnswered");
      cy.get('[data-test="requested-Datasets-Resolve"]').should("exist").should("be.visible").click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/companies/compA/frameworks/p2p");
    });
  });
});
