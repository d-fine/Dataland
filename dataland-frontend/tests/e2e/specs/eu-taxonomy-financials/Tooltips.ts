import { describeIf } from "@e2e/support/TestUtility";
import { getStoredCompaniesForDataType } from "@e2e/utils/GeneralApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum, type EuTaxonomyDataForFinancials } from "@clients/backend";
import { reader_name, reader_pw } from "@e2e/utils/Cypress";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";

let companiesWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
    companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
  });
});

/**
 * Retrieves the first company from the fake fixture dataset that has values for both
 * nfrdMandatory and assurance
 * @returns the found dataset
 */
function getCompanyWithNfrdMandatoryAndAssurance(): FixtureData<EuTaxonomyDataForFinancials> {
  return assertDefined(
    companiesWithEuTaxonomyDataForFinancials.find((it) => {
      return it.t?.nfrdMandatory !== undefined && it.t?.assurance !== undefined;
    }),
  );
}

describeIf(
  "As a user, I expect informative tooltips to be shown on the EuTaxonomy result page",
  { executionEnvironments: ["developmentLocal", "ci", "developmentCd"], onlyExecuteOnDatabaseReset: true },
  () => {
    it("Tooltips are present and contain text as expected", function () {
      const nfrdText = "Non financial disclosure directive";
      const assuranceText = "Level of Assurance specifies the confidence level";
      cy.ensureLoggedIn();
      getKeycloakToken(reader_name, reader_pw).then((token) => {
        cy.browserThen(getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyFinancials)).then(
          (storedCompanies) => {
            const testCompany = getCompanyWithNfrdMandatoryAndAssurance();
            const companyId = assertDefined(
              storedCompanies.find((storedCompany) => {
                return storedCompany.companyInformation.companyName === testCompany.companyInformation.companyName;
              })?.companyId,
            );
            cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}/*`).as("retrieveData");
            cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);
            cy.wait("@retrieveData", { timeout: Cypress.env("short_timeout_in_ms") as number });
            cy.get(".p-card-content .text-left strong").contains("NFRD required");
            cy.get('.material-icons[title="NFRD required"]').trigger("mouseenter", "center");
            cy.get(".p-tooltip").should("be.visible").contains(nfrdText);
            cy.get('.material-icons[title="NFRD required"]').trigger("mouseleave");
            cy.get(".p-tooltip").should("not.exist");
            cy.get(".p-card-content .text-left strong").contains("Level of Assurance");
            cy.get('.material-icons[title="Level of Assurance"]').trigger("mouseenter", "center");
            cy.get(".p-tooltip").should("be.visible").contains(assuranceText);
          },
        );
      });
    });
  },
);
