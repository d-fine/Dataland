import LksgPanel from "@/components/resources/frameworkDataSearch/lksg/LksgPanel.vue";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import {
  CompanyAssociatedDataLksgData,
  DataAndMetaInformationLksgData,
  DataMetaInformation,
  DataTypeEnum,
  LksgData,
  QaStatus,
} from "@clients/backend";
import { sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";

describe("Component test for LksgPanel", () => {
  let preparedFixtures: Array<FixtureData<LksgData>>;

  before(function () {
    cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it("Should display the total revenue kpi in the correct format", () => {
    const pseudoLksgData = {
      general: { masterData: { dataDate: "2023-01-01", totalRevenue: 1234567.89 } },
    } as LksgData;

    cy.mountWithPlugins(LksgPanel, {
      data() {
        return {
          waitingForData: false,
          lksgDataAndMetaInfo: [{ data: pseudoLksgData } as DataAndMetaInformationLksgData],
        };
      },
      // The code below is required to complete the component mock yet interferes with the type resolution of the
      // mountWithPlugins function.
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      created() {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,
        this.convertLksgDataToFrontendFormat();
      },
    });
    cy.get("td:contains('1.23 MM')").should("exist");
  });

  it("Should be able to handle null values in a Lksg dataset and display rows for those values", () => {
    const preparedFixture = getPreparedFixture("lksg-a-lot-of-nulls", preparedFixtures);
    cy.mountWithPlugins(LksgPanel, {
      data() {
        return {
          waitingForData: false,
          lksgDataAndMetaInfo: [{ data: preparedFixture.t } as DataAndMetaInformationLksgData],
        };
      },
      // The code below is required to complete the component mock yet interferes with the type resolution of the
      // mountWithPlugins function.
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      created() {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,
        this.convertLksgDataToFrontendFormat();
      },
    });
    // make sure only dataDate is there and other cards aren't
    cy.contains("span", "1999-12-24").should("exist");
    cy.contains("td.headers-bg", "Industry").should("exist");
    cy.get("em").its("length").should("equal", 11);
    cy.get("tr").its("length").should("equal", 13);
  });

  /**
   * Toggles the data-table row group with the given key
   * @param groupKey the key of the row group to expand
   */
  function toggleRowGroup(groupKey: string): void {
    cy.get(`span[id=${groupKey}]`).siblings("button").last().click();
  }

  it("Check Lksg view page for company with one Lksg data set", () => {
    const preparedFixture = getPreparedFixture("one-lksg-data-set-with-two-production-sites", preparedFixtures);
    const lksgData = preparedFixture.t;

    cy.intercept("/api/data/lksg/mock-data-id", {
      companyId: "mock-company-id",
      reportingPeriod: preparedFixture.reportingPeriod,
      data: lksgData,
    } as CompanyAssociatedDataLksgData);
    cy.mountWithPlugins(LksgPanel, {
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

    cy.get(`span.p-column-title`).should("contain.text", lksgData.general.masterData.dataDate.substring(0, 4));
    cy.get("tbody").find(`span:contains(${lksgData.general.masterData.dataDate})`).should("exist");

    toggleRowGroup("_masterData");
    cy.get("tbody").find(`span:contains(${lksgData.general.masterData.dataDate})`).should("not.exist");

    toggleRowGroup("_masterData");
    cy.get("table.p-datatable-table").find(`span:contains(${lksgData.general.masterData.dataDate})`).should("exist");

    cy.get("span[data-test=employeeUnder18]").should("not.exist");
    toggleRowGroup("childLabor");
    cy.get("span[data-test=employeeUnder18]").should("exist");

    toggleRowGroup("productionSpecific");
    cy.get(`a:contains(Show "List Of Production Sites")`).should("be.visible");

    cy.get("em[title='Data Date']").trigger("mouseenter", "center");
    cy.get(".p-tooltip").should("be.visible").contains("The date until when");
    cy.get("em[title='Data Date']").trigger("mouseleave");

    toggleRowGroup("certificationsPoliciesAndResponsibilities");
    cy.get("span[data-test=Report-Download-Certification]").find("i[data-test=download-icon]").should("be.visible");
  });

  /**
   * This functions imitates an api response of the /data/lksg/companies/mock-company-id endpoint
   * to include 6 active Lksg datasets from different years to test the simultaneous display of multiple Lksg
   * datasets (constructed datasets range from 2023 to 2028)
   * @param baseDataset the lksg dataset used as a basis for constructing the 6 mocked ones
   * @returns a mocked api response
   */
  function constructCompanyApiResponseForLksgForSixYears(baseDataset: LksgData): DataAndMetaInformationLksgData[] {
    const lksgDatasets: DataAndMetaInformationLksgData[] = [];
    for (let i = 0; i < 6; i++) {
      const reportingYear = 2023 + i;
      const reportingDate = `${reportingYear}-01-01`;
      const lksgData = structuredClone(baseDataset) as LksgData;
      lksgData.general.masterData.dataDate = reportingDate;
      const metaData: DataMetaInformation = {
        dataId: `dataset-${i}`,
        reportingPeriod: reportingYear.toString(),
        qaStatus: QaStatus.Accepted,
        currentlyActive: true,
        dataType: DataTypeEnum.Lksg,
        companyId: "mock-company-id",
        uploadTime: 0,
        uploaderUserId: "mock-uploader-id",
      };

      lksgDatasets.push({
        metaInfo: metaData,
        data: lksgData,
      });
    }
    return lksgDatasets;
  }

  it("Check Lksg view page for company with six Lksg data sets reported in different years ", () => {
    const preparedFixture = getPreparedFixture("six-lksg-data-sets-in-different-years", preparedFixtures);
    const mockedData = constructCompanyApiResponseForLksgForSixYears(preparedFixture.t);
    cy.intercept("/api/data/lksg/companies/mock-company-id", mockedData);

    cy.mountWithPlugins(LksgPanel, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyId: "mock-company-id",
        };
      },
    });
    cy.get("table").find(`tr:contains("Data Date")`).find(`span`).eq(6).contains("2023");

    for (let indexOfColumn = 1; indexOfColumn <= 6; indexOfColumn++) {
      cy.get(`span.p-column-title`)
        .eq(indexOfColumn)
        .should("contain.text", (2029 - indexOfColumn).toString());
    }
  });

  it("Unit test for sortReportingPeriodsToDisplayAsColumns", () => {
    const firstYearObject = { dataId: "5", reportingPeriod: "2022" };
    const secondYearObject = { dataId: "2", reportingPeriod: "2020" };
    const firstOtherObject = { dataId: "3", reportingPeriod: "Q2-2020" };
    const secondOtherObject = { dataId: "6", reportingPeriod: "Q3-2020" };
    expect(sortReportingPeriodsToDisplayAsColumns([secondYearObject, firstYearObject])).to.deep.equal([
      firstYearObject,
      secondYearObject,
    ]);
    expect(sortReportingPeriodsToDisplayAsColumns([firstYearObject, secondYearObject])).to.deep.equal([
      firstYearObject,
      secondYearObject,
    ]);
    expect(sortReportingPeriodsToDisplayAsColumns([firstYearObject, secondYearObject, firstYearObject])).to.deep.equal([
      firstYearObject,
      firstYearObject,
      secondYearObject,
    ]);
    expect(sortReportingPeriodsToDisplayAsColumns([secondOtherObject, firstOtherObject])).to.deep.equal([
      firstOtherObject,
      secondOtherObject,
    ]);
    expect(sortReportingPeriodsToDisplayAsColumns([firstOtherObject, secondOtherObject])).to.deep.equal([
      firstOtherObject,
      secondOtherObject,
    ]);
    expect(
      sortReportingPeriodsToDisplayAsColumns([firstYearObject, secondOtherObject, firstOtherObject])
    ).to.deep.equal([firstYearObject, firstOtherObject, secondOtherObject]);
  });
});
