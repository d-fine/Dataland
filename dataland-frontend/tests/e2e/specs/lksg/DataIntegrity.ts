import { describeIf } from "@e2e/support/TestUtility";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  DataMetaInformation,
  DataTypeEnum,
  LksgData,
  LksgProductionSite,
  QAStatus,
  StoredCompany,
} from "@clients/backend";
import { uploadLksgDataViaForm } from "@e2e/utils/LksgUpload";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData } from "@sharedUtils/Fixtures";
import { generateProductionSite } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";

describeIf(
  "As a user, I expect to be able to upload LkSG data via an upload form, and that the uploaded data is displayed " +
    "correctly in the frontend",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    it("Create a company via api and upload an LkSG dataset via the LkSG upload form", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
      getKeycloakToken(uploader_name, uploader_pw)
        .then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
        })
        .then((storedCompany) => {
          cy.intercept("**/api/companies/" + storedCompany.companyId).as("getCompanyInformation");
          cy.visitAndCheckAppMount(
            "/companies/" + storedCompany.companyId + "/frameworks/" + DataTypeEnum.Lksg + "/upload"
          );
          cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
          cy.url().should(
            "eq",
            getBaseUrl() + "/companies/" + storedCompany.companyId + "/frameworks/" + DataTypeEnum.Lksg + "/upload"
          );
          cy.get("h1").should("contain", testCompanyName);
          uploadLksgDataViaForm();
        });
    });

    it("Check if the list of production sites is displayed as expected", () => {
      cy.fixture("MetaInfoDataForCompany.json").then((metaInfos) => {
        cy.fixture("CompanyInformationWithLksgData.json").then((lksgDataSets) => {
          const lksgData = prepareLksgViewIntercepts(
            (lksgDataSets as FixtureData<LksgData>[])[0],
            (metaInfos as DataMetaInformation[])[0]
          );
          cy.visit(`/companies/company-id/frameworks/${DataTypeEnum.Lksg}`);
          cy.get("table.p-datatable-table").find(`a:contains(Show "List Of Production Sites")`).click();
          const listOfProductionSites = assertDefined(lksgData.general?.productionSpecific?.listOfProductionSites);
          if (listOfProductionSites.length < 2) {
            throw Error("This test only accepts an Lksg-dataset which has at least two production sites.");
          }
          listOfProductionSites.forEach((productionSite: LksgProductionSite) => {
            if (productionSite.addressOfProductionSite && productionSite.addressOfProductionSite.streetAndHouseNumber) {
              cy.get("tbody.p-datatable-tbody").find(
                `span:contains(${productionSite.addressOfProductionSite.streetAndHouseNumber})`
              );
            }
          });
          cy.get("div.p-dialog-mask").click({ force: true });
        });
      });
    });

    /**
     * Sets the stubs for the API requests on the LkSG view page
     *
     * @param lksgDataFixture a fixture with company information and test LkSG data
     * @param dataMetaInfo a set of meta data to be adapted
     * @returns the LkSG data used for the data stub
     */
    function prepareLksgViewIntercepts(
      lksgDataFixture: FixtureData<LksgData>,
      dataMetaInfo: DataMetaInformation
    ): LksgData {
      dataMetaInfo.dataType = DataTypeEnum.Lksg;
      dataMetaInfo.qaStatus = QAStatus.Accepted;
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
      if (lksgData.general?.listOfProductionSites) {
        lksgData.general.listOfProductionSites = [generateProductionSite(), generateProductionSite()];
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
  }
);
