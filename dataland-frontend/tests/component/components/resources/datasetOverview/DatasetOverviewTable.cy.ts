import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import { DatasetTableInfo, DatasetStatus } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { DataTypeEnum } from "@clients/backend";
import Keycloak from "keycloak-js";
import { humanizeString } from "@/utils/StringHumanizer";

describe("Component test for DatasetOverviewTable", () => {
  it("Check if the table rows look as expected", () => {
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
    cy.mount(DatasetOverviewTable, {
      keycloak: {} as Keycloak,
      propsOverride: {
        datasetTableInfos: mockData,
      },
    });
    const expectedRowContents = [
      `COMPANY${mockDataEntry.companyName}`,
      `DATA FRAMEWORK${humanizeString(DataTypeEnum.Lksg)}`,
      `REPORTING PERIOD${mockDataEntry.dataReportingPeriod}`,
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
});
