import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import SfdrPanel from "@/components/resources/frameworkDataSearch/sfdr/SfdrPanel.vue";
import {
  CompanyAssociatedDataSfdrData,
  DataMetaInformation,
  DataAndMetaInformationSfdrData,
  DataTypeEnum,
  QAStatus,
  SfdrData,
} from "@clients/backend";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
describe("Component tests for SfdrPanel", () => {
  let preparedFixtures: Array<FixtureData<SfdrData>>;

  before(function () {
    cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
    });
  });

  it("Check Sfdr view page for company with one Sfdr data set", () => {
    const preparedFixture = getPreparedFixture("company-with-one-sfdr-data-set", preparedFixtures);
    const sfdrData = preparedFixture.t;

    cy.intercept("/api/data/sfdr/mock-data-id", {
      companyId: "mock-company-id",
      reportingPeriod: preparedFixture.reportingPeriod,
      data: sfdrData,
    } as CompanyAssociatedDataSfdrData);
    cy.mountWithPlugins(SfdrPanel, {
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
      .find(`span:contains(${sfdrData.social!.general!.fiscalYearEnd!})`)
      .should("exist");

    cy.get("button.p-row-toggler").eq(0).click();
    cy.get("table.p-datatable-table")
      .find(`span:contains(${sfdrData.social!.general!.fiscalYearEnd!})`)
      .should("not.exist");

    cy.get("button.p-row-toggler").eq(0).click();
    cy.get("table.p-datatable-table")
      .find(`span:contains(${sfdrData.social!.general!.fiscalYearEnd!})`)
      .should("exist");
  });

  /**
   * This functions imitates an api response of the /data/sfdr/companies/mock-company-id endpoint
   * to include 6 active Sfdr datasets from different years to test the simultaneous display of multiple Sfdr
   * datasets (constructed datasets range from 2023 to 2028)
   *
   * @param baseDataset the SFDR dataset used as a basis for constructing the 6 mocked ones
   * @returns a mocked api response
   */
  function constructCompanyApiResponseForSfdrSixYears(baseDataset: SfdrData): DataAndMetaInformationSfdrData[] {
    const sfdrDatasets: DataAndMetaInformationSfdrData[] = [];
    for (let i = 0; i < 6; i++) {
      const reportingYear = 2023 + i;
      const fiscalYearEnd = `${reportingYear}-01-01`;
      const sfdrData = structuredClone(baseDataset) as SfdrData;
      sfdrData.social!.general!.fiscalYearEnd = fiscalYearEnd;
      const metaData: DataMetaInformation = {
        dataId: `dataset-${i}`,
        reportingPeriod: reportingYear.toString(),
        qaStatus: QAStatus.Accepted,
        currentlyActive: true,
        dataType: DataTypeEnum.Sfdr,
        companyId: "mock-company-id",
        uploadTime: 0,
        uploaderUserId: "mock-uploader-id",
      };

      sfdrDatasets.push({
        metaInfo: metaData,
        data: sfdrData,
      });
    }
    return sfdrDatasets;
  }

  it("Check Sfdr view page for company with six Sfdr datasets reported in different years", () => {
    const preparedFixture = getPreparedFixture("company-with-one-sfdr-data-set", preparedFixtures);
    const mockedData = constructCompanyApiResponseForSfdrSixYears(preparedFixture.t);
    cy.intercept("/api/data/sfdr/companies/mock-company-id", mockedData);

    cy.mountWithPlugins(SfdrPanel, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyId: "mock-company-id",
        };
      },
    });
    cy.get("table").find(`tr:contains("Fiscal Year End")`).find(`span`).eq(6).contains("2023-01-01");

    for (let indexOfColumn = 1; indexOfColumn <= 6; indexOfColumn++) {
      cy.get(`span.p-column-title`)
        .eq(indexOfColumn)
        .should("contain.text", (2029 - indexOfColumn).toString());
    }
  });
});
