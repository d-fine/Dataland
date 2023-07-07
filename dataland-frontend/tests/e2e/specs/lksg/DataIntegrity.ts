import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  DataMetaInformation,
  DataTypeEnum,
  LksgData,
  LksgProductionSite,
  QaStatus,
  StoredCompany,
} from "@clients/backend";
import { uploadLksgDataViaForm } from "@e2e/utils/LksgUpload";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { generateProductionSite } from "@e2e/fixtures/lksg/LksgDataFixtures";

describeIf(
  "As a user, I expect to be able to upload LkSG data via an upload form, and that the uploaded data is displayed " +
    "correctly in the frontend",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    /**
     * Toggles the data-table row group with the given key
     * @param groupKey the key of the row group to expand
     */
    function toggleRowGroup(groupKey: string): void {
      cy.get(`span[data-test=${groupKey}]`).siblings("button").last().click();
    }

    /**
     * validates that the data uploaded via the function `uploadLksgDataViaForm` is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     */
    function validateFormUploadedData(companyId: string): void {
      cy.visit("/companies/" + companyId + "/frameworks/" + DataTypeEnum.Lksg);
      cy.get('td > [data-test="productionSpecificOwnOperations"]').click();
      cy.contains('Show "Most Important Products"').click();
      cy.get(".p-dialog").find(".p-dialog-title").should("have.text", "Most Important Products");
      cy.get(".p-dialog th").eq(0).should("have.text", "Product Name");
      cy.get(".p-dialog th").eq(1).should("have.text", "Production Steps");
      cy.get(".p-dialog th").eq(2).should("have.text", "Related Corporate Supply Chain");
      cy.get(".p-dialog tr").should("have.length", 3);
      cy.get(".p-dialog tr").eq(1).find("td").eq(0).should("have.text", "Test Product 1");
      cy.get(".p-dialog tr").eq(1).find("td").eq(1).find("li").should("have.length", 2);
      cy.get(".p-dialog tr").eq(1).find("td").eq(1).find("li").eq(0).should("have.text", "first");
      cy.get(".p-dialog tr").eq(1).find("td").eq(1).find("li").eq(1).should("have.text", "second");
      cy.get(".p-dialog tr").eq(1).find("td").eq(2).should("have.text", "Description of something");
      cy.get(".p-dialog tr").eq(2).find("td").eq(0).should("have.text", "Test Product 2");
    }

    it("Create a company via api and upload an LkSG dataset via the LkSG upload form", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
      getKeycloakToken(uploader_name, uploader_pw)
        .then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
        })
        .then((storedCompany) => {
          cy.ensureLoggedIn(admin_name, admin_pw);
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
          validateFormUploadedData(storedCompany.companyId);
        });
    });

    it("Check if the list of production sites is displayed as expected", () => {
      cy.fixture("MetaInfoDataForCompany.json").then((metaInfos) => {
        cy.fixture("CompanyInformationWithLksgPreparedFixtures").then((lksgDataSets) => {
          const lksgData = prepareLksgViewIntercepts(
            getPreparedFixture("one-lksg-data-set-with-two-production-sites", lksgDataSets as FixtureData<LksgData>[]),
            (metaInfos as DataMetaInformation[])[0]
          );
          cy.visit(`/companies/company-id/frameworks/${DataTypeEnum.Lksg}`);
          toggleRowGroup("productionSpecific");
          cy.get(`a:contains(Show "List Of Production Sites")`).click();
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
     * @param dataMetaInfo a set of meta data to be adapted
     * @returns the LkSG data used for the data stub
     */
    function prepareLksgViewIntercepts(
      lksgDataFixture: FixtureData<LksgData>,
      dataMetaInfo: DataMetaInformation
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
  }
);
