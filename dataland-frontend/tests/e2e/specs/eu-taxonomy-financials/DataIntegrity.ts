import { describeIf } from "../../support/TestUtility";
import { createCompanyAndGetId } from "../../utils/CompanyUpload";
import { submitEuTaxonomyFinancialsUploadFormAndGetDataId } from "../../utils/EuTaxonomyFinancialsUpload";

const timeout = 120 * 1000;
describeIf(
  "As a user, I expect that the correct data gets displayed depending on the type of the financial company",
  {
    executionEnvironments: ["development"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
    });

    it("Create a CreditInstitution (combined field submission)", () => {
      createCompanyAndGetId("Meyer Credit Institution").then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
        cy.get("select[name=financialServicesType]").select("Credit Institution");
        cy.get("select[name=attestation]").select("Limited Assurance");
        cy.get('input[name="reportingObligation"][value=Yes]').check();
        cy.get("input[name=tradingPortfolioAndInterbankLoans]").type("0.5");
        submitEuTaxonomyFinancialsUploadFormAndGetDataId();
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy`);
        cy.get('div[name="tradingPortfolioAndOnDemandInterbankLoans"]').should(
          "contain",
          "Trading portfolio & on demand interbank loans"
        );
        cy.get('div[name="onDemandInterbankLoans"]').should("not.exist");
        cy.get('div[name="tradingPortfolio"]').should("not.exist");
      });
    });

    it("Create a CreditInstitution (individual field submission)", () => {
      createCompanyAndGetId("MTM Credit Institution").then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
        cy.get("select[name=financialServicesType]").select("Credit Institution");
        cy.get("select[name=attestation]").select("Limited Assurance");
        cy.get('input[name="reportingObligation"][value=Yes]').check();
        cy.get("input[name=tradingPortfolio]").type("0.25");
        submitEuTaxonomyFinancialsUploadFormAndGetDataId();
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy`);
        cy.get('div[name="tradingPortfolio"]').should("contain", "Trading portfolio").should("contain", "25");
        cy.get('div[name="onDemandInterbankLoans"]')
          .should("contain", "On demand interbank loans")
          .should("contain", "No data has been reported");
        cy.get("body").should("not.contain", "Trading portfolio & on demand interbank loans");
      });
    });

    it("Create an insurance company", () => {
      createCompanyAndGetId("InstantlyHealth AG").then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
        cy.get("select[name=financialServicesType]").select("Insurance or Reinsurance");
        cy.get("select[name=attestation]").select("Limited Assurance");
        cy.get('input[name="reportingObligation"][value=Yes]').check();
        cy.get("input[name=taxonomyEligibleNonLifeInsuranceActivities]").type("0.22");
        submitEuTaxonomyFinancialsUploadFormAndGetDataId();
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy`);
        cy.get('div[name="taxonomyEligibleNonLifeInsuranceActivities"]')
          .should("contain", "Taxonomy-eligible non-life insurance economic activities")
          .should("contain", "22");
        cy.get("body").should("not.contain", "Trading portfolio");
        cy.get("body").should("not.contain", "on demand interbank loans");
      });
    });

    it("Create an Asset Manager", () => {
      createCompanyAndGetId("Magic Super-Stoxx Holding").then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
        cy.get("select[name=financialServicesType]").select("Asset Management");
        cy.get("select[name=attestation]").select("Limited Assurance");
        cy.get('input[name="reportingObligation"][value=Yes]').check();
        cy.get('input[name="taxonomyEligibleActivity"]').type("0.23");
        cy.get('input[name="derivatives"]').type("0.24");
        cy.get('input[name="banksAndIssuers"]').type("0.25");
        cy.get('input[name="investmentNonNfrd"]').type("0.26");
        submitEuTaxonomyFinancialsUploadFormAndGetDataId();
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy`);
        cy.get('div[name="taxonomyEligibleActivity"]')
          .should("contain", "Taxonomy-eligible economic activity")
          .should("contain", "23");
        cy.get('div[name="derivatives"]').should("contain", "Derivatives").should("contain", "24");
        cy.get('div[name="banksAndIssuers"]').should("contain", "Banks and issuers").should("contain", "25");
        cy.get('div[name="investmentNonNfrd"]').should("contain", "Non-NFRD").should("contain", "26");
        cy.get("body").should("not.contain", "Trading portfolio");
        cy.get("body").should("not.contain", "on demand interbank loans");
        cy.get("body").should("not.contain", "Taxonomy-eligible non-life insurance economic activities");
      });
    });
  }
);
