import DatasetOverviewTable from "@/components/resources/datasetOverview/DatasetOverviewTable.vue";
import { DatasetTableInfo, DatasetStatus } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { DataTypeEnum } from "@clients/backend";
import Keycloak from "keycloak-js";

describe("Component test for DatasetOverviewTable", () => {
  it("Checks that outdated datasets are displayed as such", () => {
    const mockDataEntry: DatasetTableInfo = {
      dataId: "Mock-Data-Id",
      companyId: "Mock-Company-Id",
      dataType: DataTypeEnum.Lksg,
      companyName: "Mock-Company-Name",
      dataReportingPeriod: "2023",
      status: DatasetStatus.Outdated,
      uploadTimeInMs: 1672527600000, // 1.1.2023 00:00:00:0000
    };
    const mockData = [mockDataEntry];
    cy.mount(DatasetOverviewTable, {
      keycloak: {} as Keycloak,
      propsOverride: {
        datasetTableInfos: mockData,
      },
    });
    cy.get("div.p-badge").contains("OUTDATED").should("exist");
  });
});
