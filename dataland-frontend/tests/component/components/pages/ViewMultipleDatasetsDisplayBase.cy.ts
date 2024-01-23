import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import ViewMultipleDatasetsDisplayBase from "@/components/generics/ViewMultipleDatasetsDisplayBase.vue";
import {
  type DataAndMetaInformationLksgData,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  QaStatus,
} from "@clients/backend";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

describe("Component test for the view multiple dataset display base component", () => {
  let preparedFixtures: Array<FixtureData<LksgData>>;

  before(function () {
    cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it("Checks, if the toggle of hidden fields works for empty and conditional fields", () => {
    const preparedFixture = getPreparedFixture("lksg-with-nulls-and-no-child-labor-under-18", preparedFixtures);
    const mockedData = constructCompanyApiResponseForLksg(preparedFixture.t);
    const companyInformationObject = preparedFixture.companyInformation;
    cy.intercept(`/api/companies/mock-company-id/info`, companyInformationObject);
    cy.intercept(`/api/data/lksg/dataset-a`, {
      companyId: mockedData.metaInfo.companyId,
      reportingPeriod: mockedData.metaInfo.reportingPeriod,
      data: mockedData.data,
    });
    cy.intercept(`/api/metadata?companyId=mock-company-id`, [mockedData.metaInfo]);
    cy.mountWithPlugins(ViewMultipleDatasetsDisplayBase, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyId: mockedData.metaInfo.companyId,
        dataType: DataTypeEnum.Lksg,
        reportingPeriod: mockedData.metaInfo.reportingPeriod,
        viewInPreviewMode: false,
      },
    });
    checkToggleEmptyFieldsSwitch("Number of Employees");
    cy.get('tr[data-section-label="Social"]').click();
    cy.get('tr[data-section-label="Child labor"]').click();
    cy.get('td[data-cell-label="Employee(s) Under 15"]').should("not.exist");
  });
});

/**
 * This functions imitates an api response of the /data/lksg/companies/mock-company-id endpoint
 * to include 6 active Lksg datasets from different years to test the simultaneous display of multiple Lksg
 * datasets (constructed datasets range from 2023 to 2028)
 * @param baseDataset the lksg dataset used as a basis for constructing the 6 mocked ones
 * @returns a mocked api response
 */
function constructCompanyApiResponseForLksg(baseDataset: LksgData): DataAndMetaInformationLksgData {
  const reportingYear = 2023;
  const reportingDate = `${reportingYear}-01-01`;
  const lksgData: LksgData = structuredClone(baseDataset);
  lksgData.general.masterData.dataDate = reportingDate;
  const metaData: DataMetaInformation = {
    dataId: `dataset-a`,
    reportingPeriod: reportingYear.toString(),
    qaStatus: QaStatus.Accepted,
    currentlyActive: true,
    dataType: DataTypeEnum.Lksg,
    companyId: "mock-company-id",
    uploadTime: 0,
    uploaderUserId: "mock-uploader-id",
  };
  return { metaInfo: metaData, data: lksgData };
}

/**
 * This function toggles the hide data button and checks whether a specific field is hidden or displayed.
 * @param toggledFieldName Name of a field which is toggled by the input switch
 */
export function checkToggleEmptyFieldsSwitch(toggledFieldName: string): void {
  cy.wait(100);
  cy.get("span").contains(toggledFieldName).should("not.exist");
  cy.get('span[data-test="hideEmptyDataToggleCaption"]').should("exist");
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should("have.class", "p-inputswitch-checked").click();
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should("not.have.class", "p-inputswitch-checked");
  cy.get("span").contains(toggledFieldName).should("exist");
  cy.get('div[data-test="hideEmptyDataToggleButton"]').click();
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should("have.class", "p-inputswitch-checked");
  cy.get("span").contains(toggledFieldName).should("not.exist");
}
