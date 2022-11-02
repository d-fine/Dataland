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
    executionEnvironments: ["developmentLocal", "development", "development_2"],
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

    function getPreparedFixture(name: string): FixtureData<EuTaxonomyDataForNonFinancials> {
      const preparedFixture = preparedFixtures.find((it): boolean => it.companyInformation.companyName == name)!;
      if (!preparedFixture) {
        throw new Error("The provided company name could not be found in the prepared fixtures.");
      } else {
        return preparedFixture;
      }
    }

    function roundNumberToTwoDecimalPlaces(inputNumber: number): number {
      return Math.round(inputNumber * 100) / 100;
    }

    /**
     * This function opens the upload page. Then the uploadFormFiller is executed. It's intended to fill the upload form.
     * Then, the upload button is clicked, and the resulting id is taken. Next, the EU Taxonomy Page is opened.
     * On this page, the euTaxonomyPageVerifier is executed - it's intended to verify contents of the EU Taxonomy page.
     * @param companyId the company ID to upload the data for
     * @param uploadFormFiller the fill method for the upload Form
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
      const preparedFixture = getPreparedFixture("only-eglibile-and-total-numbers");
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
      const preparedFixture = getPreparedFixture("only-eglibile-numbers");
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
