import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";

const timeout = 120 * 1000;
describeIf(
  "As a user, I expect data that I upload for a company to be displayed correctly",
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
     * Retrieves the prepared fixture identified by the provided company name
     *
     * @param name the name of the prepared fixture company
     * @returns the found dataset from the prepared fixtures
     */
    function getPreparedFixture(name: string): FixtureData<EuTaxonomyDataForNonFinancials> {
      const preparedFixture = preparedFixtures.find((it): boolean => it.companyInformation.companyName == name)!;
      if (!preparedFixture) {
        throw new ReferenceError(
          "Variable preparedFixture is undefined because the provided company name could not be found in the prepared fixtures."
        );
      } else {
        return preparedFixture;
      }
    }

    /**
     * Rounds the input number to 2 decimal places
     *
     * @param inputNumber the number to round
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
              cy.wait("@retrieveTaxonomyData", { timeout: timeout }).then(() => {
                euTaxonomyPageVerifier();
              });
            }
          );
        });
      });
    }

    it("Create a EU Taxonomy Dataset via upload form with total(€) and eligible(%) numbers", () => {
      const preparedFixture = getPreparedFixture("only-eligible-and-total-numbers");
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

    it("Create a EU Taxonomy Dataset via upload form with only eligible(%) numbers", () => {
      const preparedFixture = getPreparedFixture("only-eligible-numbers");
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
