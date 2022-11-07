import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { EuTaxonomyDataForFinancials } from "@clients/backend";
import {
  generateEuTaxonomyDataForFinancials,
  generateEuTaxonomyDataForFinancialsWithTypes,
} from "./EuTaxonomyDataForFinancialsFixtures";
import { randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateDatapoint } from "@e2e/fixtures/common/DataPointFixtures";

type generatorFunction = (input: FixtureData<EuTaxonomyDataForFinancials>) => FixtureData<EuTaxonomyDataForFinancials>;

export function generateEuTaxonomyForFinancialsPreparedFixtures(): Array<FixtureData<EuTaxonomyDataForFinancials>> {
  const creationFunctions: Array<generatorFunction> = [
    createAssetManagementAndInsuranceCompany,
    createCreditInstitutionDualFieldSubmission,
    createCreditInstitutionSingleFieldSubmission,
    createInsuranceCompany,
    createAssetManagementCompany,
    createAllValuesCompany,
  ];
  const fixtureBase = generateFixtureDataset<EuTaxonomyDataForFinancials>(
    generateEuTaxonomyDataForFinancials,
    creationFunctions.length
  );
  const preparedFixtures = [];
  for (let i = 0; i < creationFunctions.length; i++) {
    preparedFixtures.push(creationFunctions[i](fixtureBase[i]));
  }
  return preparedFixtures;
}

function createCreditInstitutionDualFieldSubmission(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "credit-institution-dual-field-submission";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["CreditInstitution"]);
  input.t.creditInstitutionKpis = {
    interbankLoans: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    tradingPortfolio: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  return input;
}

function createCreditInstitutionSingleFieldSubmission(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "credit-institution-single-field-submission";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["CreditInstitution"]);
  input.t.creditInstitutionKpis = {
    tradingPortfolioAndInterbankLoans: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  return input;
}

function createInsuranceCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "insurance-company";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["InsuranceOrReinsurance"]);
  return input;
}

function createAssetManagementCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "asset-management-company";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["AssetManagement"]);
  return input;
}

function createAssetManagementAndInsuranceCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "asset-management-insurance-company";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["AssetManagement", "InsuranceOrReinsurance"]);
  return input;
}

function createAllValuesCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "company-for-all-types";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes([
    "CreditInstitution",
    "AssetManagement",
    "InsuranceOrReinsurance",
    "InvestmentFirm",
  ]);
  input.t.creditInstitutionKpis = {
    interbankLoans: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    tradingPortfolio: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    tradingPortfolioAndInterbankLoans: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    greenAssetRatio: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  input.t.insuranceKpis = {
    taxonomyEligibleNonLifeInsuranceActivities: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  input.t.investmentFirmKpis = {
    greenAssetRatio: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  input.t.eligibilityKpis = {
    CreditInstitution: {
      banksAndIssuers: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      derivatives: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      investmentNonNfrd: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyNonEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    },
    AssetManagement: {
      banksAndIssuers: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      derivatives: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      investmentNonNfrd: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyNonEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    },
    InsuranceOrReinsurance: {
      banksAndIssuers: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      derivatives: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      investmentNonNfrd: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyNonEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    },
    InvestmentFirm: {
      banksAndIssuers: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      derivatives: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      investmentNonNfrd: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
      taxonomyNonEligibleActivity: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    },
  };
  return input;
}
