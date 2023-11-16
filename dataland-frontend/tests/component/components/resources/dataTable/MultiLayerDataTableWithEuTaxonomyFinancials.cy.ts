import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  DataTypeEnum,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
  type ExtendedDataPointBigDecimal,
} from "@clients/backend";

import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import { mountMLDTFrameworkPanelFromFakeFixture } from "@ct/testUtils/MultiLayerDataTableComponentTestUtils";
import { configForEuTaxonomyFinancialsMLDT } from "@/components/resources/frameworkDataSearch/euTaxonomy/configMLDT/configForEutaxonomyFinancialsMLDT";

describe("Component test for EuTaxonomyFinancialPanel", () => {
  let preparedFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;
  const EuTaxonomyFinancialDisplayConfiguration =
    configForEuTaxonomyFinancialsMLDT as MLDTConfig<EuTaxonomyDataForFinancials>;

  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    });
  });

  /**
   * Formats a datapoint as a percentage value rounded to a precision of 0.01%.
   * Returns "No data has been reported" if the datapoint contains no value
   * @param value the value of the datapoint to format as a percentage
   * @returns the formatted string
   */
  function formatPercentageNumberAsString(value?: ExtendedDataPointBigDecimal | null): string {
    if (value === undefined || value === null || value.value === undefined || value.value === null) {
      return "";
    }
    return (Math.round(value.value * 100) / 100).toString();
  }

  /**
   * Verifies that the frontend correctly displays eligibilityKPIs for a specific company type
   * @param financialCompanyType the company type to check
   * @param eligibilityKpis the dataset used as the source of truth
   */
  function checkCommonFields(financialCompanyType: string, eligibilityKpis: EligibilityKpis): void {
    cy.get(`tr[data-test="${financialCompanyType}"]`).click().next('tr[data-section-label="Eligibility KPIs"').click();
    cy.get('td[data-test="taxonomyEligibleActivityInPercent"]').should(
      "contain",
      formatPercentageNumberAsString(eligibilityKpis.taxonomyEligibleActivityInPercent),
    );
    cy.get('td[data-test="taxonomyNonEligibleActivityInPercent"]').should(
      "contain",
      formatPercentageNumberAsString(eligibilityKpis.taxonomyNonEligibleActivityInPercent),
    );
    cy.get('td[data-test="derivativesInPercent"]').should(
      "contain",
      formatPercentageNumberAsString(eligibilityKpis.derivativesInPercent),
    );
    cy.get('td[data-test="banksAndIssuersInPercent"]').should(
      "contain",
      formatPercentageNumberAsString(eligibilityKpis.banksAndIssuersInPercent),
    );
    cy.get('td[data-test="investmentNonNfrdInPercent"]').should(
      "contain",
      formatPercentageNumberAsString(eligibilityKpis.investmentNonNfrdInPercent),
    );
  }

  /**
   * Verifies that the frontend correctly displays the credit institution KPIs
   * @param testData he dataset used as the source of truth
   */
  function checkCreditInstitutionValues(testData: EuTaxonomyDataForFinancials): void {
    checkCommonFields("CreditInstitution", testData.eligibilityKpis!.CreditInstitution);

    cy.get('td[data-test="tradingPortfolioCreditInstitution"]').should(
      "contain",
      formatPercentageNumberAsString(testData.creditInstitutionKpis!.tradingPortfolioInPercent),
    );
    cy.get('td[data-test="interbankLoansCreditInstitution"]').should(
      "contain",
      formatPercentageNumberAsString(testData.creditInstitutionKpis!.interbankLoansInPercent),
    );
    cy.get('td[data-test="tradingPortfolioAndInterbankLoansInPercent"]').should(
      "contain",
      formatPercentageNumberAsString(testData.creditInstitutionKpis!.tradingPortfolioAndInterbankLoansInPercent),
    );
    cy.get('td[data-test="greenAssetRatioCreditInstitution"]').should(
      "contain",
      formatPercentageNumberAsString(testData.creditInstitutionKpis!.greenAssetRatioInPercent),
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
      formatPercentageNumberAsString(testData.insuranceKpis!.taxonomyEligibleNonLifeInsuranceActivitiesInPercent),
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
      formatPercentageNumberAsString(testData.investmentFirmKpis!.greenAssetRatioInPercent),
    );
  }

  it("Check EuTaxonomyFinancial view page", () => {
    const preparedFixture = getPreparedFixture("company-for-all-types", preparedFixtures);
    const EuTaxonomyFinancialData = preparedFixture.t;

    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.EutaxonomyFinancials, EuTaxonomyFinancialDisplayConfiguration, [
      preparedFixture,
    ]);

    checkCreditInstitutionValues(EuTaxonomyFinancialData);
    checkInsuranceValues(EuTaxonomyFinancialData);
    checkInvestmentFirmValues(EuTaxonomyFinancialData);
  });
});
