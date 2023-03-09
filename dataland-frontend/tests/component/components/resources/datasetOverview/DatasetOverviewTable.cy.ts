import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import { DatasetTableInfo, DatasetStatus } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { DataTypeEnum, StoredCompany, QAStatus } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import { minimalKeycloakMock } from "../../../testUtils/keycloak";

describe("Component test for DatasetOverviewTable", () => {
  /**
   * Mounts the DatasetOverviewTable with a single approved dataset
   */
  function prepareSimpleDatasetOverviewTable(): void {
    const keycloakMock = minimalKeycloakMock({
      userId: "Mock-User-Id",
      roles: ["ROLE_USER", "ROLE_UPLOADER"],
    });
    const mockDataEntry: DatasetTableInfo = {
      dataId: "Mock-Data-Id",
      companyId: "Mock-Company-Id",
      dataType: DataTypeEnum.Lksg,
      companyName: "Mock-Company-Name",
      dataReportingPeriod: "2023",
      status: DatasetStatus.QAApproved,
      uploadTimeInMs: 1672527600000, // 1.1.2023 00:00:00:0000
    };
    const mockData = [mockDataEntry];

    cy.mountWithPlugins<typeof DatasetOverviewTable>(DatasetOverviewTable, {
      keycloak: keycloakMock,
    }).then(({ component, wrapper }) => {
      void wrapper.setProps({
        datasetTableInfos: mockData,
      });
    });
  }

  /**
   * Returns a mocked StoredCompany entity
   *
   * @param companyName the name of the company to mock
   * @returns a mocked StoredCompany entity with a corresponding dataset
   */
  function getMockCompaniesResponse(companyName: string): StoredCompany {
    return {
      companyId: "mock-company-id",
      companyInformation: {
        companyName: companyName,
        countryCode: "DE",
        headquarters: "DE",
        sector: "MOCK-SECTOR",
        identifiers: [],
      },
      dataRegisteredByDataland: [
        {
          dataId: "Mock-Data-Id",
          dataType: DataTypeEnum.Lksg,
          companyId: "mock-company-id",
          qaStatus: QAStatus.Accepted,
          currentlyActive: true,
          reportingPeriod: "2023",
          uploaderUserId: "Mock-User-Id",
          uploadTime: 1672527600, // 1.1.2023 00:00:00:0000
        },
      ],
    };
  }

  it("Check if the table rows look as expected", () => {
    prepareSimpleDatasetOverviewTable();
    const expectedRowContents = [
      `COMPANYMock-Company-Name`,
      `DATA FRAMEWORK${humanizeString(DataTypeEnum.Lksg)}`,
      `REPORTING PERIOD2023`,
      "STATUSAPPROVED",
    ];
    cy.get("tbody td").should((elements) => {
      expect(elements.length).to.equal(6);
    });
    cy.get("tbody td").each((element, index) => {
      if (index < expectedRowContents.length) {
        expect(element.text()).to.equal(expectedRowContents[index]);
      } else if (index == 4) {
        expect(Date.parse(element.text().substring(15)).toString()).not.to.equal(NaN.toString());
      } else if (index == 5) {
        expect(element.text()).to.contain("VIEW");
      }
    });
    cy.get("tbody td a").should("have.attr", "href", `/companies/Mock-Company-Id/frameworks/lksg/Mock-Data-Id`);
  });

  it("Validates the layout of the table header", () => {
    prepareSimpleDatasetOverviewTable();
    const expectedHeaders = ["COMPANY", "DATA FRAMEWORK", "SUBMISSION DATE", "REPORTING PERIOD", "STATUS"];
    expectedHeaders.forEach((value) => {
      cy.get(`table th:contains(${value})`).should("exist");
    });
    const unexpectedHeaders = ["YEAR"];
    unexpectedHeaders.forEach((value) => {
      cy.get(`table th:contains(${value})`).should("not.exist");
    });
    cy.get("th").each((element) => {
      if (!expectedHeaders.includes(element.text())) {
        expect(element.html()).to.contain("<input");
      }
    });
  });

  it("Check if search filter works as expected", () => {
    const mockCompanyToBeSearchedFor = getMockCompaniesResponse("CoolCompany");
    cy.intercept("**/api/companies?**", []);
    cy.intercept("**/api/companies?**searchString=CoolCompany**", [mockCompanyToBeSearchedFor]);
    prepareSimpleDatasetOverviewTable();
    cy.get("td").contains("Mock-Company-Name").should("exist");
    cy.get("input").type("CoolCompany");
    cy.get("td").contains("CoolCompany").should("exist");
    cy.get("td").contains("Mock-Company-Name").should("not.exist");
  });
});
