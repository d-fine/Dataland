import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { getPreparedFixture } from "@e2e/utils/GeneralApiUtils";

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
     *
     * @param inputNumber The number which should be rounded
     * @returns the rounded number
     */
    function roundNumberToTwoDecimalPlaces(inputNumber: number): number {
      return Math.round(inputNumber * 100) / 100;
    }

    /**
     * This function uploads fixture data of one company and the associated data via API. Afterwards the result is
     * checked using the provided verifier.
     *
     * @param fixtureData the company and its associated data
     * @param euTaxonomyPageVerifier the verify method for the EU Taxonomy Page
     */
    function uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndVerifyEuTaxonomyPage(
      fixtureData: FixtureData<EuTaxonomyDataForNonFinancials>,
      euTaxonomyPageVerifier: () => void
    ): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(
          token,
          generateDummyCompanyInformation(fixtureData.companyInformation.companyName)
        ).then((storedCompany) => {
          return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(token, storedCompany.companyId, fixtureData.t).then(
            () => {
              cy.intercept("**/api/data/eutaxonomy-non-financials/*").as("retrieveTaxonomyData");
              cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/eutaxonomy-non-financials`);
              cy.wait("@retrieveTaxonomyData", { timeout: Cypress.env("long_timeout_in_ms") as number }).then(() => {
                euTaxonomyPageVerifier();
              });
            }
          );
        });
      });
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
  }
);
