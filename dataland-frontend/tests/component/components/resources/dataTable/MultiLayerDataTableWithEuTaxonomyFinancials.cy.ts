import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type EligibilityKpis, type EuTaxonomyDataForFinancials } from '@clients/backend';

import {
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';

import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { configForEuTaxonomyFinancialsMLDT } from '@/components/resources/frameworkDataSearch/euTaxonomy/configForEutaxonomyFinancialsMLDT';
import { formatPercentageNumberAsString } from '@/utils/Formatter';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { euTaxonomyKpiNameMappings } from '@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel';

describe('Component test for EuTaxonomyFinancialPanel', () => {
  let preparedFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;
  const EuTaxonomyFinancialDisplayConfiguration =
    configForEuTaxonomyFinancialsMLDT as MLDTConfig<EuTaxonomyDataForFinancials>;

  before(function () {
    cy.fixture('CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    });
  });

  /**
   * Verifies that the frontend correctly displays eligibilityKPIs for a specific company type
   * @param financialCompanyType the company type to check
   * @param eligibilityKpis the dataset used as the source of truth
   */
  function checkCommonFields(financialCompanyType: string, eligibilityKpis: EligibilityKpis): void {
    getSectionHead(financialCompanyType, true);
    getCellValueContainer(euTaxonomyKpiNameMappings.taxonomyEligibleActivityInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(eligibilityKpis.taxonomyEligibleActivityInPercent?.value))
    );

    getCellValueContainer(euTaxonomyKpiNameMappings.taxonomyNonEligibleActivityInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(eligibilityKpis.taxonomyNonEligibleActivityInPercent?.value))
    );

    getCellValueContainer(euTaxonomyKpiNameMappings.derivativesInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(eligibilityKpis.derivativesInPercent?.value))
    );

    getCellValueContainer(euTaxonomyKpiNameMappings.banksAndIssuersInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(eligibilityKpis.banksAndIssuersInPercent?.value))
    );

    getCellValueContainer(euTaxonomyKpiNameMappings.investmentNonNfrdInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(eligibilityKpis.investmentNonNfrdInPercent?.value))
    );
  }

  /**
   * Verifies that the frontend correctly displays the credit institution KPIs
   * @param testData he dataset used as the source of truth
   */
  function checkCreditInstitutionValues(testData: EuTaxonomyDataForFinancials): void {
    checkCommonFields('Credit Institution', testData.eligibilityKpis!.CreditInstitution);

    getCellValueContainer(euTaxonomyKpiNameMappings.tradingPortfolioInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(testData.creditInstitutionKpis!.tradingPortfolioInPercent?.value))
    );

    getCellValueContainer(euTaxonomyKpiNameMappings.interbankLoansInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(testData.creditInstitutionKpis!.interbankLoansInPercent?.value))
    );

    getCellValueContainer(euTaxonomyKpiNameMappings.tradingPortfolioAndInterbankLoansInPercent).should(
      'contain',
      formatPercentageNumberAsString(
        assertDefined(testData.creditInstitutionKpis!.tradingPortfolioAndInterbankLoansInPercent?.value)
      )
    );

    getCellValueContainer(euTaxonomyKpiNameMappings.greenAssetRatioInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(testData.creditInstitutionKpis!.greenAssetRatioInPercent?.value))
    );

    getSectionHead('Eligibility KPIs', true).should('exist');
    getSectionHead('Credit Institution', true).should('exist');
  }

  /**
   * Verifies that the frontend correctly displays the insurance firm KPIs
   * @param testData the dataset used as the source of truth
   */
  function checkInsuranceValues(testData: EuTaxonomyDataForFinancials): void {
    checkCommonFields('Insurance or Reinsurance', testData.eligibilityKpis!.InsuranceOrReinsurance);
    getCellValueContainer(euTaxonomyKpiNameMappings.taxonomyEligibleNonLifeInsuranceActivitiesInPercent).should(
      'contain',
      formatPercentageNumberAsString(
        assertDefined(testData.insuranceKpis!.taxonomyEligibleNonLifeInsuranceActivitiesInPercent?.value)
      )
    );
    getSectionHead('Eligibility KPIs', true).should('exist');
    getSectionHead('Insurance or Reinsurance', true).should('exist');
  }

  /**
   * Verifies that the frontend correctly displays the investment firm KPIs
   * @param testData the dataset used as the source of truth
   */
  function checkInvestmentFirmValues(testData: EuTaxonomyDataForFinancials): void {
    checkCommonFields('Investment Firm', testData.eligibilityKpis!.InvestmentFirm);
    getCellValueContainer(euTaxonomyKpiNameMappings.greenAssetRatioInPercent).should(
      'contain',
      formatPercentageNumberAsString(assertDefined(testData.investmentFirmKpis!.greenAssetRatioInPercent?.value))
    );
  }

  it('Check EuTaxonomyFinancial view page', () => {
    const preparedFixture = getPreparedFixture('company-for-all-types', preparedFixtures);
    const EuTaxonomyFinancialData = preparedFixture.t;

    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.EutaxonomyFinancials, EuTaxonomyFinancialDisplayConfiguration, [
      preparedFixture,
    ]);

    checkCreditInstitutionValues(EuTaxonomyFinancialData);
    checkInsuranceValues(EuTaxonomyFinancialData);
    checkInvestmentFirmValues(EuTaxonomyFinancialData);
  });
});
