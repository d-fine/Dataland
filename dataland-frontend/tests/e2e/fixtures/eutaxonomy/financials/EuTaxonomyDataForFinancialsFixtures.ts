import { faker } from "@faker-js/faker";
import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  InsuranceKpis,
  CreditInstitutionKpis,
  InvestmentFirmKpis,
} from "@clients/backend";

import { convertToPercentageString, getCompanyTypeCsvValue, getCompanyTypeHeader } from "@e2e/fixtures/CsvUtils";
import { generateDatapointOrNotReportedAtRandom, getCsvDataPointMapping } from "@e2e/fixtures/common/DataPointFixtures";
import { getCsvCompanyMapping } from "@e2e/fixtures/CompanyFixtures";
import { getCsvSharedEuTaxonomyValuesMapping, populateSharedValues } from "../EuTaxonomySharedValuesFixtures";
import { FixtureData, DataPoint, ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { parse } from "json2csv";

export function generateInsuranceKpis(referencedReports: ReferencedReports): InsuranceKpis {
  const taxonomyEligibleNonLifeInsuranceActivities = randomPercentageValue();
  return {
    taxonomyEligibleNonLifeInsuranceActivities: generateDatapointOrNotReportedAtRandom(
      taxonomyEligibleNonLifeInsuranceActivities,
      referencedReports
    ),
  };
}

export function generateCreditInstitutionKpis(referencedReports: ReferencedReports): CreditInstitutionKpis {
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

export function generateInvestmentFirmKpis(referencedReports: ReferencedReports): InvestmentFirmKpis {
  const greenAssetRatioInvestmentFirm = randomPercentageValue();
  return {
    greenAssetRatio: generateDatapointOrNotReportedAtRandom(greenAssetRatioInvestmentFirm, referencedReports),
  };
}

export function generateEuTaxonomyDataForFinancialsWithTypes(
  financialServicesTypes: Array<EuTaxonomyDataForFinancialsFinancialServicesTypesEnum>
): EuTaxonomyDataForFinancials {
  const returnBase: EuTaxonomyDataForFinancials = {};
  populateSharedValues(returnBase);
  const eligibilityKpis = Object.fromEntries(
    financialServicesTypes.map((it) => [it, generateEligibilityKpis(returnBase.referencedReports!)])
  );
  returnBase.financialServicesTypes = financialServicesTypes;
  returnBase.eligibilityKpis = eligibilityKpis;
  returnBase.creditInstitutionKpis =
    financialServicesTypes.indexOf("CreditInstitution") >= 0
      ? generateCreditInstitutionKpis(returnBase.referencedReports!)
      : undefined;
  returnBase.insuranceKpis =
    financialServicesTypes.indexOf("InsuranceOrReinsurance") >= 0
      ? generateInsuranceKpis(returnBase.referencedReports!)
      : undefined;
  returnBase.investmentFirmKpis =
    financialServicesTypes.indexOf("InvestmentFirm") >= 0
      ? generateInvestmentFirmKpis(returnBase.referencedReports!)
      : undefined;
  return returnBase;
}

export function generateEuTaxonomyDataForFinancials(): EuTaxonomyDataForFinancials {
  const financialServicesTypes = faker.helpers.arrayElements(
    Object.values(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum)
  );
  return generateEuTaxonomyDataForFinancialsWithTypes(financialServicesTypes);
}

export function generateEligibilityKpis(reports: ReferencedReports): EligibilityKpis {
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

export function getCsvEligibilityKpiMapping(
  type: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum
): Array<DataPoint<FixtureData<EuTaxonomyDataForFinancials>, string | number>> {
  return [
    ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
      `Exposures to taxonomy-eligible economic activities ${getCompanyTypeHeader(type)}`,
      (row) => row.t.eligibilityKpis![type]?.taxonomyEligibleActivity,
      convertToPercentageString
    ),
    ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
      `Exposures to taxonomy non-eligible economic activities ${getCompanyTypeHeader(type)}`,
      (row) => row.t.eligibilityKpis![type]?.taxonomyNonEligibleActivity,
      convertToPercentageString
    ),
    ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
      `Exposures to central governments, central banks, supranational issuers ${getCompanyTypeHeader(type)}`,
      (row) => row.t.eligibilityKpis![type]?.banksAndIssuers,
      convertToPercentageString
    ),
    ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
      `Exposures to derivatives ${getCompanyTypeHeader(type)}`,
      (row) => row.t.eligibilityKpis![type]?.derivatives,
      convertToPercentageString
    ),
    ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
      `Exposures to non-NFRD entities ${getCompanyTypeHeader(type)}`,
      (row) => row.t.eligibilityKpis![type]?.investmentNonNfrd,
      convertToPercentageString
    ),
  ];
}

export function generateCSVDataForFinancials(
  companyInformationWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>
): string {
  const options = {
    fields: [
      ...getCsvCompanyMapping<EuTaxonomyDataForFinancials>(),
      ...getCsvSharedEuTaxonomyValuesMapping(2),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
        `Trading portfolio & on-demand interbank loans`,
        (row) => row.t.creditInstitutionKpis?.tradingPortfolioAndInterbankLoans,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
        `Trading portfolio`,
        (row) => row.t.creditInstitutionKpis?.tradingPortfolio,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
        `On-demand interbank loans`,
        (row) => row.t.creditInstitutionKpis?.interbankLoans,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
        `Taxonomy-eligible non-life insurance economic activities`,
        (row) => row.t.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivities,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
        `Green Asset Ratio Credit Institution`,
        (row) => row.t.creditInstitutionKpis?.greenAssetRatio,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
        `Green Asset Ratio Investment Firm`,
        (row) => row.t.investmentFirmKpis?.greenAssetRatio,
        convertToPercentageString
      ),
      {
        label: "FS - company type",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          row.t.financialServicesTypes?.map((it) => getCompanyTypeCsvValue(it)).join(", "),
      },
    ],
    delimiter: ";",
  };
  return parse(companyInformationWithEuTaxonomyDataForFinancials, options);
}
