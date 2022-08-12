import { faker } from "@faker-js/faker";
import {
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsAttestationEnum,
  EuTaxonomyDataForFinancialsReportingObligationEnum,
  EuTaxonomyDataForFinancialsFinancialServicesTypeEnum,
} from "../../../build/clients/backend/api";

import { convertToPercentageString, getAttestation, getCompanyType } from "./CsvUtils";
import { FixtureData } from "./GenerateFakeFixtures";
import { getCsvCompanyMapping } from "./CompanyFixtures";
const { parse } = require("json2csv");

const maxEuro = 1000000;
const minEuro = 50000;
const resolution = 0.0001;

export function generateEuTaxonomyDataForFinancials(): EuTaxonomyDataForFinancials {
  const attestation = faker.helpers.arrayElement(Object.values(EuTaxonomyDataForFinancialsAttestationEnum));
  const reportingObligation = faker.helpers.arrayElement(
    Object.values(EuTaxonomyDataForFinancialsReportingObligationEnum)
  );
  const financialServicesType = faker.helpers.arrayElement(
    Object.values(EuTaxonomyDataForFinancialsFinancialServicesTypeEnum)
  );
  const taxonomyEligibleEconomicActivity = faker.datatype.float({ min: 0, max: 1, precision: resolution });
  const eligibleDerivatives = faker.datatype.float({ min: 0, max: 1, precision: resolution });
  const banksAndIssuers = faker.datatype.float({ min: 0, max: 1, precision: resolution });
  const nonNfrd = faker.datatype.float({ min: 0, max: 1, precision: resolution });

  let tradingPortfolioAndInterbankLoans = undefined;
  let interbankLoans = undefined;
  let tradingPortfolio = undefined;
  let taxonomyEligibleNonLifeInsuranceActivities = undefined;
  if (financialServicesType == "CreditInstitution") {
    const singleOrDualField = Math.random();
    if (singleOrDualField < 0.5) {
      tradingPortfolioAndInterbankLoans = faker.datatype.float({ min: 0, max: 1, precision: resolution });
    } else {
      interbankLoans = faker.datatype.float({ min: 0, max: 1, precision: resolution });
      tradingPortfolio = faker.datatype.float({ min: 0, max: 1, precision: resolution });
    }
  } else if (financialServicesType == "InsuranceOrReinsurance") {
    taxonomyEligibleNonLifeInsuranceActivities = faker.datatype.float({ min: 0, max: 1, precision: resolution });
  }
  return {
    reportingObligation: reportingObligation,
    attestation: attestation,
    financialServicesType: financialServicesType,
    eligibilityKpis: {
      banksAndIssuers: banksAndIssuers,
      derivatives: eligibleDerivatives,
      investmentNonNfrd: nonNfrd,
      taxonomyEligibleActivity: taxonomyEligibleEconomicActivity,
    },
    creditInstitutionKpis: {
      interbankLoans: interbankLoans,
      tradingPortfolio: tradingPortfolio,
      tradingPortfolioAndInterbankLoans: tradingPortfolioAndInterbankLoans,
    },
    insuranceKpis: {
      taxonomyEligibleNonLifeInsuranceActivities: taxonomyEligibleNonLifeInsuranceActivities,
    },
  };
}

export function generateCSVDataForFinancials(
  companyInformationWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>
) {
  const options = {
    fields: [
      ...getCsvCompanyMapping<EuTaxonomyDataForFinancials>(),
      {
        label: "Exposures to taxonomy-eligible economic activities",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.eligibilityKpis?.taxonomyEligibleActivity),
      },
      {
        label: "Exposures to derivatives",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.eligibilityKpis?.derivatives),
      },
      {
        label: "Exposures to central governments, central banks, supranational issuers",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.eligibilityKpis?.banksAndIssuers),
      },
      {
        label: "Exposures to non-NFRD entities",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) =>
          convertToPercentageString(row.t.eligibilityKpis?.investmentNonNfrd),
      },
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
      { label: "NFRD mandatory", value: (row: FixtureData<EuTaxonomyDataForFinancials>) => row.t.reportingObligation },
      {
        label: "FS - company type",
        value: (row: FixtureData<EuTaxonomyDataForFinancials>) => getCompanyType(row.t.financialServicesType),
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
  return parse(companyInformationWithEuTaxonomyDataForFinancials, options);
}
