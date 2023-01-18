import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { LksgData } from "@clients/backend";
import {
  getReportingYearOfLksgDataSet,
  uploadOneLksgDatasetViaApi,
  getPreparedLksgFixture,
  uploadCompanyAndLksgDataViaApi,
} from "@e2e/utils/LksgApiUtils";
import { UploadIds } from "@e2e/utils/GeneralApiUtils";
import Chainable = Cypress.Chainable;
import { MONTH_AND_DAY_OF_LKSG_PREPARED_FIXTURES } from "@e2e/utils/Constants";

const dateAndMonthOfAdditionallyUploadedLksgDataSets = "-12-31";

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
      const preparedFixture = getPreparedLksgFixture("one-lksg-data-set", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      uploadCompanyAndLksgDataViaApi(companyInformation, lksgData).then((uploadIds) => {
        cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");
        cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
        cy.wait("@retrieveLksgData", { timeout: 15 * 1000 }).then(() => {
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
        });
      });
    });

    it("Check Lksg view page for company with two Lksg data sets reported for the same year", () => {
      const preparedFixture = getPreparedLksgFixture("two-lksg-data-sets-in-same-year", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      uploadCompanyAndLksgDataViaApi(companyInformation, lksgData).then((uploadIds) => {
        return uploadAnotherLksgDataSetToExistingCompany(uploadIds, true).then(() => {
          cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");

          cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
          cy.wait("@retrieveLksgData", { timeout: 15 * 1000 }).then(() => {
            cy.get("table")
              .find(`tr:contains("Data Date")`)
              .find(`span`)
              .eq(1)
              .contains(dateAndMonthOfAdditionallyUploadedLksgDataSets);

            cy.get("table")
              .find(`tr:contains("Data Date")`)
              .find(`span`)
              .eq(2)
              .contains(MONTH_AND_DAY_OF_LKSG_PREPARED_FIXTURES);
          });
        });
      });
    });

    it("Check Lksg view page for company with six Lksg data sets reported in different years ", () => {
      const preparedFixture = getPreparedLksgFixture("six-lksg-data-sets-in-different-years", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      uploadCompanyAndLksgDataViaApi(companyInformation, lksgData).then((uploadIds) => {
        const reportingYearAsString = getYearFromLksgDate(lksgData.social!.general!.dataDate!);
        const reportingYear: number = +reportingYearAsString;
        const totalNumberOfLksgDataSetsForCompany = 6;
        return (
          uploadAnotherLksgDataSetToExistingCompany(uploadIds)
            .then(uploadAnotherLksgDataSetToExistingCompany)
            .then(uploadAnotherLksgDataSetToExistingCompany)
            .then(uploadAnotherLksgDataSetToExistingCompany)
            .then(uploadAnotherLksgDataSetToExistingCompany)
            // TODO does someone have an idea how this can be done in one line like "queue this calllb
            .then(() => {
              cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");
              cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
              cy.wait("@retrieveLksgData", { timeout: 15 * 1000 }).then(() => {
                cy.get("table")
                  .find(`tr:contains("Data Date")`)
                  .find(`span`)
                  .eq(1)
                  .contains(lksgData.social!.general!.dataDate!);

                cy.get(`span.p-column-title`).eq(1).should("contain.text", reportingYearAsString);

                for (let indexOfColumn = 2; indexOfColumn <= totalNumberOfLksgDataSetsForCompany; indexOfColumn++) {
                  cy.get(`span.p-column-title`)
                    .eq(indexOfColumn)
                    .should("contain.text", (reportingYear - indexOfColumn + 1).toString());
                }
              });
            })
        );
      });
    });
  }
);
