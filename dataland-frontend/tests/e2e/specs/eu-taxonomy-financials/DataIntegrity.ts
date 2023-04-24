import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
  fillEuTaxonomyForFinancialsUploadForm,
  submitEuTaxonomyFinancialsUploadForm,
  uploadOneEuTaxonomyFinancialsDatasetViaApi,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import {
  CompanyInformation,
  DataPointBigDecimal,
  DataTypeEnum,
  EligibilityKpis,
  EuTaxonomyDataForFinancials,
} from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";

describeIf(
  "As a user, I expect that the correct data gets displayed depending on the type of the financial company",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
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
     *
     * @param companyInformation the company information to upload
     * @param testData the dataset to upload
     * @param reportingPeriod the period associated to the EU Taxonomy data for Financials to upload
     */
    function uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
      companyInformation: CompanyInformation,
      testData: EuTaxonomyDataForFinancials,
      reportingPeriod: string
    ): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
          (storedCompany) => {
            return uploadOneEuTaxonomyFinancialsDatasetViaApi(
              token,
              storedCompany.companyId,
              reportingPeriod,
              testData
            ).then(() => {
              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`
              );
            });
          }
        );
      });
    }

    /**
     * Formats a datapoint as a percentage value rounded to a precision of 0.01%.
     * Returns "No data has been reported" if the datapoint contains no value
     *
     * @param value the value of the datapoint to format as a percentage
     * @returns the formatted string
     */
    function formatPercentNumber(value?: DataPointBigDecimal): string {
      if (value === undefined || value === null || value.value === undefined || value.value === null)
        return "No data has been reported";
      return (Math.round(value.value * 100 * 100) / 100).toString();
    }

    /**
     * Verifies that the frontend correctly displays eligibilityKPIs for a specific company type
     *
     * @param financialCompanyType the company type to check
     * @param eligibilityKpis the dataset used as the source of truth
     */
    function checkCommonFields(financialCompanyType: string, eligibilityKpis: EligibilityKpis): void {
      cy.get(`div[name="taxonomyEligibleActivity${financialCompanyType}"]`)
        .should("contain", "Taxonomy-eligible economic activity")
        .should("contain", formatPercentNumber(eligibilityKpis.taxonomyEligibleActivity));
      cy.get(`div[name="taxonomyNonEligibleActivity${financialCompanyType}"]`)
        .should("contain", "Taxonomy-non-eligible economic activity")
        .should("contain", formatPercentNumber(eligibilityKpis.taxonomyNonEligibleActivity));
      cy.get(`div[name="derivatives${financialCompanyType}"]`)
        .should("contain", "Derivatives")
        .should("contain", formatPercentNumber(eligibilityKpis.derivatives));
      cy.get(`div[name="banksAndIssuers${financialCompanyType}"]`)
        .should("contain", "Banks and issuers")
        .should("contain", formatPercentNumber(eligibilityKpis.banksAndIssuers));
      cy.get(`div[name="investmentNonNfrd${financialCompanyType}"]`)
        .should("contain", "Non-NFRD")
        .should("contain", formatPercentNumber(eligibilityKpis.investmentNonNfrd));
    }

    /**
     * Verifies that the frontend correctly displays the insurance firm KPIs
     *
     * @param testData the dataset used as the source of truth
     */
    function checkInsuranceValues(testData: EuTaxonomyDataForFinancials): void {
      checkCommonFields("InsuranceOrReinsurance", testData.eligibilityKpis!.InsuranceOrReinsurance);
      cy.get('div[name="taxonomyEligibleNonLifeInsuranceActivities"]')
        .should("contain", "Taxonomy-eligible non-life insurance economic activities")
        .should("contain", formatPercentNumber(testData.insuranceKpis!.taxonomyEligibleNonLifeInsuranceActivities));
    }

    /**
     * Verifies that the frontend correctly displays the investment firm KPIs
     *
     * @param testData the dataset used as the source of truth
     */
    function checkInvestmentFirmValues(testData: EuTaxonomyDataForFinancials): void {
      checkCommonFields("InvestmentFirm", testData.eligibilityKpis!.InvestmentFirm);
      cy.get('div[name="greenAssetRatioInvestmentFirm"]')
        .should("contain", "Green asset ratio")
        .should("contain", formatPercentNumber(testData.investmentFirmKpis!.greenAssetRatio));
    }

    /**
     * Verifies that the frontend correctly displays the credit institution KPIs
     *
     * @param testData he dataset used as the source of truth
     * @param individualFieldSubmission whether individual field submission is expected
     * @param dualFieldSubmission whether dual field submission is expected
     */
    function checkCreditInstitutionValues(
      testData: EuTaxonomyDataForFinancials,
      individualFieldSubmission: boolean,
      dualFieldSubmission: boolean
    ): void {
      checkCommonFields("CreditInstitution", testData.eligibilityKpis!.CreditInstitution);
      if (individualFieldSubmission) {
        cy.get('div[name="tradingPortfolio"]')
          .should("contain", "Trading portfolio")
          .should("contain", formatPercentNumber(testData.creditInstitutionKpis!.tradingPortfolio));
        cy.get('div[name="onDemandInterbankLoans"]')
          .should("contain", "On demand interbank loans")
          .should("contain", formatPercentNumber(testData.creditInstitutionKpis!.interbankLoans));
        if (!dualFieldSubmission) {
          cy.get("body").should("not.contain", "Trading portfolio & on demand interbank loans");
        }
      }
      if (dualFieldSubmission) {
        cy.get('div[name="tradingPortfolioAndOnDemandInterbankLoans"]')
          .should("contain", "Trading portfolio & on demand interbank loans")
          .should("contain", formatPercentNumber(testData.creditInstitutionKpis!.tradingPortfolioAndInterbankLoans));
        if (!individualFieldSubmission) {
          cy.get("body").should("not.contain", /^Trading portfolio$/);
          cy.get("body").should("not.contain", "On demand interbank loans");
        }
      }
      cy.get('div[name="greenAssetRatioCreditInstitution"]')
        .should("contain", "Green asset ratio")
        .should("contain", formatPercentNumber(testData.creditInstitutionKpis!.greenAssetRatio));
    }

    it("Create a CreditInstitution (combined field submission)", () => {
      const testData = getPreparedFixture("credit-institution-single-field-submission", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod
      );
      checkCreditInstitutionValues(testData.t, false, true);
    });

    it("Create a CreditInstitution (individual field submission)", () => {
      const testData = getPreparedFixture("credit-institution-dual-field-submission", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod
      );
      checkCreditInstitutionValues(testData.t, true, false);
    });

    it("Create an insurance company", () => {
      const testData = getPreparedFixture("insurance-company", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod
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
        testData.reportingPeriod
      );
      checkInvestmentFirmValues(testData.t);
    });

    it("Create an Asset Manager", () => {
      const testData = getPreparedFixture("asset-management-company", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForFinancialsViaApiAndVisitFrameworkDataViewPage(
        testData.companyInformation,
        testData.t,
        testData.reportingPeriod
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
        testData.reportingPeriod
      );
      checkInsuranceValues(testData.t);
      checkCommonFields("AssetManagement", testData.t.eligibilityKpis!.AssetManagement);
      cy.get("body").should("not.contain", "Trading portfolio");
      cy.get("body").should("not.contain", "demand interbank loans");
    });
  }
);
