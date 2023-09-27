import { faker } from "@faker-js/faker";
import {
  type CreditInstitutionKpis,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  type InsuranceKpis,
  type InvestmentFirmKpis,
} from "@clients/backend";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { pickSubsetOfElements } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a single eutaxonomy-financials fixture
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns a random eutaxonomy-financials fixture
 */
export function generateEuTaxonomyDataForFinancials(
  undefinedProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForFinancials {
  const dataGenerator = new EuFinancialsGenerator(undefinedProbability, true);
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
      this.missingValueProbability,
    );
    const eligibilityKpis = Object.fromEntries(
      financialServicesTypes.map((it) => [it, this.generateEligibilityKpis()]),
    );
    returnBase.financialServicesTypes = financialServicesTypes;
    returnBase.eligibilityKpis = eligibilityKpis;
    returnBase.creditInstitutionKpis =
      financialServicesTypes.indexOf("CreditInstitution") >= 0 ? this.generateCreditInstitutionKpis() : this.missingValue();
    returnBase.insuranceKpis =
      financialServicesTypes.indexOf("InsuranceOrReinsurance") >= 0 ? this.generateInsuranceKpis() : this.missingValue();
    returnBase.investmentFirmKpis =
      financialServicesTypes.indexOf("InvestmentFirm") >= 0 ? this.generateInvestmentFirmKpis() : this.missingValue();
    return returnBase;
  }
  /**
   * Generates random insurance company KPIs
   * @returns random insurance company KPIs
   */
  generateInsuranceKpis(): InsuranceKpis {
    return {
      taxonomyEligibleNonLifeInsuranceActivitiesInPercent: this.randomDataPoint(generatePercentageValue()),
    };
  }

  /**
   * Generates random credit institution KPIs
   * @returns random credit institution KPIs
   */
  generateCreditInstitutionKpis(): CreditInstitutionKpis {
    let tradingPortfolioAndInterbankLoans = undefined;
    let interbankLoans = undefined;
    let tradingPortfolio = undefined;

    const singleOrDualField = faker.datatype.boolean();
    if (singleOrDualField) {
      tradingPortfolioAndInterbankLoans = this.randomPercentageValue();
    } else {
      interbankLoans = this.randomPercentageValue();
      tradingPortfolio = this.randomPercentageValue();
    }
    const greenAssetRatioCreditInstitution = this.randomPercentageValue();

    return {
      interbankLoansInPercent: this.randomDataPoint(interbankLoans),
      tradingPortfolioInPercent: this.randomDataPoint(tradingPortfolio),
      tradingPortfolioAndInterbankLoansInPercent: this.randomDataPoint(tradingPortfolioAndInterbankLoans),
      greenAssetRatioInPercent: this.randomDataPoint(greenAssetRatioCreditInstitution),
    };
  }

  /**
   * Generates random investment firm KPIs
   * @returns random investment firm KPIs
   */
  generateInvestmentFirmKpis(): InvestmentFirmKpis {
    return {
      greenAssetRatioInPercent: this.randomDataPoint(generatePercentageValue()),
    };
  }
  /**
   * Generates a random set of eligibility KPIS
   * @returns a random set of eligibility KPI´s
   */
  generateEligibilityKpis(): EligibilityKpis {
    return {
      banksAndIssuersInPercent: this.randomDataPoint(generatePercentageValue()),
      derivativesInPercent: this.randomDataPoint(generatePercentageValue()),
      investmentNonNfrdInPercent: this.randomDataPoint(generatePercentageValue()),
      taxonomyEligibleActivityInPercent: this.randomDataPoint(generatePercentageValue()),
      taxonomyNonEligibleActivityInPercent: this.randomDataPoint(generatePercentageValue()),
    };
  }
}
