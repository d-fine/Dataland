import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";
import { CompanyInformation, LksgData } from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { getReportingYearOfLksgDataSet, uploadOneLksgDatasetViaApi } from "@e2e/utils/LksgApiUtils";
import { UploadIds } from "@e2e/utils/GeneralApiUtils";
import Chainable = Cypress.Chainable;

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

    function getPreparedFixture(companyName: string): FixtureData<LksgData> {
      const preparedFixture = preparedFixtures.find(
        (fixtureData): boolean => fixtureData.companyInformation.companyName == companyName
      )!;
      if (!preparedFixture) {
        throw new ReferenceError(
          "Variable preparedFixture is undefined because the provided company name could not be found in the prepared fixtures."
        );
      } else {
        return preparedFixture;
      }
    } // TODO this is partially a duplicate in all DataIntegrity tests

    function uploadCompanyAndLksgDataViaApi(
      companyInformation: CompanyInformation,
      testData: LksgData
    ): Chainable<UploadIds> {
      return getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
          (storedCompany) => {
            return uploadOneLksgDatasetViaApi(token, storedCompany.companyId, testData).then((dataMetaInformation) => {
              return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
            });
          }
        );
      });
    } // TODO might be kind of a duplicate for all Dataintegrity tests!

    function uploadAnotherLksgDataSetToExistingCompany(
      companyId: string,
      alreadyExistingLksgDataSetIdForCompany: string,
      isNewLksgDataSetInSameYear: boolean
    ): Chainable<UploadIds> {
      return getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        return getReportingYearOfLksgDataSet(alreadyExistingLksgDataSetIdForCompany, token).then(
          (reportingYearAsString) => {
            let reportingYearOfNewLksgDataSet;
            if (isNewLksgDataSetInSameYear) {
              reportingYearOfNewLksgDataSet = reportingYearAsString;
            } else {
              const reportingYear: number = +reportingYearAsString;
              reportingYearOfNewLksgDataSet = reportingYear - 1;
            }
            const dataSet = generateLksgData(reportingYearOfNewLksgDataSet.toString() + "-12-31");
            return uploadOneLksgDatasetViaApi(token, companyId, dataSet).then((dataMetaInformation) => {
              return { companyId: companyId, dataId: dataMetaInformation.dataId };
            });
          }
        );
      });
    }

    function uploadCompanyWithOneLksgDataSetAndVerifyLksgPageForIt(): void {
      const preparedFixture = getPreparedFixture("one-lksg-data-set");
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      uploadCompanyAndLksgDataViaApi(companyInformation, lksgData).then((uploadIds) => {
        cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");
        cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
        cy.wait("@retrieveLksgData", { timeout: 15 * 1000 }).then(() => {
          cy.get(`h1`).should("contain", companyInformation.companyName);

          cy.get(`span.p-column-title`) // TODO "!"
            .should("contain.text", lksgData.social!.general!.dataDate!.split("-").shift());

          cy.get("table.p-datatable-table")
            .find(`span:contains(${lksgData.social!.general!.dataDate})`) //TODO "!" ?? Is date always there?
            .should("exist");

          cy.get("button.p-row-toggler").eq(0).click();
          cy.get("table.p-datatable-table")
            .find(`span:contains(${lksgData.social!.general!.dataDate})`) //TODO "!" ?? Is date always there?
            .should("not.exist");

          cy.get("button.p-row-toggler").eq(0).click();
          cy.get("table.p-datatable-table")
            .find(`span:contains(${lksgData.social!.general!.dataDate})`) //TODO "!" ?? Is date always there?
            .should("exist");

          cy.get("table.p-datatable-table").find(`span:contains("Employee Under 18")`).should("not.exist");

          cy.get("button.p-row-toggler").eq(1).click();
          cy.get("table.p-datatable-table").find(`span:contains("Employee Under 18")`).should("exist");

          cy.get("table > tbody > tr:nth-child(11) > td.headers-bg.flex").find(`span:contains("Employee Under 18")`).should("exist")
          cy.get("table > tbody > tr:nth-child(11) > td:nth-child(2)").find(`span:contains("No")`).should("exist")
          // TODO we could think about a way to make this more stable,  e.g. looking for "Employee Under 18" and look for a sibling td-element

          cy.get("table.p-datatable-table").find(`a:contains(Show "List Of Production Sites")`).click();

          lksgData.social!.general!.listOfProductionSites!.forEach(  // TODO catch undefined case?
              (productionSite) => cy.get("tbody.p-datatable-tbody").find(`span:contains(${productionSite.address})`)
          )

          cy.get("div.p-dialog").find("span.p-dialog-header-close-icon").click();

          // TODO one tooltip
        });
      });
    }

    it("Check Lksg view page for company with one Lksg data set", () => {
      uploadCompanyWithOneLksgDataSetAndVerifyLksgPageForIt();
    });

    it("Check Lksg view page for company with two Lksg data sets reported for the same year", () => {
      const preparedFixture = getPreparedFixture("two-lksg-data-sets-in-same-year");
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      uploadCompanyAndLksgDataViaApi(companyInformation, lksgData).then((uploadIds) => {
        return uploadAnotherLksgDataSetToExistingCompany(uploadIds.companyId, uploadIds.dataId, true).then(() => {
          cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");
          cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
          cy.wait("@retrieveLksgData", { timeout: 15 * 1000 }).then(() => {

            cy.get(`span.p-column-title`)
                .should("contain.text", lksgData.social!.general!.dataDate!.split("-").shift());


            /* TODO activate this test.   currently it is deactivated because it fails because frontend not like expected
            cy.get("table.p-datatable-table")
                .find(`span:contains("-01-01")`)
                .should("not.exist");

            cy.get("table.p-datatable-table")
                .find(`span:contains("-12-31")`)
                .should("exist");
            */
          });
        });
      });
    });

    it("Check Lksg view page for company with six Lksg data sets reported in different years ", () => {
      const preparedFixture = getPreparedFixture("six-lksg-data-sets-in-different-years");
      const companyInformation = preparedFixture.companyInformation;
      const lksgData = preparedFixture.t;

      uploadCompanyAndLksgDataViaApi(companyInformation, lksgData).then((uploadIds) => {
        const companyId = uploadIds.companyId;
        return uploadAnotherLksgDataSetToExistingCompany(companyId, uploadIds.dataId, false).then((uploadIds) => {
          return uploadAnotherLksgDataSetToExistingCompany(companyId, uploadIds.dataId, false).then((uploadIds) => {
            return uploadAnotherLksgDataSetToExistingCompany(companyId, uploadIds.dataId, false).then((uploadIds) => {
              return uploadAnotherLksgDataSetToExistingCompany(companyId, uploadIds.dataId, false).then((uploadIds) => {
                return uploadAnotherLksgDataSetToExistingCompany(companyId, uploadIds.dataId, false).then(() => {
                  // TODO the "repeat five times" could be possible with less code"
                  cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");
                  cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/lksg`);
                  cy.wait("@retrieveLksgData", { timeout: 15 * 1000 }).then(() => {
                    cy.get(`h1`).should("contain", companyInformation.companyName);

                    //  TODO  check if it looks as expected
                  });
                });
              });
            });
          });
        });
      });
    });
  }
);
