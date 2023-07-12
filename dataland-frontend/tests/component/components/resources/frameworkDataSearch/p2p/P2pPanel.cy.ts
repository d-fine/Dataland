import P2pPanel from "../../../../../../src/components/resources/frameworkDataSearch/p2p/P2pPanel.vue";
import { FixtureData, getPreparedFixture } from "../../../../../sharedUtils/Fixtures";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import {
  CompanyAssociatedDataPathwaysToParisData,
  DataAndMetaInformationPathwaysToParisData,
  DataMetaInformation,
  DataTypeEnum,
  P2pSector,
  PathwaysToParisData,
  QaStatus,
} from "../../../../../../build/clients/backend";
import { sortReportingPeriodsToDisplayAsColumns } from "../../../../../../src/utils/DataTableDisplay";

describe("Component test for P2pPanel", () => {
  let preparedFixtures: Array<FixtureData<PathwaysToParisData>>;

  before(function () {
    cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
    });
  });

  it("Should display the total revenue kpi in the correct format", () => {
    const pseudoP2pData = {
      general: { general: { dataDate: "2023-01-01", sector: [P2pSector.Ammonia] } },
    } as PathwaysToParisData;

    cy.mountWithPlugins(P2pPanel, {
      data() {
        return {
          waitingForData: false,
          p2pDataAndMetaInfo: [{ data: pseudoP2pData } as DataAndMetaInformationPathwaysToParisData],
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
        this.convertP2pDataToFrontendFormat();
      },
    });
    cy.get("td:contains('Ammonia')").should("exist");
  });

  /**
   * Toggles the data-table row group with the given key
   * @param groupKey the key of the row group to expand
   */
  function toggleRowGroup(groupKey: string): void {
    cy.get(`span[id=${groupKey}]`).siblings("button").last().click();
  }

  it("Check P2p view page for company with one P2p data set", () => {
    const preparedFixture = getPreparedFixture("one-p2p-data-set-with-two-production-sites", preparedFixtures);
    const p2pData = preparedFixture.t;

    cy.intercept("/api/data/p2p/mock-data-id", {
      companyId: "mock-company-id",
      reportingPeriod: preparedFixture.reportingPeriod,
      data: p2pData,
    } as CompanyAssociatedDataPathwaysToParisData);
    cy.mountWithPlugins(P2pPanel, {
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

    cy.get(`span.p-column-title`).should("contain.text", p2pData.general.general.dataDate.substring(0, 4));
    cy.get("tbody").find(`span:contains(${p2pData.general.general.dataDate})`).should("exist");

    toggleRowGroup("_masterData");
    cy.get("tbody").find(`span:contains(${p2pData.general.general.dataDate})`).should("not.exist");

    toggleRowGroup("_masterData");
    cy.get("table.p-datatable-table").find(`span:contains(${p2pData.general.general.dataDate})`).should("exist");

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
  function constructCompanyApiResponseForP2pForSixYears(
    baseDataset: PathwaysToParisData
  ): DataAndMetaInformationPathwaysToParisData[] {
    const p2pDatasets: DataAndMetaInformationPathwaysToParisData[] = [];
    for (let i = 0; i < 6; i++) {
      const reportingYear = 2023 + i;
      const reportingDate = `${reportingYear}-01-01`;
      const p2pData = structuredClone(baseDataset) as PathwaysToParisData;
      p2pData.general.general.dataDate = reportingDate;
      const metaData: DataMetaInformation = {
        dataId: `dataset-${i}`,
        reportingPeriod: reportingYear.toString(),
        qaStatus: QaStatus.Accepted,
        currentlyActive: true,
        dataType: DataTypeEnum.P2p,
        companyId: "mock-company-id",
        uploadTime: 0,
        uploaderUserId: "mock-uploader-id",
      };

      p2pDatasets.push({
        metaInfo: metaData,
        data: p2pData,
      });
    }
    return p2pDatasets;
  }

  it("Check P2p view page for company with six P2p data sets reported in different years ", () => {
    const preparedFixture = getPreparedFixture("six-p2p-data-sets-in-different-years", preparedFixtures);
    const mockedData = constructCompanyApiResponseForP2pForSixYears(preparedFixture.t);
    cy.intercept("/api/data/p2p/companies/mock-company-id", mockedData);

    cy.mountWithPlugins(P2pPanel, {
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
