import { faker } from "@faker-js/faker";
import {
  CreditInstitutionKpis,
  EligibilityKpis,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  InsuranceKpis,
  InvestmentFirmKpis,
} from "@clients/backend";
import { generateDatapointOrNotReportedAtRandom } from "@e2e/fixtures/common/DataPointFixtures";
import { generateEuTaxonomyWithBaseFields } from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";

/**
 * Generates random insurance company KPIs
 * @param referencedReports he reports that can be referenced as data sources
 * @returns random insurance company KPIs
 */
export function generateInsuranceKpis(referencedReports: ReferencedDocuments): InsuranceKpis {
  const taxonomyEligibleNonLifeInsuranceActivities = randomPercentageValue();
  return {
    taxonomyEligibleNonLifeInsuranceActivities: generateDatapointOrNotReportedAtRandom(
      taxonomyEligibleNonLifeInsuranceActivities,
      referencedReports
    ),
  };
}

/**
 * Generates random credit institution KPIs
 * @param referencedReports he reports that can be referenced as data sources
 * @returns random credit institution KPIs
 */
export function generateCreditInstitutionKpis(referencedReports: ReferencedDocuments): CreditInstitutionKpis {
  let tradingPortfolioAndInterbankLoans = undefined;
  let interbankLoans = undefined;
  let tradingPortfolio = undefined;

  const singleOrDualField = faker.datatype.boolean();
  if (singleOrDualField) {
    tradingPortfolioAndInterbankLoans = randomPercentageValue();
  } else {
    interbankLoans = randomPercentageValue();
    tradingPortfolio = randomPercentageValue();
  }
  const greenAssetRatioCreditInstitution = randomPercentageValue();

  return {
    interbankLoans: generateDatapointOrNotReportedAtRandom(interbankLoans, referencedReports),
    tradingPortfolio: generateDatapointOrNotReportedAtRandom(tradingPortfolio, referencedReports),
    tradingPortfolioAndInterbankLoans: generateDatapointOrNotReportedAtRandom(
      tradingPortfolioAndInterbankLoans,
      referencedReports
    ),
    greenAssetRatio: generateDatapointOrNotReportedAtRandom(greenAssetRatioCreditInstitution, referencedReports),
  };
}

/**
 * Generates random investment firm KPIs
 * @param referencedReports he reports that can be referenced as data sources
 * @returns random investment firm KPIs
 */
export function generateInvestmentFirmKpis(referencedReports: ReferencedDocuments): InvestmentFirmKpis {
  const greenAssetRatioInvestmentFirm = randomPercentageValue();
  return {
    greenAssetRatio: generateDatapointOrNotReportedAtRandom(greenAssetRatioInvestmentFirm, referencedReports),
  };
}

/**
 * Generates a single eutaxonomy-financials fixture for a company with the given financial services types
 * @param financialServicesTypes the financial services of the company to generate data for
 * @returns a random eutaxonomy-financials fixture
 */
export function generateEuTaxonomyDataForFinancialsWithTypes(
  financialServicesTypes: Array<EuTaxonomyDataForFinancialsFinancialServicesTypesEnum>
): EuTaxonomyDataForFinancials {
  const returnBase: EuTaxonomyDataForFinancials = generateEuTaxonomyWithBaseFields();
  const eligibilityKpis = Object.fromEntries(
    financialServicesTypes.map((it) => [it, generateEligibilityKpis(assertDefined(returnBase.referencedReports))])
  );
  returnBase.financialServicesTypes = financialServicesTypes;
  returnBase.eligibilityKpis = eligibilityKpis;
  returnBase.creditInstitutionKpis =
    financialServicesTypes.indexOf("CreditInstitution") >= 0
      ? generateCreditInstitutionKpis(assertDefined(returnBase.referencedReports))
      : undefined;
  returnBase.insuranceKpis =
    financialServicesTypes.indexOf("InsuranceOrReinsurance") >= 0
      ? generateInsuranceKpis(assertDefined(returnBase.referencedReports))
      : undefined;
  returnBase.investmentFirmKpis =
    financialServicesTypes.indexOf("InvestmentFirm") >= 0
      ? generateInvestmentFirmKpis(assertDefined(returnBase.referencedReports))
      : undefined;
  return returnBase;
}

/**
 * Generates a single eutaxonomy-financials fixture
 * @returns a random eutaxonomy-financials fixture
 */
export function generateEuTaxonomyDataForFinancials(): EuTaxonomyDataForFinancials {
  const financialServicesTypes = faker.helpers.arrayElements(
    Object.values(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum)
  );
  return generateEuTaxonomyDataForFinancialsWithTypes(financialServicesTypes);
}

/**
 * Generates a random set of eligibility KPIS
 * @param reports the reports that can be referenced as data sources
 * @returns a random set of eligibility KPI´s
 */
export function generateEligibilityKpis(reports: ReferencedDocuments): EligibilityKpis {
  const taxonomyEligibleEconomicActivity = randomPercentageValue();
  const taxonomyNonEligibleEconomicActivity = randomPercentageValue();
  const eligibleDerivatives = randomPercentageValue();
  const banksAndIssuers = randomPercentageValue();
  const nonNfrd = randomPercentageValue();

  return {
    banksAndIssuers: generateDatapointOrNotReportedAtRandom(banksAndIssuers, reports),
    derivatives: generateDatapointOrNotReportedAtRandom(eligibleDerivatives, reports),
    investmentNonNfrd: generateDatapointOrNotReportedAtRandom(nonNfrd, reports),
    taxonomyEligibleActivity: generateDatapointOrNotReportedAtRandom(taxonomyEligibleEconomicActivity, reports),
    taxonomyNonEligibleActivity: generateDatapointOrNotReportedAtRandom(taxonomyNonEligibleEconomicActivity, reports),
  };
}
