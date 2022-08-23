import { describeIf } from "../../support/TestUtility";
import { createCompanyAndGetId } from "../../utils/CompanyUpload";
import { submitEuTaxonomyFinancialsUploadForm, generateEuTaxonomyUpload } from "../../utils/EuTaxonomyFinancialsUpload";
import {
  CompanyInformation,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  EligibilityKpis,
} from "../../../../build/clients/backend/org/dataland/datalandfrontend/openApiClient/model";

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

    let element: Array<{
      companyInformation: CompanyInformation;
      t: EuTaxonomyDataForFinancials;
    }>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (companies) {
        element = companies;
      });
    });

    function getCompanyAssociatedDataWithSpecificFinancialType(
      serviceTypes: Array<EuTaxonomyDataForFinancialsFinancialServicesTypesEnum>
    ) {
      return element.filter((it) => {
        return (
          serviceTypes.every((serviceType) => it.t.financialServicesTypes.includes(serviceType)) &&
          serviceTypes.length === it.t.financialServicesTypes.length
        );
      });
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

    function formatPercentNumber(value?: any): number {
      return Math.round((value || 0) * 100 * 100) / 100;
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
      const testData = getCompanyAssociatedDataWithSpecificFinancialType([
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution,
      ]).filter((it) => {
        return it.t.creditInstitutionKpis!.tradingPortfolioAndInterbankLoans !== undefined;
      })[0];
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkCommonFields("CreditInstitution", testData.t.eligibilityKpis!.CreditInstitution);
      cy.get('div[name="tradingPortfolioAndOnDemandInterbankLoans"]')
        .should("contain", "Trading portfolio & on demand interbank loans")
        .should("contain", formatPercentNumber(testData.t.creditInstitutionKpis!.tradingPortfolioAndInterbankLoans));
      cy.get("body").should("not.contain", /^Trading portfolio$/);
      cy.get("body").should("not.contain", "On demand interbank loans");
    });

    it("Create a CreditInstitution (individual field submission)", () => {
      const testData = getCompanyAssociatedDataWithSpecificFinancialType([
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution,
      ]).filter((it) => {
        return it.t.creditInstitutionKpis!.tradingPortfolioAndInterbankLoans === undefined;
      })[0];
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
      const testData = getCompanyAssociatedDataWithSpecificFinancialType([
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance,
      ])[0];
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkInsuranceValues(testData.t);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });

    it("Create an Asset Manager", () => {
      const testData = getCompanyAssociatedDataWithSpecificFinancialType([
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement,
      ])[0];
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkCommonFields("AssetManagement", testData.t.eligibilityKpis!.AssetManagement);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
      cy.get("body").should("not.contain", "Taxonomy-eligible non-life insurance economic activities");
    });

    it("Create a Company that is Asset Manager and Insurance", () => {
      const testData = getCompanyAssociatedDataWithSpecificFinancialType([
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement,
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance,
      ])[0];
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkInsuranceValues(testData.t);
      checkCommonFields("AssetManagement", testData.t.eligibilityKpis!.AssetManagement);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });
  }
);
