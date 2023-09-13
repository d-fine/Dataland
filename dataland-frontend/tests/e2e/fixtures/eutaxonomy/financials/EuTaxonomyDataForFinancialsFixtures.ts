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
import { type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a single eutaxonomy-financials fixture
 * @param undefinedProbability
 * @returns a random eutaxonomy-financials fixture
 */
export function generateEuTaxonomyDataForFinancials(
  undefinedProbability = DEFAULT_PROBABILITY,
): EuTaxonomyDataForFinancials {
  const dataGenerator = new EuFinancialsGenerator(undefinedProbability);
  return dataGenerator.generateEuTaxonomyDataForFinancialsWithTypes();
}

export class EuFinancialsGenerator extends Generator {
  financialServicesTypes: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum[];

  constructor(undefinedProbability = DEFAULT_PROBABILITY) {
    super(undefinedProbability);
    this.financialServicesTypes = faker.helpers.arrayElements(
      Object.values(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum),
    );
  }

  setReports(reports: ReferencedDocuments): void {
    this.reports = reports;
  }

  /**
   * Generates a single eutaxonomy-financials fixture for a company with the given financial services types
   * @param financialServicesTypes
   * @returns a random eutaxonomy-financials fixture
   */
  generateEuTaxonomyDataForFinancialsWithTypes(
    financialServicesTypes = this.financialServicesTypes,
  ): EuTaxonomyDataForFinancials {
    const returnBase: EuTaxonomyDataForFinancials = generateEuTaxonomyWithBaseFields();
    const eligibilityKpis = Object.fromEntries(
      financialServicesTypes.map((it) => [it, this.generateEligibilityKpis()]),
    );
    returnBase.financialServicesTypes = financialServicesTypes;
    returnBase.eligibilityKpis = eligibilityKpis;
    returnBase.creditInstitutionKpis =
      financialServicesTypes.indexOf("CreditInstitution") >= 0 ? this.generateCreditInstitutionKpis() : undefined;
    returnBase.insuranceKpis =
      financialServicesTypes.indexOf("InsuranceOrReinsurance") >= 0 ? this.generateInsuranceKpis() : undefined;
    returnBase.investmentFirmKpis =
      financialServicesTypes.indexOf("InvestmentFirm") >= 0 ? this.generateInvestmentFirmKpis() : undefined;
    return returnBase;
  }
  /**
   * Generates random insurance company KPIs
   * @returns random insurance company KPIs
   */
  generateInsuranceKpis(): InsuranceKpis {
    return {
      taxonomyEligibleNonLifeInsuranceActivities: this.randomDataPoint(this.randomPercentageValue()),
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
      interbankLoans: this.randomDataPoint(interbankLoans),
      tradingPortfolio: this.randomDataPoint(tradingPortfolio),
      tradingPortfolioAndInterbankLoans: this.randomDataPoint(tradingPortfolioAndInterbankLoans),
      greenAssetRatio: this.randomDataPoint(greenAssetRatioCreditInstitution),
    };
  }

  /**
   * Generates random investment firm KPIs
   * @returns random investment firm KPIs
   */
  generateInvestmentFirmKpis(): InvestmentFirmKpis {
    return {
      greenAssetRatio: this.randomDataPoint(this.randomPercentageValue()),
    };
  }
  /**
   * Generates a random set of eligibility KPIS
   * @returns a random set of eligibility KPI´s
   */
  generateEligibilityKpis(): EligibilityKpis {
    return {
      banksAndIssuers: this.randomDataPoint(this.randomPercentageValue()),
      derivatives: this.randomDataPoint(this.randomPercentageValue()),
      investmentNonNfrd: this.randomDataPoint(this.randomPercentageValue()),
      taxonomyEligibleActivity: this.randomDataPoint(this.randomPercentageValue()),
      taxonomyNonEligibleActivity: this.randomDataPoint(this.randomPercentageValue()),
    };
  }
}
