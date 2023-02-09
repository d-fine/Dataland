import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { Configuration, DataTypeEnum, SfdrData, SfdrDataControllerApi } from "@clients/backend";
import { uploadOneSfdrDataset, uploadCompanyAndSfdrDataViaApi } from "@e2e/utils/SfdrUpload";
import { getPreparedFixture, getStoredCompaniesForDataType, UploadIds } from "@e2e/utils/GeneralApiUtils";
import Chainable = Cypress.Chainable;
import { generateSfdrData } from "../../fixtures/sfdr/SfdrDataFixtures";

const dateAndMonthOfAdditionallyUploadedSfdrDataSets = "-12-31";
const monthAndDayOfSfdrPreparedFixtures = "-01-01";

describeIf(
  "As a user, I expect SFDR data that I upload for a company to be displayed correctly",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<SfdrData>>;

    before(function () {
      cy.log('<--------------preparedFixtures', preparedFixtures)
      cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;

      });
    });

    /**
     * Gets an Sfdr dataset based on the provided data ID and parses the Fiscal Year from its Fiscal Year field
     *
     * @param token The API bearer token to use
     * @param dataId The data ID of an Sfdr dataset
     * @returns the year from the Fiscal Year value of the Sfdr dataset as string
     */
    async function getReportingYearOfSfdrDataSet(token: string, dataId: string): Promise<string> {
      const response = await new SfdrDataControllerApi(
        new Configuration({ accessToken: token })
      ).getCompanyAssociatedSfdrData(dataId);
      const sfdrData = response.data.data as SfdrData;
      if (sfdrData) {
        const fiscalYearEndAsString = sfdrData.social!.general!.fiscalYearEnd as string;
        return new Date(fiscalYearEndAsString).getFullYear().toString();
      } else {
        throw Error(`No Sfdr dataset could be retrieved for the provided dataId ${dataId}`);
      }
    }

    /**
     * Uploads an LkSG dataset to an existing company that already has at least one LkSG dataset uploaded for.
     *
     * @param uploadIdsOfExistingCompanyAndSfdrDataSet contains the companyId of the existing company, and the data ID
     * of one already existing SFDR dataset for that company
     * @returns an object which contains the companyId of the already existing comapny and the dataId of the newly
     * uploaded LkSG dataset
     */
    function uploadAnotherSfdrDataSetToExistingCompany(
      uploadIdsOfExistingCompanyAndSfdrDataSet: UploadIds
    ): Chainable<UploadIds> {
      const companyId = uploadIdsOfExistingCompanyAndSfdrDataSet.companyId;
      const dataId = uploadIdsOfExistingCompanyAndSfdrDataSet.dataId;
      return getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return getReportingYearOfSfdrDataSet(token, dataId).then((reportingYearAsString) => {
          const reportingYear: number = +reportingYearAsString;
          const fiscalYearEndOfNewSfdrDataSet = reportingYear + 1;
          const newSfdrDataSet = generateSfdrData(
            fiscalYearEndOfNewSfdrDataSet.toString() + dateAndMonthOfAdditionallyUploadedSfdrDataSets
          );
          return uploadOneSfdrDataset(token, companyId, newSfdrDataSet).then((dataMetaInformation) => {
            return { companyId: companyId, dataId: dataMetaInformation.dataId };
          });
        });
      });
    }

    it("Check Sfdr view page for company with one Sfdr data set", () => {
      const preparedFixture = getPreparedFixture("company-with-sfdr-data-set", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const sfdrData = preparedFixture.t;

      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        return uploadCompanyAndSfdrDataViaApi(token, companyInformation, sfdrData).then((uploadIds) => {
          cy.intercept("**/api/data/sfdr/company/*").as("retrieveSfdrData");
          cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/sfdr`);
          cy.wait("@retrieveSfdrData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(() => {
            cy.get(`h1`).should("contain", companyInformation.companyName);


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
        });
      });
    });

  }
);
