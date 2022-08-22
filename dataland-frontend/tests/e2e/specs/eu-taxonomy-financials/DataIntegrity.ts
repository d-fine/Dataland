import { describeIf } from "../../support/TestUtility";
import { createCompanyAndGetId } from "../../utils/CompanyUpload";
import { submitEuTaxonomyFinancialsUploadFormAndGetDataId } from "../../utils/EuTaxonomyFinancialsUpload";
import {
  CompanyInformation,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
} from "../../../../build/clients/backend/api";

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

    function fillField(divName: string, inputName: string, value?: any) {
      if (value !== undefined) {
        const input = value.toString();
        if (divName === "") {
          cy.get(`input[name="${inputName}"]`).type(input);
        } else {
          cy.get(`div[name="${divName}"]`).find(`input[name="${inputName}"]`).type(input);
        }
      }
    }

    function generateEuTaxonomyUpload(data: EuTaxonomyDataForFinancials) {
      cy.get("select[name=financialServicesTypes]").select(data.financialServicesTypes);
      cy.get("select[name=attestation]").select(data.attestation.toString());
      cy.get(`input[name="reportingObligation"][value=${data.reportingObligation.toString()}]`).check();
      if (data.eligibilityKpis !== undefined) {
        fillEligibilityKpis("CreditInstitution", data.eligibilityKpis.CreditInstitution);
        fillEligibilityKpis("InsuranceOrReinsurance", data.eligibilityKpis.InsuranceOrReinsurance);
        fillEligibilityKpis("AssetManagement", data.eligibilityKpis.AssetManagement);
      }
      if (data.insuranceKpis !== undefined) {
        fillField(
          "",
          "taxonomyEligibleNonLifeInsuranceActivities",
          data.insuranceKpis.taxonomyEligibleNonLifeInsuranceActivities
        );
      }
      if (data.creditInstitutionKpis !== undefined) {
        fillField(
          "",
          "tradingPortfolioAndInterbankLoans",
          data.creditInstitutionKpis.tradingPortfolioAndInterbankLoans
        );
        fillField("", "tradingPortfolio", data.creditInstitutionKpis.tradingPortfolio);
        fillField("", "interbankLoans", data.creditInstitutionKpis.interbankLoans);
      }
    }

    function fillEligibilityKpis(divName: string, data: EuTaxonomyDataForFinancials.eligibilityKpis) {
      fillField(divName, "taxonomyEligibleActivity", data.taxonomyEligibleActivity);
      fillField(divName, "derivatives", data.derivatives);
      fillField(divName, "banksAndIssuers", data.banksAndIssuers);
      fillField(divName, "investmentNonNfrd", data.investmentNonNfrd);
    }

    function uploadDataAndVisitCompanyPage(
      companyInformation: CompanyInformation,
      testData: EuTaxonomyDataForFinancials
    ) {
      createCompanyAndGetId(companyInformation.companyName).then((companyId) => {
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
        generateEuTaxonomyUpload(testData);
        submitEuTaxonomyFinancialsUploadFormAndGetDataId();
        cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials`);
      });
    }

    function formatPercentNumber(value?: any): Number {
      return Math.round((value || 0) * 100 * 100) / 100;
    }

    function checkInsuranceValues(testData: EuTaxonomyDataForFinancials) {
      cy.get('div[name="taxonomyEligibleActivityInsuranceOrReinsurance"]')
        .should("contain", "Taxonomy-eligible economic activity")
        .should(
          "contain",
          formatPercentNumber(testData.eligibilityKpis!.InsuranceOrReinsurance.taxonomyEligibleActivity)
        );
      cy.get('div[name="derivativesInsuranceOrReinsurance"]')
        .should("contain", "Derivatives")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.InsuranceOrReinsurance.derivatives));
      cy.get('div[name="banksAndIssuersInsuranceOrReinsurance"]')
        .should("contain", "Banks and issuers")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.InsuranceOrReinsurance.banksAndIssuers));
      cy.get('div[name="investmentNonNfrdInsuranceOrReinsurance"]')
        .should("contain", "Non-NFRD")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.InsuranceOrReinsurance.investmentNonNfrd));
      cy.get('div[name="taxonomyEligibleNonLifeInsuranceActivities"]')
        .should("contain", "Taxonomy-eligible non-life insurance economic activities")
        .should("contain", formatPercentNumber(testData.insuranceKpis!.taxonomyEligibleNonLifeInsuranceActivities));
    }

    function checkAssetManagementValues(testData: EuTaxonomyDataForFinancials) {
      cy.get('div[name="taxonomyEligibleActivityAssetManagement"]')
        .should("contain", "Taxonomy-eligible economic activity")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.AssetManagement.taxonomyEligibleActivity));
      cy.get('div[name="derivativesAssetManagement"]')
        .should("contain", "Derivatives")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.AssetManagement.derivatives));
      cy.get('div[name="banksAndIssuersAssetManagement"]')
        .should("contain", "Banks and issuers")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.AssetManagement.banksAndIssuers));
      cy.get('div[name="investmentNonNfrdAssetManagement"]')
        .should("contain", "Non-NFRD")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.AssetManagement.investmentNonNfrd));
    }

    function checkGeneralCreditInstitutionValues(testData: EuTaxonomyDataForFinancials) {
      cy.get('div[name="taxonomyEligibleActivityCreditInstitution"]')
        .should("contain", "Taxonomy-eligible economic activity")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.CreditInstitution.taxonomyEligibleActivity));
      cy.get('div[name="derivativesCreditInstitution"]')
        .should("contain", "Derivatives")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.CreditInstitution.derivatives));
      cy.get('div[name="banksAndIssuersCreditInstitution"]')
        .should("contain", "Banks and issuers")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.CreditInstitution.banksAndIssuers));
      cy.get('div[name="investmentNonNfrdCreditInstitution"]')
        .should("contain", "Non-NFRD")
        .should("contain", formatPercentNumber(testData.eligibilityKpis!.CreditInstitution.investmentNonNfrd));
    }

    it("Create a CreditInstitution (combined field submission)", () => {
      const testData = getCompanyAssociatedDataWithSpecificFinancialType([
        EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution,
      ]).filter((it) => {
        return it.t.creditInstitutionKpis!.tradingPortfolioAndInterbankLoans !== undefined;
      })[0];
      uploadDataAndVisitCompanyPage(testData.companyInformation, testData.t);
      checkGeneralCreditInstitutionValues(testData.t);
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
      checkGeneralCreditInstitutionValues(testData.t);
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
      checkAssetManagementValues(testData.t);
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
      checkAssetManagementValues(testData.t);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });
  }
);
