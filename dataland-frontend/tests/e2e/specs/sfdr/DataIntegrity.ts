import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { Configuration, SfdrDataControllerApi, SfdrData } from "@clients/backend";
import { uploadCompanyAndSfdrDataViaApi, uploadOneSfdrDataset } from "@e2e/utils/SfdrUpload";
import { getPreparedFixture, UploadIds } from "@e2e/utils/GeneralApiUtils";
import { generateSfdrData } from "../../fixtures/sfdr/SfdrDataFixtures";
import Chainable = Cypress.Chainable;

const dateAndMonthOfAdditionallyUploadedLksgDataSets = "-11-12";

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
      cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
      });
    });

    /**
     * Uploads an SFDR dataset to an existing company that already has at least one SFDR dataset uploaded for.
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
        return getFiscalYearEndOfSfdrDataSet(token, dataId).then((fiscalYearEndAsString) => {
          const fiscalYearEnd: number = +fiscalYearEndAsString;
          const reportingYearOfNewSfdrDataSet = fiscalYearEnd + 1;
          const newSfdrDataSet = generateSfdrData(
            reportingYearOfNewSfdrDataSet.toString() + dateAndMonthOfAdditionallyUploadedLksgDataSets
          );
          return uploadOneSfdrDataset(token, companyId, newSfdrDataSet).then((dataMetaInformation) => {
            return { companyId: companyId, dataId: dataMetaInformation.dataId };
          });
        });
      });
    }

    /**
     * Parses the year from a date string
     *
     * @param sfdrDate date to parse
     * @returns the year from the date as string
     */
    function getYearFromSfdrDate(sfdrDate: string): string {
      return new Date(sfdrDate).getFullYear().toString();
    }

    /**
     * Gets an SFDR dataset based on the provided data ID and parses the Fiscal Year End from its date field
     *
     * @param token The API bearer token to use
     * @param dataId The data ID of an SFDR dataset
     * @returns the year from the Fiscal Year End value of the SFDR dataset as string
     */
    async function getFiscalYearEndOfSfdrDataSet(token: string, dataId: string): Promise<string> {
      const response = await new SfdrDataControllerApi(
        new Configuration({ accessToken: token })
      ).getCompanyAssociatedSfdrData(dataId);
      const sfdrData = response.data.data as SfdrData;
      if (sfdrData) {
        const fiscalYearEndAsString = sfdrData.social!.general!.fiscalYearEnd as string;
        return getYearFromSfdrDate(fiscalYearEndAsString);
      } else {
        throw Error(`No Sfdr dataset could be retrieved for the provided dataId ${dataId}`);
      }
    }

    it("Check Sfdr view page for company with one Sfdr data set", () => {
      const preparedFixture = getPreparedFixture("company-with-one-sfdr-data-set", preparedFixtures);
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

    it("Check Sfdr view page for company with two Sfdr data sets", () => {
      const preparedFixture = getPreparedFixture("two-sfdr-data-sets-in-different-years", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const sfdrData = preparedFixture.t;
      const fiscalYearEndAsString = getYearFromSfdrDate(sfdrData.social!.general!.fiscalYearEnd!);
      const numberOfSfdrDataSetsForCompany = 4;

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyAndSfdrDataViaApi(token, companyInformation, sfdrData).then((uploadIds) => {
          let currentChainable: Chainable<UploadIds> = uploadAnotherSfdrDataSetToExistingCompany(uploadIds);
          for (let i = 3; i <= numberOfSfdrDataSetsForCompany; i++) {
            currentChainable = currentChainable.then(uploadAnotherSfdrDataSetToExistingCompany);
          }
          cy.intercept("**/api/data/sfdr/company/*").as("retrieveSfdrData");
          cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/sfdr`);
          cy.wait("@retrieveSfdrData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(() => {
            cy.get("table")
              .find(`tr:contains("Fiscal Year End")`)
              .find(`span`)
              .eq(numberOfSfdrDataSetsForCompany)
              .contains(sfdrData.social!.general!.fiscalYearEnd!);

            cy.get(`span.p-column-title`)
              .eq(numberOfSfdrDataSetsForCompany)
              .should("contain.text", fiscalYearEndAsString);
          });
        });
      });
    });
  }
);
