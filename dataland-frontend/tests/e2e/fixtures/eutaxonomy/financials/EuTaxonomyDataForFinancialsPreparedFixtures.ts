import { FixtureData, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { EuTaxonomyDataForFinancials, EuTaxonomyDataForNonFinancials } from "@clients/backend";
import {
  generateEuTaxonomyDataForFinancials,
  generateEuTaxonomyDataForFinancialsWithTypes,
} from "./EuTaxonomyDataForFinancialsFixtures";
import { randomPercentageValue } from "../../common/NumberFixtures";
import { generateDatapoint } from "../../common/DataPointFixtures";

type generatorFunction = (
  input: FixtureData<EuTaxonomyDataForNonFinancials>
) => FixtureData<EuTaxonomyDataForNonFinancials>;

export function generateEuTaxonomyForFinancialsPreparedFixtures(): Array<FixtureData<EuTaxonomyDataForFinancials>> {
  const creationFunctions: Array<generatorFunction> = [
    createAssetManagementAndCreditInstitutionCompany,
    createCreditInstitutionDualFieldSubmission,
    createCreditInstitutionSingleFieldSubmission,
    createInsuranceCompany,
    createAssetManagementCompany,
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
): FixtureData<EuTaxonomyDataForNonFinancials> {
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
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "credit-institution-single-field-submission";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["CreditInstitution"]);
  input.t.creditInstitutionKpis = {
    tradingPortfolioAndInterbankLoans: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  return input;
}

function createInsuranceCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "insurance-company";
  input.t = input.t = generateEuTaxonomyDataForFinancialsWithTypes(["InsuranceOrReinsurance"]);
  return input;
}

function createAssetManagementCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "asset-management-company";
  input.t = input.t = generateEuTaxonomyDataForFinancialsWithTypes(["AssetManagement"]);
  return input;
}

function createAssetManagementAndCreditInstitutionCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForNonFinancials> {
  input.companyInformation.companyName = "asset-management-credit-institution";
  input.t = input.t = generateEuTaxonomyDataForFinancialsWithTypes(["AssetManagement", "CreditInstitution"]);
  return input;
}
