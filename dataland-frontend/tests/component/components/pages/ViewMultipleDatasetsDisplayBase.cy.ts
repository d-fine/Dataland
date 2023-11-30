import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import ViewMultipleDatasetsDisplayBase from "@/components/generics/ViewMultipleDatasetsDisplayBase.vue";
import {
  type DataAndMetaInformationLksgData,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  QaStatus,
} from "../../../../build/clients/backend";
import { type FixtureData, getPreparedFixture } from "../../../sharedUtils/Fixtures";
import { type DataAndMetaInformation } from "../../../../src/api-models/DataAndMetaInformation";

describe("This describes a component test for the view Page", () => {
  let preparedFixtures: Array<FixtureData<LksgData>>;

  before(function () {
    cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it("Checks, if the toggle of hidden fields works", () => {
    const preparedFixture = getPreparedFixture("lksg-with-nulls-and-child-labor-under-18", preparedFixtures);
    const mockedData = constructCompanyApiResponseForLksg(preparedFixture.t);
    console.log("abc:", mockedData);
    const mockCompanyId = mockedData.metaInfo.companyId;
    const companyInformationObject = preparedFixture.companyInformation;
    cy.intercept(`/api/companies/mock-company-id/info`, companyInformationObject);
    cy.intercept(`/api/metadata/dataset-a`, mockedData.metaInfo);
    cy.intercept(`/api/data/lksg/dataset-a`, mockedData.data);

    cy.intercept("**/api/metadata*", { fixture: "MetaInfoDataMocksForOneCompany", times: 1 }).as("metaDataFetch");
    cy.mountWithPlugins(ViewMultipleDatasetsDisplayBase, {
      keycloak: minimalKeycloakMock({}),
      props: {
        companyId: mockCompanyId,
        dataType: DataTypeEnum.Lksg,
        dataId: mockedData.metaInfo.dataId,
        reportingPeriod: preparedFixture.reportingPeriod,
        viewInPreviewMode: false,
      },
    }).then(() => {});
  });
});

/**
 * This functions imitates an api response of the /data/lksg/companies/mock-company-id endpoint
 * to include 6 active Lksg datasets from different years to test the simultaneous display of multiple Lksg
 * datasets (constructed datasets range from 2023 to 2028)
 * @param baseDataset the lksg dataset used as a basis for constructing the 6 mocked ones
 * @returns a mocked api response
 */
function constructCompanyApiResponseForLksg(baseDataset: LksgData): DataAndMetaInformation<LksgData> {
  const reportingYear = 2023;
  const reportingDate = `${reportingYear}-01-01`;
  const lksgData = structuredClone(baseDataset);
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
  const lksgDataset: DataAndMetaInformationLksgData = {
    metaInfo: metaData,
    data: lksgData,
  };
  return lksgDataset;
}
