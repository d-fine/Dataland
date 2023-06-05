import { DataTypeEnum, EuTaxonomyDataForFinancials } from "@clients/backend";
import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation } from "@e2e/utils/CompanyUpload";
import { admin_name, admin_pw  } from "@e2e/utils/Cypress";
import {
  fillEligibilityKpis,
  fillEuTaxonomyForFinancialsRequiredFields,
  fillField,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadCompanyViaApiAndEuTaxonomyDataViaForm } from "@e2e/utils/GeneralApiUtils";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { CyHttpMessages } from "cypress/types/net-stubbing";

describeIf(
  "As a user, I expect to be able to add a new dataset and see it as pending",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    let testData: FixtureData<EuTaxonomyDataForFinancials>;
    const testCompany = generateDummyCompanyInformation("company-for-testing-kpi-sections");

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    it("Check wether newly added dataset has Pending status", () => {
      uploadCompanyViaApiAndEuTaxonomyDataViaForm<EuTaxonomyDataForFinancials>(
        DataTypeEnum.EutaxonomyFinancials,
        testCompany,
        testData.t,
        fillEuTaxonomyForm,
        submissionDataIntercept,
        () => undefined
      );
    });
  }
);

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 * @param data the data to fill the form with
 */
function fillEuTaxonomyForm(data: EuTaxonomyDataForFinancials): void {
  fillEuTaxonomyForFinancialsRequiredFields(data);

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .first()
    .click({ force: true });

  cy.get('[data-test="addKpisButton"]').click({ force: true });

  fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
  fillField(
    "creditInstitutionKpis",
    "tradingPortfolioAndInterbankLoans",
    data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoans
  );
  fillField("creditInstitutionKpis", "tradingPortfolio", data.creditInstitutionKpis?.tradingPortfolio);
  fillField("creditInstitutionKpis", "interbankLoans", data.creditInstitutionKpis?.interbankLoans);
  fillField("creditInstitutionKpis", "greenAssetRatio", data.creditInstitutionKpis?.greenAssetRatio);
}

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 * @param request the intercepted request
 */
function submissionDataIntercept(request: CyHttpMessages.IncomingHttpRequest): void {
  console.log(request);
}
