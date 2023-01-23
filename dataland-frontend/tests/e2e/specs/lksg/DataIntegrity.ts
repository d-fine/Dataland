import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { Configuration, DataTypeEnum, LksgData, LksgDataControllerApi } from "@clients/backend";
import { uploadOneLksgDatasetViaApi, uploadCompanyAndLksgDataViaApi } from "../../utils/LksgUpload";
import { getPreparedFixture, getStoredCompaniesForDataType, UploadIds } from "@e2e/utils/GeneralApiUtils";
import Chainable = Cypress.Chainable;

const dateAndMonthOfAdditionallyUploadedLksgDataSets = "-12-31";
const monthAndDayOfLksgPreparedFixtures = "-01-01";

describeIf(
  "As a user, I expect Lksg data that I upload for a company to be displayed correctly",
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

    async function getReportingYearOfLksgDataSet(dataId: string, token: string): Promise<string> {
      const response = await new LksgDataControllerApi(
        new Configuration({ accessToken: token })
      ).getCompanyAssociatedLksgData(dataId);
      const lksgData = response.data.data;
      if (lksgData) {
        const reportingDateAsString = lksgData.social!.general!.dataDate as string;
        return new Date(reportingDateAsString).getFullYear().toString();
      } else {
        throw Error(`No Lksg dataset could be retrieved for the provided dataId ${dataId}`);
      }
    }

    function getYearFromLksgDate(lksgDate: string): string {
      return lksgDate.split("-")[0];
    }

    function uploadAnotherLksgDataSetToExistingCompany(
      uploadIdsOfExistingCompanyAndLksgDataSet: UploadIds,
      isNewLksgDataSetInSameYear?: boolean
    ): Chainable<UploadIds> {
      const companyId = uploadIdsOfExistingCompanyAndLksgDataSet.companyId;
      const dataId = uploadIdsOfExistingCompanyAndLksgDataSet.dataId;
      return getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        return getReportingYearOfLksgDataSet(dataId, token).then((reportingYearAsString) => {
          let reportingYearOfNewLksgDataSet;
          if (isNewLksgDataSetInSameYear) {
            reportingYearOfNewLksgDataSet = reportingYearAsString;
          } else {
            const reportingYear: number = +reportingYearAsString;
            reportingYearOfNewLksgDataSet = reportingYear + 1;
          }
          const newLksgDataSet = generateLksgData(
            reportingYearOfNewLksgDataSet.toString() + dateAndMonthOfAdditionallyUploadedLksgDataSets
          );
          return uploadOneLksgDatasetViaApi(token, companyId, newLksgDataSet).then((dataMetaInformation) => {
            return { companyId: companyId, dataId: dataMetaInformation.dataId };
          });
        });
      });
    }

    it("Check Lksg view page for company with one Lksg data set", () => {
      const preparedFixture = getPreparedFixture("one-lksg-data-set", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        return uploadCompanyAndLksgDataViaApi(token, companyInformation, lksgData).then((uploadIds) => {
          cy.intercept("**/api/data/lksg/company/*").as("retrieveLksgData");
          cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
          cy.wait("@retrieveLksgData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(() => {
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
            listOfProductionSites.forEach((productionSite) => {
              cy.get("tbody.p-datatable-tbody").find(`span:contains(${productionSite.address!})`);
            });
            cy.get("div.p-dialog").find("span.p-dialog-header-close-icon").click();

            cy.get("em.info-icon").eq(0).trigger("mouseenter", "center");
            cy.get(".p-tooltip").should("be.visible").contains("The date until for which");
            cy.get("em.info-icon").eq(0).trigger("mouseleave");

            cy.get("table.p-datatable-table")
              .find(`span:contains(${lksgData.social!.general!.vatIdentificationNumber!})`)
              .should("exist");

            return getStoredCompaniesForDataType(token, DataTypeEnum.Lksg).then((listOfStoredCompanies) => {
              const nameOfSomeCompanyWithLksgData = listOfStoredCompanies[0].companyInformation.companyName;
              cy.intercept("**/api/companies*").as("searchCompany");
              cy.get("input[id=framework_data_search_bar_standard]")
                .click({ force: true })
                .type(nameOfSomeCompanyWithLksgData);
              cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
                cy.intercept("**/api/data/lksg/company/*").as("retrieveLksgData");
                cy.get("input[id=framework_data_search_bar_standard]").type("{downArrow}").type("{enter}");
                cy.wait("@retrieveLksgData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
                  cy.url().should("include", "/companies/").url().should("include", "/frameworks/");

                  cy.get("table.p-datatable-table")
                    .find(`span:contains(${lksgData.social!.general!.vatIdentificationNumber!})`)
                    .should("not.exist");
                });
              });
            });
          });
        });
      });
    });

    it("Check Lksg view page for company with two Lksg data sets reported for the same year", () => {
      const preparedFixture = getPreparedFixture("two-lksg-data-sets-in-same-year", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyAndLksgDataViaApi(token, companyInformation, lksgData).then((uploadIds) => {
          return uploadAnotherLksgDataSetToExistingCompany(uploadIds, true).then(() => {
            cy.intercept("**/api/data/lksg/company/*").as("retrieveLksgData");
            cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
            cy.wait("@retrieveLksgData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(() => {
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
          });
        });
      });
    });

    it("Check Lksg view page for company with six Lksg data sets reported in different years ", () => {
      const preparedFixture = getPreparedFixture("six-lksg-data-sets-in-different-years", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;
      const reportingYearAsString = getYearFromLksgDate(lksgData.social!.general!.dataDate!);
      const reportingYear: number = +reportingYearAsString;
      const numberOfLksgDataSetsForCompany = 6;

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyAndLksgDataViaApi(token, companyInformation, lksgData).then((uploadIds) => {
          let currentChainable: Chainable<UploadIds> = uploadAnotherLksgDataSetToExistingCompany(uploadIds);
          for (let i = 3; i <= numberOfLksgDataSetsForCompany; i++) {
            currentChainable = currentChainable.then(uploadAnotherLksgDataSetToExistingCompany);
          }
          cy.intercept("**/api/data/lksg/company/*").as("retrieveLksgData");
          cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
          cy.wait("@retrieveLksgData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(() => {
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
          });
        });
      });
    });
  }
);
