import { faker } from "@faker-js/faker";
import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
} from "@clients/backend";

import { convertToPercentageString, getCompanyTypeCsvValue, getCompanyTypeHeader } from "./CsvUtils";
import { FixtureData } from "./GenerateFakeFixtures";
import { generateDatapointOrNotReportedAtRandom } from "./DataPointFixtures";
import { getCsvCompanyMapping } from "./CompanyFixtures";
import { getCsvDataPointMapping } from "./DataPointFixtures";
import { getCsvSharedEuTaxonomyValuesMapping, populateSharedValues } from "./EuTaxonomySharedValues";
import { ReferencedReports } from "./Utils";

const { parse } = require("json2csv");

const resolution = 0.0001;

export function generateEuTaxonomyDataForFinancials(): EuTaxonomyDataForFinancials {
  const returnBase: EuTaxonomyDataForFinancials = {};
  populateSharedValues(returnBase);

  const financialServicesTypes = faker.helpers.arrayElements(
    Object.values(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum)
  );

  let tradingPortfolioAndInterbankLoans = undefined;
  let interbankLoans = undefined;
  let tradingPortfolio = undefined;
  let taxonomyEligibleNonLifeInsuranceActivities = undefined;
  let greenAssetRatio = undefined;

  if (financialServicesTypes.indexOf("CreditInstitution") >= 0) {
    const singleOrDualField = faker.datatype.boolean();
    if (singleOrDualField) {
      tradingPortfolioAndInterbankLoans = faker.datatype.float({
        min: 0,
        max: 1,
        precision: resolution,
      });
    } else {
      interbankLoans = faker.datatype.float({
        min: 0,
        max: 1,
        precision: resolution,
      });
      tradingPortfolio = faker.datatype.float({
        min: 0,
        max: 1,
        precision: resolution,
      });
    }
  } else if (financialServicesTypes.indexOf("InsuranceOrReinsurance") >= 0) {
    taxonomyEligibleNonLifeInsuranceActivities = faker.datatype.float({
      min: 0,
      max: 1,
      precision: resolution,
    });
  }

  if (
    financialServicesTypes.indexOf("CreditInstitution") >= 0 ||
    financialServicesTypes.indexOf("InvestmentFirm") >= 0
  ) {
    greenAssetRatio = faker.datatype.float({ min: 0, max: 1, precision: resolution });
  }

  const eligibilityKpis = Object.fromEntries(
    financialServicesTypes.map((it) => [it, generateEligibilityKpis(returnBase.referencedReports!!)])
  );

  returnBase.greenAssetRatio = generateDatapointOrNotReportedAtRandom(greenAssetRatio, returnBase.referencedReports!!);
  returnBase.financialServicesTypes = financialServicesTypes;
  returnBase.eligibilityKpis = eligibilityKpis;
  returnBase.creditInstitutionKpis = {
    interbankLoans: generateDatapointOrNotReportedAtRandom(interbankLoans, returnBase.referencedReports!!),
    tradingPortfolio: generateDatapointOrNotReportedAtRandom(tradingPortfolio, returnBase.referencedReports!!),
    tradingPortfolioAndInterbankLoans: generateDatapointOrNotReportedAtRandom(
      tradingPortfolioAndInterbankLoans,
      returnBase.referencedReports!!
    ),
  };
  returnBase.insuranceKpis = {
    taxonomyEligibleNonLifeInsuranceActivities: generateDatapointOrNotReportedAtRandom(
      taxonomyEligibleNonLifeInsuranceActivities,
      returnBase.referencedReports!!
    ),
  };
  return returnBase;
}

export function generateEligibilityKpis(reports: ReferencedReports): EligibilityKpis {
  const taxonomyEligibleEconomicActivity = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const taxonomyNonEligibleEconomicActivity = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const eligibleDerivatives = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const banksAndIssuers = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const nonNfrd = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });

  return {
    banksAndIssuers: generateDatapointOrNotReportedAtRandom(banksAndIssuers, reports),
    derivatives: generateDatapointOrNotReportedAtRandom(eligibleDerivatives, reports),
    investmentNonNfrd: generateDatapointOrNotReportedAtRandom(nonNfrd, reports),
    taxonomyEligibleActivity: generateDatapointOrNotReportedAtRandom(taxonomyEligibleEconomicActivity, reports),
    taxonomyNonEligibleActivity: generateDatapointOrNotReportedAtRandom(taxonomyNonEligibleEconomicActivity, reports),
  };
}

export function getCsvEligibilityKpiMapping(type: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum) {
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
) {
  const options = {
    fields: [
      ...getCsvSharedEuTaxonomyValuesMapping(2),
      ...getCsvCompanyMapping<EuTaxonomyDataForFinancials>(),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForFinancials>>(
        `Trading portfolio & on demand interbank loans`,
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
        `Green Asset Ratio`,
        (row) => row.t.greenAssetRatio,
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
