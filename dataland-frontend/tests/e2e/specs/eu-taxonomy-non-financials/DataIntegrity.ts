import { describeIf } from "@e2e/support/TestUtility";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { DataTypeEnum, EuTaxonomyDataForNonFinancials } from "@clients/backend";
import {
  uploadEuTaxonomyDataForNonFinancialsViaForm,
  uploadOneEuTaxonomyNonFinancialsDatasetViaApi,
} from "@e2e/utils/EuTaxonomyNonFinancialsUpload";

describeIf(
  "As a user, I expect Eu Taxonomy Data for non-financials that I upload for a company to be displayed correctly",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
      });
    });

    /**
     * Rounds a number to two decimal places.
     * @param inputNumber The number which should be rounded
     * @returns the rounded number
     */
    function roundNumberToTwoDecimalPlaces(inputNumber: number): number {
      return Math.round(inputNumber * 100) / 100;
    }

    it("Create a EU Taxonomy Dataset via Api with total(€) and eligible(%) numbers", () => {
      const preparedFixture = getPreparedFixture("only-eligible-and-total-numbers", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndVerifyEuTaxonomyPage(preparedFixture, () => {
        cy.get("body").should("contain", "Eligible Revenue").should("contain", `Out of total of`);
        cy.get("body")
          .should("contain", "Eligible Revenue")
          .should(
            "contain",
            `${roundNumberToTwoDecimalPlaces(100 * preparedFixture.t.revenue!.eligiblePercentage!.value!)}%`
          );
        cy.get(".font-medium.text-3xl").should("contain", "€");
      });
    });

    it("Create a EU Taxonomy Dataset via Api with only eligible(%) numbers", () => {
      const preparedFixture = getPreparedFixture("only-eligible-numbers", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndVerifyEuTaxonomyPage(preparedFixture, () => {
        cy.get("body")
          .should("contain", "Eligible OpEx")
          .should(
            "contain",
            `${roundNumberToTwoDecimalPlaces(100 * preparedFixture.t.revenue!.eligiblePercentage!.value!)}%`
          );
        cy.get("body").should("contain", "Eligible Revenue").should("not.contain", `Out of total of`);
        cy.get(".font-medium.text-3xl").should("not.contain", "€");
      });
    });

    it("Create a EU Taxonomy Dataset via Api without referenced reports and ensure that the reports banner is not displayed", () => {
      const preparedFixture = getPreparedFixture("company_without_reports", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndVerifyEuTaxonomyPage(preparedFixture, () => {
        cy.get("div[data-test='reportsBanner']").should("not.exist");
      });
    });

    it("Upload EU Taxonomy Dataset via form, check that redirect to MyDatasets works and assure that it can be viewed on the framework ", () => {
      getKeycloakToken(uploader_name, uploader_pw).then((token) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation("All fields filled")).then(
          (storedCompany) => {
            uploadEuTaxonomyDataForNonFinancialsViaForm(storedCompany.companyId)
              .url()
              .should("eq", getBaseUrl() + "/datasets");
            cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/*`).as("retrieveFullTaxonomyData");
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
            );
            cy.wait("@retrieveFullTaxonomyData", { timeout: Cypress.env("long_timeout_in_ms") as number }).then(() => {
              cy.get("h1[class='mb-0']").contains("All fields filled");
              cy.get("body").should("contain", "Eligible Revenue").should("contain", "%");
              cy.get("body").should("contain", "Aligned Revenue").should("contain", "%");
              cy.get("body").should("contain", "Eligible CapEx").should("contain", "%");
              cy.get("body").should("contain", "Aligned CapEx").should("contain", "%");
              cy.get("body").should("contain", "Eligible OpEx").should("contain", "%");
              cy.get("body").should("contain", "Aligned OpEx").should("contain", "%");
            });
          }
        );
      });
    });

    it(
      "Upload EU Taxonomy Dataset via form with no values for revenue and assure that it can be viewed on the framework " +
        "data view page with an appropriate message shown for the missing revenue data",
      () => {
        const companyName = "Missing field company";
        const missingDataMessage = "No data has been reported";
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((storedCompany) => {
            uploadEuTaxonomyDataForNonFinancialsViaForm(storedCompany.companyId, true);
            cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/*`).as("retrieveTaxonomyData");
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
            );
            cy.wait("@retrieveTaxonomyData", { timeout: Cypress.env("long_timeout_in_ms") as number }).then(() => {
              cy.get("h1[class='mb-0']").contains(companyName);
              cy.get("body").should("contain", "Eligible Revenue").should("contain", missingDataMessage);
            });
          });
        });
      }
    );
  }
);

describeIf(
  "As a user, I expect Eu Taxonomy Data for non-financials to have a reports banner from where I can download the referenced reports",
  {
    executionEnvironments: ["developmentLocal", "ci"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
      });
    });

    it("Create a EU Taxonomy Dataset via Api and ensure the reports banner exists and documents can be downloaded", () => {
      const preparedFixture = getPreparedFixture("only-eligible-and-total-numbers", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndVerifyEuTaxonomyPage(preparedFixture, () => {
        cy.get("div[data-test='reportsBanner']").should("exist");
        const expectedPathToDownloadedReport = Cypress.config("downloadsFolder") + "/StandardWordExport.pdf";
        cy.readFile(expectedPathToDownloadedReport).should("not.exist");
        const downloadLinkSelector = "span[data-test='Report-Download']";
        cy.get(downloadLinkSelector)
          .click({ multiple: true })
          .then(() => {
            cy.readFile("../testing/data/documents/StandardWordExport.pdf", "binary", {
              timeout: Cypress.env("medium_timeout_in_ms") as number,
            }).then((expectedPdfBinary) => {
              cy.task("calculateHash", expectedPdfBinary).then((expectedPdfHash) => {
                cy.readFile(expectedPathToDownloadedReport, "binary", {
                  timeout: Cypress.env("medium_timeout_in_ms") as number,
                }).then((receivedPdfHash) => {
                  cy.task("calculateHash", receivedPdfHash).should("eq", expectedPdfHash);
                });
                cy.task("deleteFolder", Cypress.config("downloadsFolder"));
              });
            });
          });
      });
    });
  }
);

/**
 * This function uploads fixture data of one company and the associated data via API. Afterwards the result is
 * checked using the provided verifier.
 * @param fixtureData the company and its associated data
 * @param euTaxonomyPageVerifier the verify method for the EU Taxonomy Page
 */
function uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndVerifyEuTaxonomyPage(
  fixtureData: FixtureData<EuTaxonomyDataForNonFinancials>,
  euTaxonomyPageVerifier: () => void
): void {
  getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
    return uploadCompanyViaApi(token, generateDummyCompanyInformation(fixtureData.companyInformation.companyName)).then(
      (storedCompany) => {
        return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
          token,
          storedCompany.companyId,
          fixtureData.reportingPeriod,
          fixtureData.t
        ).then(() => {
          cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/*`).as("retrieveTaxonomyData");
          cy.visitAndCheckAppMount(
            `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
          );
          cy.wait("@retrieveTaxonomyData", { timeout: Cypress.env("long_timeout_in_ms") as number }).then(() => {
            euTaxonomyPageVerifier();
          });
        });
      }
    );
  });
}
