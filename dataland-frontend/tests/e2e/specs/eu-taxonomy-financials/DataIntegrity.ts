import { describeIf } from "@e2e/support/TestUtility";
import { createCompanyAndGetId } from "@e2e/utils/CompanyUpload";
import { submitEuTaxonomyFinancialsUploadForm, generateEuTaxonomyUpload } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import {
  CompanyInformation,
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  DataPointBigDecimal,
} from "@clients/backend";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";

describeIf(
  "As a user, I expect that the correct data gets displayed depending on the type of the financial company",
  {
    executionEnvironments: ["developmentLocal", "development"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
    });

    let preparedFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (companies) {
        preparedFixtures = companies;
      });
    });

    function getPreparedFixture(name: String): FixtureData<EuTaxonomyDataForFinancials> {
      return preparedFixtures.find((it) => it.companyInformation.companyName == name)!!;
    }

    function uploadDataAndVisitCompanyPage(
      companyInformation: CompanyInformation,
      testData: EuTaxonomyDataForFinancials
    ) {
      createCompanyAndGetId(companyInformation.companyName).then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
        generateEuTaxonomyUpload(testData);
        submitEuTaxonomyFinancialsUploadForm();
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials`);
      });
    }

    function formatPercentNumber(value?: DataPointBigDecimal): string {
      if (value === undefined || value === null || value.value === undefined || value.value === null)
        return "No data has been reported";
      return (Math.round(value.value * 100 * 100) / 100).toString();
    }

    function checkCommonFields(type: string, data: EligibilityKpis) {
      cy.get(`div[name="taxonomyEligibleActivity${type}"]`)
        .should("contain", "Taxonomy-eligible economic activity")
        .should("contain", formatPercentNumber(data.taxonomyEligibleActivity));
      cy.get(`div[name="derivatives${type}"]`)
        .should("contain", "Derivatives")
        .should("contain", formatPercentNumber(data.derivatives));
      cy.get(`div[name="banksAndIssuers${type}"]`)
        .should("contain", "Banks and issuers")
        .should("contain", formatPercentNumber(data.banksAndIssuers));
      cy.get(`div[name="investmentNonNfrd${type}"]`)
        .should("contain", "Non-NFRD")
        .should("contain", formatPercentNumber(data.investmentNonNfrd));
    }

    function checkInsuranceValues(testData: EuTaxonomyDataForFinancials) {
      checkCommonFields("InsuranceOrReinsurance", testData.eligibilityKpis!.InsuranceOrReinsurance);
      cy.get('div[name="taxonomyEligibleNonLifeInsuranceActivities"]')
        .should("contain", "Taxonomy-eligible non-life insurance economic activities")
        .should("contain", formatPercentNumber(testData.insuranceKpis!.taxonomyEligibleNonLifeInsuranceActivities));
    }

    it("Create a CreditInstitution (combined field submission)", () => {
      const testData = getPreparedFixture("credit-institution-single-field-submission");
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkCommonFields("CreditInstitution", testData.t.eligibilityKpis!.CreditInstitution);
      cy.get('div[name="tradingPortfolioAndOnDemandInterbankLoans"]')
        .should("contain", "Trading portfolio & on demand interbank loans")
        .should("contain", formatPercentNumber(testData.t.creditInstitutionKpis!.tradingPortfolioAndInterbankLoans));
      cy.get("body").should("not.contain", /^Trading portfolio$/);
      cy.get("body").should("not.contain", "On demand interbank loans");
    });

    it("Create a CreditInstitution (individual field submission)", () => {
      const testData = getPreparedFixture("credit-institution-dual-field-submission");
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkCommonFields("CreditInstitution", testData.t.eligibilityKpis!.CreditInstitution);
      cy.get('div[name="tradingPortfolio"]')
        .should("contain", "Trading portfolio")
        .should("contain", formatPercentNumber(testData.t.creditInstitutionKpis!.tradingPortfolio));
      cy.get('div[name="onDemandInterbankLoans"]')
        .should("contain", "On demand interbank loans")
        .should("contain", formatPercentNumber(testData.t.creditInstitutionKpis!.interbankLoans));
      cy.get("body").should("not.contain", "Trading portfolio & on demand interbank loans");
    });

    it("Create an insurance company", () => {
      const testData = getPreparedFixture("insurance-company");
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkInsuranceValues(testData.t);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });

    it("Create an Asset Manager", () => {
      const testData = getPreparedFixture("asset-management-company");
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkCommonFields("AssetManagement", testData.t.eligibilityKpis!.AssetManagement);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
      cy.get("body").should("not.contain", "Taxonomy-eligible non-life insurance economic activities");
    });

    it("Create a Company that is Asset Manager and Insurance", () => {
      const testData = getPreparedFixture("asset-management-insurance-company");
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkInsuranceValues(testData.t);
      checkCommonFields("AssetManagement", testData.t.eligibilityKpis!.AssetManagement);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });
  }
);
