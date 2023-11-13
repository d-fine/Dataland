import { faker } from "@faker-js/faker";
import {
  type CreditInstitutionKpis,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  type InsuranceKpis,
  type InvestmentFirmKpis,
} from "@clients/backend";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy-shared/EuTaxonomySharedValuesFixtures";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { pickSubsetOfElements } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a single eutaxonomy-financials fixture
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random eutaxonomy-financials fixture
 */
export function generateEuTaxonomyDataForFinancials(
  nullProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForFinancials {
  const dataGenerator = new EuFinancialsGenerator(nullProbability);
  return dataGenerator.generateEuTaxonomyDataForFinancialsWithTypes();
}

export class EuFinancialsGenerator extends Generator {
  financialServicesTypes = pickSubsetOfElements(Object.values(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum));

  /**
   * Generates a single eutaxonomy-financials fixture for a company with the given financial services types
   * @param financialServicesTypes the selected financial service type, if not given default values are used
   * @returns a random eutaxonomy-financials fixture
   */
  generateEuTaxonomyDataForFinancialsWithTypes(
    financialServicesTypes = this.financialServicesTypes,
  ): EuTaxonomyDataForFinancials {
    const returnBase: EuTaxonomyDataForFinancials = generateEuTaxonomyWithBaseFields(
      this.reports,
      this.nullProbability,
    );
    const eligibilityKpis = Object.fromEntries(
      financialServicesTypes.map((it) => [it, this.generateEligibilityKpis()]),
    );
    returnBase.financialServicesTypes = financialServicesTypes;
    returnBase.eligibilityKpis = eligibilityKpis;
    returnBase.creditInstitutionKpis =
      financialServicesTypes.indexOf("CreditInstitution") >= 0 ? this.generateCreditInstitutionKpis() : null;
    returnBase.insuranceKpis =
      financialServicesTypes.indexOf("InsuranceOrReinsurance") >= 0 ? this.generateInsuranceKpis() : null;
    returnBase.investmentFirmKpis =
      financialServicesTypes.indexOf("InvestmentFirm") >= 0 ? this.generateInvestmentFirmKpis() : null;
    return returnBase;
  }
  /**
   * Generates random insurance company KPIs
   * @returns random insurance company KPIs
   */
  generateInsuranceKpis(): InsuranceKpis {
    return {
      taxonomyEligibleNonLifeInsuranceActivitiesInPercent: this.randomExtendedDataPoint(generatePercentageValue()),
    };
  }

  /**
   * Generates random credit institution KPIs
   * @returns random credit institution KPIs
   */
  generateCreditInstitutionKpis(): CreditInstitutionKpis {
    let tradingPortfolioAndInterbankLoans = null;
    let interbankLoans = null;
    let tradingPortfolio = null;

    const singleOrDualField = faker.datatype.boolean();
    if (singleOrDualField) {
      tradingPortfolioAndInterbankLoans = this.randomPercentageValue();
    } else {
      interbankLoans = this.randomPercentageValue();
      tradingPortfolio = this.randomPercentageValue();
    }
    const greenAssetRatioCreditInstitution = this.randomPercentageValue();

    return {
      interbankLoansInPercent: this.randomExtendedDataPoint(interbankLoans),
      tradingPortfolioInPercent: this.randomExtendedDataPoint(tradingPortfolio),
      tradingPortfolioAndInterbankLoansInPercent: this.randomExtendedDataPoint(tradingPortfolioAndInterbankLoans),
      greenAssetRatioInPercent: this.randomExtendedDataPoint(greenAssetRatioCreditInstitution),
    };
  }

  /**
   * Generates random investment firm KPIs
   * @returns random investment firm KPIs
   */
  generateInvestmentFirmKpis(): InvestmentFirmKpis {
    return {
      greenAssetRatioInPercent: this.randomExtendedDataPoint(generatePercentageValue()),
    };
  }
  /**
   * Generates a random set of eligibility KPIS
   * @returns a random set of eligibility KPI´s
   */
  generateEligibilityKpis(): EligibilityKpis {
    return {
      banksAndIssuersInPercent: this.randomExtendedDataPoint(generatePercentageValue()),
      derivativesInPercent: this.randomExtendedDataPoint(generatePercentageValue()),
      investmentNonNfrdInPercent: this.randomExtendedDataPoint(generatePercentageValue()),
      taxonomyEligibleActivityInPercent: this.randomExtendedDataPoint(generatePercentageValue()),
      taxonomyNonEligibleActivityInPercent: this.randomExtendedDataPoint(generatePercentageValue()),
    };
  }
}
