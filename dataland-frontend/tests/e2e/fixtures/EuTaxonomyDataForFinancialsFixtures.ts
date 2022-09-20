import { faker } from "@faker-js/faker";
import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
} from "@clients/backend";

import { convertToPercentageString, getAttestation, getCompanyType } from "./CsvUtils";
import { FixtureData } from "./GenerateFakeFixtures";
import { generateDatapointOrNotReportedAtRandom } from "./DataPointFixtures";
import { getCsvCompanyMapping } from "./CompanyFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { randomYesNo } from "./YesNoFixtures";
const { parse } = require("json2csv");

const resolution = 0.0001;

export function generateEuTaxonomyDataForFinancials(): EuTaxonomyDataForFinancials {
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

  const eligibilityKpis = Object.fromEntries(financialServicesTypes.map((it) => [it, generateEligibilityKpis()]));

  return {
    reportingObligation: randomYesNo(),
    activityLevelReporting: randomYesNo(),
    assurance: generateAssuranceData(),
    greenAssetRatio: generateDatapointOrNotReportedAtRandom(greenAssetRatio),
    financialServicesTypes: financialServicesTypes,
    eligibilityKpis: eligibilityKpis,
    creditInstitutionKpis: {
      interbankLoans: generateDatapointOrNotReportedAtRandom(interbankLoans),
      tradingPortfolio: generateDatapointOrNotReportedAtRandom(tradingPortfolio),
      tradingPortfolioAndInterbankLoans: generateDatapointOrNotReportedAtRandom(tradingPortfolioAndInterbankLoans),
    },
    insuranceKpis: {
      taxonomyEligibleNonLifeInsuranceActivities: generateDatapointOrNotReportedAtRandom(
        taxonomyEligibleNonLifeInsuranceActivities
      ),
    },
  };
}

export function generateEligibilityKpis(): EligibilityKpis {
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
    banksAndIssuers: generateDatapointOrNotReportedAtRandom(banksAndIssuers),
    derivatives: generateDatapointOrNotReportedAtRandom(eligibleDerivatives),
    investmentNonNfrd: generateDatapointOrNotReportedAtRandom(nonNfrd),
    taxonomyEligibleActivity: generateDatapointOrNotReportedAtRandom(taxonomyEligibleEconomicActivity),
    taxonomyNonEligibleActivity: generateDatapointOrNotReportedAtRandom(taxonomyNonEligibleEconomicActivity),
  };
}

export function getCsvEligibilityKpiMapping(type: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum) {
  /*return [
    {
      label: `Exposures to taxonomy-eligible economic activities ${getCompanyType(type)}`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
        convertToPercentageString(row.t.eligibilityKpis![type]?.taxonomyEligibleActivity),
    },
    {
      label: `Exposures to derivatives ${getCompanyType(type)}`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
        convertToPercentageString(row.t.eligibilityKpis![type]?.derivatives),
    },
    {
      label: `Exposures to central governments, central banks, supranational issuers ${getCompanyType(type)}`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
        convertToPercentageString(row.t.eligibilityKpis![type]?.banksAndIssuers),
    },
    {
      label: `Exposures to non-NFRD entities ${getCompanyType(type)}`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
        convertToPercentageString(row.t.eligibilityKpis![type]?.investmentNonNfrd),
    },
  ];*/
  return [];
}

export function generateCSVDataForFinancials(
  companyInformationWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>
) {
  return "";
  /*const options = {
    fields: [
      ...getCsvCompanyMapping<EuTaxonomyDataForFinancials>(),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution),
      ...getCsvEligibilityKpiMapping(EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement),
      {
        label: "Trading portfolio & on demand interbank loans",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.creditInstitutionKpis?.tradingPortfolioAndInterbankLoans),
      },
      {
        label: "Trading portfolio",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.creditInstitutionKpis?.tradingPortfolio),
      },
      {
        label: "On-demand interbank loans",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.creditInstitutionKpis?.interbankLoans),
      },
      {
        label: "Taxonomy-eligible non-life insurance economic activities",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivities),
      },
      { label: "IS/FS", value: "companyType", default: "FS" },
      {
        label: "NFRD mandatory",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) => row.t.reportingObligation,
      },
      {
        label: "FS - company type",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          row.t.financialServicesTypes.map((it) => getCompanyType(it)).join(", "),
      },
      {
        label: "Assurance",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) => {
          return getAttestation(row.t.attestation);
        },
      },
    ],
    delimiter: ";",
  };
  return parse(companyInformationWithEuTaxonomyDataForFinancials, options);*/
}
