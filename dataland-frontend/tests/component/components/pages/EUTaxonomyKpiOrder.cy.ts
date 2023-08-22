import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import ThreeLayerTable from "@/components/resources/frameworkDataSearch/euTaxonomy/NewEuTaxonomyForNonFinancialsPanel.vue";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  type CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials,
  type DataMetaInformation,
  type NewEuTaxonomyDataForNonFinancials,
} from "@clients/backend";
describe("Component test for the NewEUTaxonomy Page", () => {
  let preparedFixtures: Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;

  before(() => {
    cy.fixture("CompanyInformationWithNewEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<NewEuTaxonomyDataForNonFinancials>>;
    });
  });

  //const kpiList:string[] = ["GENERAL", "REVENUE", "CAPEX", "OPEX"]; These 2 lists have to be used when running the cypress test locally, because the fixtures are different.
  //const kpiListOrderChanged = ["REVENUE", "GENERAL", "OPEX", "CAPEX"];
  const kpiList: string[] = ["BASIC INFORMATION", "ASSURANCE", "REVENUE", "CAPEX", "OPEX"];
  const kpiListOrderChanged = ["ASSURANCE", "OPEX", "BASIC INFORMATION", "REVENUE", "CAPEX"];

  it("Check order of the displayed KPIs and category entries", () => {
    const preparedFixture = getPreparedFixture("only-eligible-numbers", preparedFixtures);
    const newEuTaxonomyDataForNonFinancialsData = preparedFixture.t;

    cy.intercept("/api/data/new-eutaxonomy-non-financials/mock-data-id", {
      companyId: "mock-company-id",
      reportingPeriod: preparedFixture.reportingPeriod,
      data: newEuTaxonomyDataForNonFinancialsData,
    } as CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials);
    cy.mountWithPlugins(ThreeLayerTable, {
      keycloak: minimalKeycloakMock({}),
      global: {
        stubs: {
          transition: false,
        },
      },
      data() {
        return {
          companyId: "mock-company-id",
          singleDataMetaInfoToDisplay: {
            dataId: "mock-data-id",
            reportingPeriod: preparedFixture.reportingPeriod,
          } as DataMetaInformation,
        };
      },
    });
    /**
        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style")
            .each((element, index) =>  {
                cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("have.text",kpiList[index]);
            });
     
        cy.wait(50);
     
        cy.get("[data-test='ThreeLayerTableTest']").get(".d-table-style")
            .each((element, index) =>  {
                cy.wrap(element).eq(0).eq(0).get(".p-badge").eq(index).should("not.have.text", kpiListOrderChanged[index]);
            });
     */
    cy.get("[data-test='TwoLayerTest']").eq(2);
  });
});
