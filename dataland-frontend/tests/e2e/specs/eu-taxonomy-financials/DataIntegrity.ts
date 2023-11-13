import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
  type CompanyInformation,
  type ExtendedDataPointBigDecimal,
  DataTypeEnum,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
} from "@clients/backend";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { admin_name, admin_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";

describeIf(
  "As a user, I expect that the correct data gets displayed depending on the type of the financial company",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
      });
    });

    /**
     * Uploads the provided company and dataset to Dataland via the API and navigates to the page of the uploaded
     * dataset
     * @param companyInformation the company information to upload
     * @param testData the dataset to upload
     * @param reportingPeriod the period associated to the EU Taxonomy data for Financials to upload
     */
    function uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
      companyInformation: CompanyInformation,
      testData: EuTaxonomyDataForFinancials,
      reportingPeriod: string,
    ): void {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
          (storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.EutaxonomyFinancials,
              token,
              storedCompany.companyId,
              reportingPeriod,
              testData,
            ).then(() => {
              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`,
              );
            });
          },
        );
      });
    }

    /**
     * Formats a datapoint as a percentage value rounded to a precision of 0.01%.
     * Returns "No data has been reported" if the datapoint contains no value
     * @param value the value of the datapoint to format as a percentage
     * @returns the formatted string
     */
    function formatPercentNumber(value?: ExtendedDataPointBigDecimal | null): string {
      if (value === undefined || value === null || value.value === undefined || value.value === null)
        return "No data has been reported";
      return (Math.round(value.value * 100) / 100).toString();
    }

    /**
     * Verifies that the frontend correctly displays eligibilityKPIs for a specific company type
     * @param financialCompanyType the company type to check
     * @param eligibilityKpis the dataset used as the source of truth
     */
    function checkCommonFields(financialCompanyType: string, eligibilityKpis: EligibilityKpis): void {
      cy.get(`tr[data-test="${financialCompanyType}"]`).click();
      cy.get('td[data-test="taxonomyEligibleActivityInPercent"]').should(
        "contain",
        formatPercentNumber(eligibilityKpis.taxonomyEligibleActivityInPercent),
      );
      cy.get('td[data-test="taxonomyNonEligibleActivityInPercent"]').should(
        "contain",
        formatPercentNumber(eligibilityKpis.taxonomyNonEligibleActivityInPercent),
      );
      cy.get('td[data-test="derivativesInPercent"]').should(
        "contain",
        formatPercentNumber(eligibilityKpis.derivativesInPercent),
      );
      cy.get('td[data-test="banksAndIssuersInPercent"]').should(
        "contain",
        formatPercentNumber(eligibilityKpis.banksAndIssuersInPercent),
      );
      cy.get('td[data-test="investmentNonNfrdInPercent"]').should(
        "contain",
        formatPercentNumber(eligibilityKpis.investmentNonNfrdInPercent),
      );
    }

    /**
     * Verifies that the frontend correctly displays the insurance firm KPIs
     * @param testData the dataset used as the source of truth
     */
    function checkInsuranceValues(testData: EuTaxonomyDataForFinancials): void {
      checkCommonFields("InsuranceOrReinsurance", testData.eligibilityKpis!.InsuranceOrReinsurance);
      cy.get('td[data-test="taxonomyEligibleNonLifeInsuranceActivities"]').should(
        "contain",
        formatPercentNumber(testData.insuranceKpis!.taxonomyEligibleNonLifeInsuranceActivitiesInPercent),
      );
    }

    /**
     * Verifies that the frontend correctly displays the investment firm KPIs
     * @param testData the dataset used as the source of truth
     */
    function checkInvestmentFirmValues(testData: EuTaxonomyDataForFinancials): void {
      checkCommonFields("InvestmentFirm", testData.eligibilityKpis!.InvestmentFirm);
      cy.get('td[data-test="greenAssetRatioInvestmentFirm"]').should(
        "contain",
        formatPercentNumber(testData.investmentFirmKpis!.greenAssetRatioInPercent),
      );
    }

    /**
     * Verifies that the frontend correctly displays the credit institution KPIs
     * @param testData he dataset used as the source of truth
     * @param individualFieldSubmission whether individual field submission is expected
     * @param dualFieldSubmission whether dual field submission is expected
     */
    function checkCreditInstitutionValues(
      testData: EuTaxonomyDataForFinancials,
      individualFieldSubmission: boolean,
      dualFieldSubmission: boolean,
    ): void {
      checkCommonFields("CreditInstitution", testData.eligibilityKpis!.CreditInstitution);
      if (individualFieldSubmission) {
        cy.get('td[data-test="tradingPortfolio"]').should(
          "contain",
          formatPercentNumber(testData.creditInstitutionKpis!.tradingPortfolioInPercent),
        );
        cy.get('td[data-test="onDemandInterbankLoans"]').should(
          "contain",
          formatPercentNumber(testData.creditInstitutionKpis!.interbankLoansInPercent),
        );
        if (!dualFieldSubmission) {
          cy.get("body").should("not.contain", "Trading portfolio & on demand interbank loans");
        }
      }
      if (dualFieldSubmission) {
        cy.get('td[data-test="tradingPortfolioAndInterbankLoansInPercent"]').should(
          "contain",
          formatPercentNumber(testData.creditInstitutionKpis!.tradingPortfolioAndInterbankLoansInPercent),
        );
        if (!individualFieldSubmission) {
          cy.get("body").should("not.contain", "Trading portfolio");
          cy.get("body").should("not.contain", "On demand interbank loans");
        }
      }
      cy.get('td[data-test="greenAssetRatioCreditInstitution"]').should(
        "contain",
        formatPercentNumber(testData.creditInstitutionKpis!.greenAssetRatioInPercent),
      );
    }

    it("Create a CreditInstitution (combined field submission)", () => {
      const testData = getPreparedFixture("credit-institution-single-field-submission", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod,
      );
      checkCreditInstitutionValues(testData.t, false, true);
    });

    it("Create a CreditInstitution (individual field submission)", () => {
      const testData = getPreparedFixture("credit-institution-dual-field-submission", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod,
      );
      checkCreditInstitutionValues(testData.t, true, false);
    });

    it("Create an insurance company", () => {
      const testData = getPreparedFixture("insurance-company", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod,
      );
      checkInsuranceValues(testData.t);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });

    it("Create an Investment Firm", () => {
      const testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod,
      );
      checkInvestmentFirmValues(testData.t);
    });

    it("Create an Asset Manager", () => {
      const testData = getPreparedFixture("asset-management-company", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod,
      );
      checkCommonFields("AssetManagement", testData.t.eligibilityKpis!.AssetManagement);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
      cy.get("body").should("not.contain", "Taxonomy-eligible non-life insurance economic activities");
    });

    it("Create a Company that is Asset Manager and Insurance", () => {
      const testData = getPreparedFixture("asset-management-insurance-company", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod,
      );
      checkInsuranceValues(testData.t);
      checkCommonFields("AssetManagement", testData.t.eligibilityKpis!.AssetManagement);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });
  },
);
