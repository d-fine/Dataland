import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import { DatasetTableInfo, DatasetStatus } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { DataTypeEnum } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";

describe("Component test for DatasetOverviewTable", () => {
  const nameOfCompanyAlpha = "Imaginary-Corporate";
  const dataTypeOfDatasetForAlpha = DataTypeEnum.Lksg;
  const datasetTableInfoMockForAlpha = createDatasetTableInfoMock(nameOfCompanyAlpha, dataTypeOfDatasetForAlpha);

  const nameOfCompanyBeta = "Dream-Insurance";
  const dataTypeOfDatasetForBeta = DataTypeEnum.EutaxonomyFinancials;
  const datasetTableInfoMockForBeta = createDatasetTableInfoMock(nameOfCompanyBeta, dataTypeOfDatasetForBeta);

  /**
   * Creates a DatasetTableInfo-object based on the inputs
   *
   * @param companyName The company name that the DatasetTableInfo-object shall have
   * @param dataType The datatype that the DatasetTableInfo-object shall have
   * @returns the created DatasetTableInfo-object
   */
  function createDatasetTableInfoMock(companyName: string, dataType: DataTypeEnum): DatasetTableInfo {
    return {
      dataId: companyName + "-Mock-Data-Id",
      companyId: companyName + "-Mock-Company-Id",
      dataType: dataType,
      companyName: companyName,
      dataReportingPeriod: "2023",
      status: DatasetStatus.QAApproved,
      uploadTimeInMs: 1672527600000, // 1.1.2023 00:00:00:0000
    };
  }

  /**
   * Mounts the DatasetOverviewTable with all dataset table entries passed to it
   *
   * @param mockDatasetTableInfos The DatasetTableInfo-objects that shall be used to write some entries into the table
   */
  function prepareSimpleDatasetOverviewTable(mockDatasetTableInfos: DatasetTableInfo[]): void {
    const keycloakMock = minimalKeycloakMock({
      userId: "Mock-User-Id",
      roles: ["ROLE_USER", "ROLE_UPLOADER"],
    });
    cy.mountWithPlugins<typeof DatasetOverviewTable>(DatasetOverviewTable, {
      keycloak: keycloakMock,
    }).then((mocked) => {
      void mocked.wrapper.setProps({
        datasetTableInfos: mockDatasetTableInfos,
      });
    });
  }

  it("Check if the table rows look as expected", () => {
    prepareSimpleDatasetOverviewTable([datasetTableInfoMockForAlpha]);
    const expectedRowContents = [nameOfCompanyAlpha, humanizeString(dataTypeOfDatasetForAlpha), "2023", "APPROVED"];
    cy.get("tbody td").should((elements) => {
      expect(elements.length).to.equal(6);
    });
    cy.get("tbody td").each((element, index) => {
      if (index < expectedRowContents.length) {
        expect(element.text()).to.equal(expectedRowContents[index]);
      } else if (index == 4) {
        expect(Date.parse(element.text()).toString()).not.to.equal(NaN.toString());
      } else if (index == 5) {
        expect(element.text()).to.contain("VIEW");
      }
    });
    cy.get("tbody td a").should(
      "have.attr",
      "href",
      `/companies/${nameOfCompanyAlpha}-Mock-Company-Id/frameworks/${dataTypeOfDatasetForAlpha}/${nameOfCompanyAlpha}-Mock-Data-Id`
    );
  });

  it("Validates the layout of the table header", () => {
    prepareSimpleDatasetOverviewTable([]);
    const expectedHeaders = ["COMPANY", "DATA FRAMEWORK", "SUBMISSION DATE", "REPORTING PERIOD", "STATUS"];
    expectedHeaders.forEach((value) => {
      cy.get(`table th:contains(${value})`).should("exist");
    });
    cy.get("th").each((element) => {
      if (!expectedHeaders.includes(element.text())) {
        expect(element.html()).to.contain("<input");
      }
    });
  });

  it("Check if search filter works as expected", () => {
    const someSearchStringThatMatchesNoCompany = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX123123123";
    prepareSimpleDatasetOverviewTable([datasetTableInfoMockForAlpha, datasetTableInfoMockForBeta]);
    cy.get("td").contains(nameOfCompanyAlpha).should("exist");
    cy.get("td").contains(nameOfCompanyBeta).should("exist");
    cy.get("input").type(nameOfCompanyAlpha.substring(3, 9));
    cy.get("td").contains(nameOfCompanyAlpha).should("exist");
    cy.get("td").contains(nameOfCompanyBeta).should("not.exist");
    cy.get("input").clear(); // empty input field
    cy.get("td").contains(nameOfCompanyAlpha).should("exist");
    cy.get("td").contains(nameOfCompanyBeta).should("exist");
    cy.get("input").type(nameOfCompanyBeta.substring(2, 9));
    cy.get("td").contains(nameOfCompanyAlpha).should("not.exist");
    cy.get("td").contains(nameOfCompanyBeta).should("exist");
    cy.get("input").type(someSearchStringThatMatchesNoCompany);
    cy.get("td").contains(nameOfCompanyAlpha).should("not.exist");
    cy.get("td").contains(nameOfCompanyBeta).should("not.exist");
  });
});
