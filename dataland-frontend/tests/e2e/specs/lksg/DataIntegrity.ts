import { describeIf } from "@e2e/support/TestUtility";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { Configuration, DataTypeEnum, LksgData, LksgDataControllerApi, ProductionSite } from "@clients/backend";
import {
  uploadOneLksgDatasetViaApi,
  uploadCompanyAndLksgDataViaApi,
  uploadLksgDataViaForm,
} from "@e2e/utils/LksgUpload";
import { getPreparedFixture, UploadIds } from "@e2e/utils/GeneralApiUtils";
import Chainable = Cypress.Chainable;
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";

const dateAndMonthOfAdditionallyUploadedLksgDataSets = "-12-31";
const monthAndDayOfLksgPreparedFixtures = "-01-01";

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

    let preparedFixtures: Array<FixtureData<LksgData>>;

    before(function () {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
      });
    });

    /**
     * Gets an LkSG dataset based on the provided data ID and parses the year from its date field
     *
     * @param token The API bearer token to use
     * @param dataId The data ID of an LkSG dataset
     * @returns the year from the date value of the LkSG dataset as string
     */
    async function getReportingYearOfLksgDataSet(token: string, dataId: string): Promise<string> {
      const response = await new LksgDataControllerApi(
        new Configuration({ accessToken: token })
      ).getCompanyAssociatedLksgData(dataId);
      const lksgData = response.data.data as LksgData;
      if (lksgData) {
        const reportingDateAsString = lksgData.social!.general!.dataDate as string;
        return getYearFromLksgDate(reportingDateAsString);
      } else {
        throw Error(`No Lksg dataset could be retrieved for the provided dataId ${dataId}`);
      }
    }

    /**
     * Parses the year from a date string
     *
     * @param lksgDate date to parse
     * @returns the year from the date as string
     */
    function getYearFromLksgDate(lksgDate: string): string {
      return new Date(lksgDate).getFullYear().toString();
    }

    /**
     * Uploads an LkSG dataset to an existing company that already has at least one LkSG dataset uploaded for.
     *
     * @param uploadIdsOfExistingCompanyAndLksgDataSet contains the companyId of the existing company, and the data ID
     * of one already existing LkSG dataset for that company
     * @param isNewLksgDataSetInSameYear is a flag that decides if the new LkSG dataset should have a date which has
     * the same year as the already existing LkSG dataset
     * @returns an object which contains the companyId of the already existing company and the dataId of the newly
     * uploaded LkSG dataset
     */
    function uploadAnotherLksgDataSetToExistingCompany(
      uploadIdsOfExistingCompanyAndLksgDataSet: UploadIds,
      isNewLksgDataSetInSameYear?: boolean
    ): Chainable<UploadIds> {
      const companyId = uploadIdsOfExistingCompanyAndLksgDataSet.companyId;
      const dataId = uploadIdsOfExistingCompanyAndLksgDataSet.dataId;
      return getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return getReportingYearOfLksgDataSet(token, dataId).then((reportingYearAsString) => {
          let reportingYearOfNewLksgDataSet;
          if (isNewLksgDataSetInSameYear) {
            reportingYearOfNewLksgDataSet = reportingYearAsString;
          } else {
            const reportingYear: number = +reportingYearAsString;
            reportingYearOfNewLksgDataSet = reportingYear + 1;
          }
          const reportingDateOfNewLksgDataSet =
            reportingYearOfNewLksgDataSet.toString() + dateAndMonthOfAdditionallyUploadedLksgDataSets;
          const newLksgDataSet = generateLksgData(reportingDateOfNewLksgDataSet);
          return uploadOneLksgDatasetViaApi(token, companyId, reportingDateOfNewLksgDataSet, newLksgDataSet).then(
            (dataMetaInformation) => {
              return { companyId: companyId, dataId: dataMetaInformation.dataId };
            }
          );
        });
      });
    }

    it("Check Lksg view page for company with one Lksg data set", () => {
      const preparedFixture = getPreparedFixture("one-lksg-data-set", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;
      const reportingPeriod = preparedFixture.reportingPeriod;

      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        return uploadCompanyAndLksgDataViaApi(token, companyInformation, lksgData, reportingPeriod).then(
          (uploadIds) => {
            cy.intercept(`**/api/data/${DataTypeEnum.Lksg}/companies/*`).as("retrieveLksgData");
            cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/${DataTypeEnum.Lksg}`);
            cy.wait("@retrieveLksgData", { timeout: Cypress.env("medium_timeout_in_ms") as number });
            cy.get(`h1`).should("contain", companyInformation.companyName);

            cy.get(`span.p-column-title`).should(
              "contain.text",
              getYearFromLksgDate(lksgData.social!.general!.dataDate!)
            );

            cy.get("table.p-datatable-table")
              .find(`span:contains(${lksgData.social!.general!.dataDate!})`)
              .should("exist");

            cy.get("button.p-row-toggler").eq(0).click();
            cy.get("table.p-datatable-table")
              .find(`span:contains(${lksgData.social!.general!.dataDate!})`)
              .should("not.exist");

            cy.get("button.p-row-toggler").eq(0).click();
            cy.get("table.p-datatable-table")
              .find(`span:contains(${lksgData.social!.general!.dataDate!})`)
              .should("exist");

            cy.get("table.p-datatable-table").find(`span:contains("Employee Under 18")`).should("not.exist");

            cy.get("button.p-row-toggler").eq(1).click();
            cy.get("table.p-datatable-table").find(`span:contains("Employee Under 18")`).should("exist");

            cy.get("table")
              .find(`tr:contains("Employee Under 18 Apprentices")`)
              .find(`span:contains("No")`)
              .should("exist");

            cy.get("table.p-datatable-table").find(`a:contains(Show "List Of Production Sites")`).click();
            const listOfProductionSites = lksgData.social!.general!.listOfProductionSites!;
            if (listOfProductionSites.length < 2) {
              throw Error("This test only accepts an Lksg-dataset which has at least two production sites.");
            }
            listOfProductionSites.forEach((productionSite: ProductionSite) => {
              if (productionSite.streetAndHouseNumber) {
                cy.get("tbody.p-datatable-tbody").find(`span:contains(${productionSite.streetAndHouseNumber})`);
              }
            });
            cy.get("div.p-dialog-mask").click({ force: true });

            cy.get("em.info-icon").eq(0).trigger("mouseenter", "center");
            cy.get(".p-tooltip").should("be.visible").contains("The date until for which");
            cy.get("em.info-icon").eq(0).trigger("mouseleave");

            cy.get("table.p-datatable-table")
              .find(`span:contains(${lksgData.social!.general!.vatIdentificationNumber!})`)
              .should("exist");

            const someRandomCompanyName = "some-random-company-name-sj48jg3" + Date.now().toString() + "388fj";
            return uploadCompanyAndLksgDataViaApi(
              token,
              generateDummyCompanyInformation(someRandomCompanyName),
              generateLksgData(),
              getRandomReportingPeriod()
            ).then(() => {
              cy.intercept("**/api/companies*").as("searchCompany");
              cy.intercept(`**/api/data/${DataTypeEnum.Lksg}/companies/*`).as("retrieveLksgData");
              cy.get("input[id=framework_data_search_bar_standard]")
                .click({ force: true })
                .type(someRandomCompanyName)
                .wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number });
              cy.get(".p-autocomplete-item").contains(someRandomCompanyName).click({ force: true });
              cy.wait("@retrieveLksgData", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.url().should("include", "/companies/").url().should("include", "/frameworks/");
              cy.get("table.p-datatable-table")
                .find(`span:contains(${lksgData.social!.general!.vatIdentificationNumber!})`)
                .should("not.exist");
            });
          }
        );
      });
    });

    it("Check Lksg view page for company with two Lksg data sets reported for the same year", () => {
      const preparedFixture = getPreparedFixture("two-lksg-data-sets-in-same-year", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;
      const reportingPeriod = preparedFixture.reportingPeriod;

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyAndLksgDataViaApi(token, companyInformation, lksgData, reportingPeriod).then(
          (uploadIds) => {
            return uploadAnotherLksgDataSetToExistingCompany(uploadIds, true).then(() => {
              cy.intercept(`**/api/data/${DataTypeEnum.Lksg}/companies/*`).as("retrieveLksgData");
              cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/${DataTypeEnum.Lksg}`);
              cy.wait("@retrieveLksgData", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("table")
                .find(`tr:contains("Data Date")`)
                .find(`span`)
                .eq(1)
                .contains(dateAndMonthOfAdditionallyUploadedLksgDataSets);

              cy.get("table")
                .find(`tr:contains("Data Date")`)
                .find(`span`)
                .eq(2)
                .contains(monthAndDayOfLksgPreparedFixtures);
            });
          }
        );
      });
    });

    it("Check Lksg view page for company with six Lksg data sets reported in different years ", () => {
      const preparedFixture = getPreparedFixture("six-lksg-data-sets-in-different-years", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;
      const reportingPeriod = preparedFixture.reportingPeriod;
      const reportingYearAsString = getYearFromLksgDate(lksgData.social!.general!.dataDate!);
      const reportingYear: number = +reportingYearAsString;
      const numberOfLksgDataSetsForCompany = 6;

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyAndLksgDataViaApi(token, companyInformation, lksgData, reportingPeriod).then(
          (uploadIds) => {
            let currentChainable: Chainable<UploadIds> = uploadAnotherLksgDataSetToExistingCompany(uploadIds);
            for (let i = 3; i <= numberOfLksgDataSetsForCompany; i++) {
              currentChainable = currentChainable.then(uploadAnotherLksgDataSetToExistingCompany);
            }
            cy.intercept(`**/api/data/${DataTypeEnum.Lksg}/companies/*`).as("retrieveLksgData");
            cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/${DataTypeEnum.Lksg}`);
            cy.wait("@retrieveLksgData", { timeout: Cypress.env("medium_timeout_in_ms") as number });
            cy.get("table")
              .find(`tr:contains("Data Date")`)
              .find(`span`)
              .eq(numberOfLksgDataSetsForCompany)
              .contains(lksgData.social!.general!.dataDate!);

            cy.get(`span.p-column-title`)
              .eq(numberOfLksgDataSetsForCompany)
              .should("contain.text", reportingYearAsString);

            const latestLksgDataSetReportingYear = reportingYear + numberOfLksgDataSetsForCompany - 1;
            for (let indexOfColumn = 1; indexOfColumn < numberOfLksgDataSetsForCompany; indexOfColumn++) {
              cy.get(`span.p-column-title`)
                .eq(indexOfColumn)
                .should("contain.text", (latestLksgDataSetReportingYear + 1 - indexOfColumn).toString());
            }
          }
        );
      });
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
  }
);
