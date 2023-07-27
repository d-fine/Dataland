import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  DataMetaInformation,
  SmeData, CompanyAssociatedDataSmeData,
} from "@clients/backend";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import SmePanel from "@/components/resources/frameworkDataSearch/sme/SmePanel.vue";


describe("Component tests for SmePanel", () => {
  let preparedFixtures: Array<FixtureData<SmeData>>;

  before(function () {
    cy.fixture("CompanyInformationWithSmePreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<SmeData>>;
    });
  });

  it("Check Sme view page for company with one Sme data set", () => {
    const preparedFixture = getPreparedFixture("SME-year-2023", preparedFixtures);
    const smeData = preparedFixture.t;

    cy.intercept("/api/data/sme/mock-data-id", {
      companyId: "mock-company-id",
      reportingPeriod: preparedFixture.reportingPeriod,
      data: smeData,
    } as CompanyAssociatedDataSmeData);
    cy.mountWithPlugins(SmePanel, {
      keycloak: minimalKeycloakMock({}),
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
    cy.get("table.p-datatable-table")
      .find(`span:contains(${smeData.general!.basicInformation!.numberOfEmployees!})`)
      .should("exist");

    spanExistsNot("< 1%");
    spanIsNotVisible("Investments");
    toggleRow('POWER')
    spanExistsNot("< 1%");
    toggleRow("Investments")
    spanExists("< 1%");
    spanExistsNot("< 25%");
    toggleRow("Consumption")
    spanExists("< 25%");
  });

  // TODO make the selectors in the functions more specific for categories, subcategories and fields

  function toggleRow(rowLabel: string) {
    cy.get(`span:contains('${rowLabel}')`).click();
  }

  function spanExists(partialContent: string) {
    cy.get(`span:contains('${partialContent}')`).should("exist");
  }

  function spanExistsNot(partialContent: string) {
    cy.get(`span:contains('${partialContent}')`).should("not.exist");
  }

  function spanIsNotVisible(partialContent: string) {
    cy.get(`span:contains('${partialContent}')`).should("not.visible");
  }

  // TODO add test for address
});
