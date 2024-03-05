import { type ExtendedStoredDataRequest, RequestStatus } from "@clients/communitymanager";
import RequestedDatasetsPage from "../../../../src/components/pages/MyDataRequestsOverview.vue";
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
    companyName: "companyNotAnswered1",
    dataType: DataTypeEnum.Sme,
    reportingPeriod: "2022",
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Open,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: "dummyId",
    datalandCompanyId: "compC",
    companyName: "companyNotAnswered2",
    dataType: DataTypeEnum.EutaxonomyFinancials,
    reportingPeriod: "2021",
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Closed,
  } as ExtendedStoredDataRequest);
  mockDataRequests.push({
    dataRequestId: "dummyId",
    datalandCompanyId: "compC",
    companyName: "companyNotAnswered3",
    dataType: DataTypeEnum.EutaxonomyNonFinancials,
    reportingPeriod: "2021",
    creationTimestamp: 1709204495770,
    lastModifiedDate: 1709204495770,
    requestStatus: RequestStatus.Closed,
  } as ExtendedStoredDataRequest);
});
describe("Component tests for the data requests search page", function (): void {
  it("Check page when there are no requested datasets", function (): void {
    cy.intercept("**community/requests/user", {
      body: [],
      status: 200,
    }).as("UserRequests");
    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      cy.get('[data-test="requested-Datasets-table"]').should("not.exist");
      cy.get('[data-test="bulkDataRequestButton"]').should("exist").should("be.visible").click();
      cy.wrap(mounted.component).its("$route.path").should("eq", "/bulkdatarequest");
    });
  });

  it("Check static layout of the search page", function () {
    const placeholder = "Search by company name";
    const inputValue = "A company name";
    const expectedHeaders = ["COMPANY", "REPORTING PERIOD", "FRAMEWORK", "REQUESTED", "LAST UPDATED", "STATUS"];

    cy.intercept("**community/requests/user", {
      body: mockDataRequests,
      status: 200,
    }).as("UserRequests");
    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    });

    cy.get('[data-test="requested-Datasets-table"]').should("exist");
    expectedHeaders.forEach((value) => {
      cy.get(`table th:contains(${value})`).should("exist");
    });
    cy.get('[data-test="requested-Datasets-searchbar"]')
      .should("exist")
      .should("not.be.disabled")
      .type(inputValue)
      .should("have.value", inputValue)
      .invoke("attr", "placeholder")
      .should("contain", placeholder);
    cy.get('[data-test="requested-Datasets-frameworks"]').should("exist");
  });

  it("Check the content of the data table", function (): void {
    const expectedCompanys = ["companyAnswered", "companyNotAnswered1", "companyNotAnswered2"];
    const expectedReportingPeriods = ["2020", "2021", "2022"];

    cy.intercept("**community/requests/user", {
      body: mockDataRequests,
      status: 200,
    }).as("UserRequests");

    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    });

    expectedCompanys.forEach((value) => {
      cy.get('[data-test="requested-Datasets-table"]').find("tr").find("td").contains(value).should("exist");
    });
    cy.get('[data-test="requested-Datasets-table"]').find("tr").find("td").contains("DummyName").should("not.exist");
    expectedReportingPeriods.forEach((value) => {
      cy.get('[data-test="requested-Datasets-table"]').find("tr").find("td").contains(value).should("exist");
    });
    cy.get('[data-test="requested-Datasets-table"]').find("tr").find("td").contains("2019").should("not.exist");
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

  it("Check filter functionality and reset button", function (): void {
    const expectedFrameworks = [
      "WWF",
      "SME",
      "EU Taxonomy",
      "Pathways to Paris",
      "for financial companies",
      "for non-financial companies",
    ];

    cy.intercept("**community/requests/user", {
      body: mockDataRequests,
      status: 200,
    }).as("UserRequests");

    cy.mountWithPlugins(RequestedDatasetsPage, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      void mounted.wrapper.setData({
        selectedFrameworks: [],
      });
      expectedFrameworks.forEach((value) => {
        cy.get(`table tbody:contains(${value})`).should("not.exist");
      });
      cy.get("[data-test=reset-filter]").should("exist").click();
      expectedFrameworks.forEach((value) => {
        cy.get(`table tbody:contains(${value})`).should("exist");
      });
      cy.get(`table tbody:contains("SFDR")`).should("not.exist");
    });
  });
});
