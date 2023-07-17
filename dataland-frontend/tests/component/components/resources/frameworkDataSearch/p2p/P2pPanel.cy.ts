import P2pPanel from "@/components/resources/frameworkDataSearch/p2p/P2pPanel.vue";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import {
  CompanyAssociatedDataPathwaysToParisData,
  DataAndMetaInformationPathwaysToParisData,
  DataMetaInformation,
  DataTypeEnum,
  P2pSector,
  PathwaysToParisData,
  QaStatus,
} from "@clients/backend";
import { ReportingPeriodOfDataSetWithId, sortReportingPeriodsToDisplayAsColumns } from "@/utils/DataTableDisplay";

describe("Component test for P2pPanel", () => {
  let preparedFixtures: Array<FixtureData<PathwaysToParisData>>;

  before(function () {
    cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
    });
  });

  it("Should display the correct categories in the sector field", () => {
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
    cy.get(`span[data-test=General]`).click();
    cy.get(`span[data-test=_general]`).click();
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
    const preparedFixture = getPreparedFixture("one-p2p-data-set-with-three-sectors", preparedFixtures);
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
    cy.get(`span[data-test=General]`).click();
    toggleRowGroup("_general");
    cy.get("tbody").find(`span:contains(${p2pData.general.general.dataDate})`).should("exist");

    toggleRowGroup("_general");
    cy.get("tbody").find(`span:contains(${p2pData.general.general.dataDate})`).should("not.exist");

    toggleRowGroup("_general");
    cy.get("table.p-datatable-table").find(`span:contains(${p2pData.general.general.dataDate})`).should("exist");

    cy.get(`span[data-test=Ammonia]`).click();

    cy.get("span[data-test=ccsTechnologyAdoption]").should("not.exist");
    toggleRowGroup("decarbonisation");
    cy.get("span[data-test=ccsTechnologyAdoption]").should("exist");

    cy.get(`span[data-test="Livestock farming"]`).click();
    toggleRowGroup("animalFeed");
    cy.get("span[data-test=Report-Download-Policy]").find("i[data-test=download-icon]").should("be.visible");

    cy.get(`span[data-test=Cement]`).click();
    toggleRowGroup("material");
    cy.get("span[data-test=preCalcinedClayUsage]").should("exist");

    cy.get("em[title='Pre-calcined clay usage']").trigger("mouseenter", "center");
    cy.get(".p-tooltip").should("be.visible").contains("Share of pre-calcined");
    cy.get("em[title='Pre-calcined clay usage']").trigger("mouseleave");
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
    cy.get(`span[data-test=General]`).click();
    cy.get(`span[data-test=_general]`).click();
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
    const boolList = [false, true]; //Apparently Typescript doesn't like type conversions, so input is direct.
    for (let i = 0; i < 2; i++) {
      expect(
        swapAndSortReportingPeriodsToDisplayAsColumns([secondYearObject, firstYearObject], boolList[i])
      ).to.deep.equal([firstYearObject, secondYearObject]);

      expect(
        swapAndSortReportingPeriodsToDisplayAsColumns([secondOtherObject, firstOtherObject], boolList[i])
      ).to.deep.equal([firstOtherObject, secondOtherObject]);
    }
    expect(sortReportingPeriodsToDisplayAsColumns([firstYearObject, secondYearObject, firstYearObject])).to.deep.equal([
      firstYearObject,
      firstYearObject,
      secondYearObject,
    ]);
    expect(
      sortReportingPeriodsToDisplayAsColumns([firstYearObject, secondOtherObject, firstOtherObject])
    ).to.deep.equal([firstYearObject, firstOtherObject, secondOtherObject]);
  });
});

/**
 * Calls the testfunction for sorting and swaps the list entries if necessary.
 * @param  listOfDataDateToDisplayAsColumns list of objects to sort
 * @param boolSwap toogles the swap of both list elements in listOfDataDateToDisplayAsColumns (in case there are two.
 * Shortens the test-function and avoids code duplications.
 * @returns sorted list
 */
function swapAndSortReportingPeriodsToDisplayAsColumns(
  listOfDataDateToDisplayAsColumns: ReportingPeriodOfDataSetWithId[],
  boolSwap = false
): ReportingPeriodOfDataSetWithId[] {
  let swappedList: ReportingPeriodOfDataSetWithId[];
  if (boolSwap && listOfDataDateToDisplayAsColumns.length == 2) {
    swappedList = listOfDataDateToDisplayAsColumns.slice();
    swappedList[0] = listOfDataDateToDisplayAsColumns[1];
    swappedList[1] = listOfDataDateToDisplayAsColumns[0];
    listOfDataDateToDisplayAsColumns = swappedList.slice();
  }
  return sortReportingPeriodsToDisplayAsColumns(listOfDataDateToDisplayAsColumns);
}
