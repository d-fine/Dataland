import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  LksgDataControllerApi,
  type LksgProductionSite,
  QaStatus,
  type StoredCompany,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { generateProductionSite } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { getSectionHead } from "@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";

describeIf(
  "As a user, I expect to be able to upload LkSG data via an upload form, and that the uploaded data is displayed " +
    "correctly in the frontend",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    let lksgFixtureWithNoNullFields: FixtureData<LksgData>;

    before(function () {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
        lksgFixtureWithNoNullFields = getPreparedFixture("Lksg-dataset-with-no-null-fields", preparedFixturesLksg);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it(
      "Create a company and a Lksg dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-Lksg-Blanket-Test" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.Lksg,
              token,
              storedCompany.companyId,
              "2021",
              lksgFixtureWithNoNullFields.t, // TODO dataset
            ).then((dataMetaInformation) => {
              cy.intercept("**/api/companies/" + storedCompany.companyId).as("getCompanyInformation");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.Lksg +
                  "/upload?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("h1").should("contain", testCompanyName);
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.Lksg}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (postInterception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new LksgDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedLksgData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosGetResponse) => {
                      const frontendSubmittedP2pDataset = axiosGetResponse.data.data as unknown as Record<
                        string,
                        object
                      >;
                      const originallyUploadedP2pDataset = lksgFixtureWithNoNullFields.t as unknown as Record<
                        string,
                        object
                      >;
                      compareObjectKeysAndValuesDeep(originallyUploadedP2pDataset, frontendSubmittedP2pDataset);
                    });
                },
              );
            });
          });
        });
      },
    );

    it("Check if the list of production sites is displayed as expected", () => {
      // TODO component test?
      cy.fixture("MetaInfoDataForCompany.json").then((metaInfos) => {
        cy.fixture("CompanyInformationWithLksgPreparedFixtures").then((lksgDataSets) => {
          const lksgData = prepareLksgViewIntercepts(
            getPreparedFixture("one-lksg-data-set-with-two-production-sites", lksgDataSets as FixtureData<LksgData>[]),
            (metaInfos as DataMetaInformation[])[0],
          );
          cy.visit(`/companies/company-id/frameworks/${DataTypeEnum.Lksg}`);
          getSectionHead("Production-specific").should("have.attr", "data-section-expanded", "false").click();

          cy.get(`a:contains(Show List Of Production Sites)`).click();
          lksgData.general.productionSpecific!.listOfProductionSites!.forEach((productionSite: LksgProductionSite) => {
            if (productionSite.addressOfProductionSite?.streetAndHouseNumber) {
              cy.get("tbody.p-datatable-tbody p").contains(productionSite.addressOfProductionSite.streetAndHouseNumber);
            }
          });
          cy.get("div.p-dialog-mask").click({ force: true });
        });
      });
    });

    /**
     * Sets the stubs for the API requests on the LkSG view page
     * @param lksgDataFixture a fixture with company information and test LkSG data
     * @param dataMetaInfo a set of metadata to be adapted
     * @returns the LkSG data used for the data stub
     */
    function prepareLksgViewIntercepts(
      lksgDataFixture: FixtureData<LksgData>,
      dataMetaInfo: DataMetaInformation,
    ): LksgData {
      dataMetaInfo.dataType = DataTypeEnum.Lksg;
      dataMetaInfo.qaStatus = QaStatus.Accepted;
      dataMetaInfo.currentlyActive = true;
      const storedCompany = {
        companyId: "company-id",
        companyInformation: lksgDataFixture.companyInformation,
        dataRegisteredByDataland: [dataMetaInfo] as Array<DataMetaInformation>,
      } as StoredCompany;
      cy.intercept(`**/api/metadata*`, {
        statusCode: 200,
        body: [dataMetaInfo],
      });
      cy.intercept(`**/api/companies/*`, {
        statusCode: 200,
        body: storedCompany,
      });
      const lksgData = lksgDataFixture.t;
      if (lksgData.general?.productionSpecific?.listOfProductionSites) {
        lksgData.general.productionSpecific.listOfProductionSites = [
          generateProductionSite(),
          generateProductionSite(),
        ];
      }
      cy.intercept(`**/api/data/${DataTypeEnum.Lksg}/companies/*`, {
        statusCode: 200,
        body: [
          {
            metaInfo: dataMetaInfo,
            data: lksgData,
          },
        ],
      });
      return lksgData;
    }
  },
);
