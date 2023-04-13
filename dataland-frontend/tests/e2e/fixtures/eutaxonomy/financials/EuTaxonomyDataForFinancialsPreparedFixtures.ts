import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { EuTaxonomyDataForFinancials, QualityOptions } from "@clients/backend";
import {
  generateEuTaxonomyDataForFinancials,
  generateEuTaxonomyDataForFinancialsWithTypes,
} from "./EuTaxonomyDataForFinancialsFixtures";
import { randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateDatapoint } from "@e2e/fixtures/common/DataPointFixtures";

type generatorFunction = (input: FixtureData<EuTaxonomyDataForFinancials>) => FixtureData<EuTaxonomyDataForFinancials>;

/**
 * Generates prepared fixtures for the eutaxonomy-financials framework
 *
 * @returns the generated fixtures
 */
export function generateEuTaxonomyForFinancialsPreparedFixtures(): Array<FixtureData<EuTaxonomyDataForFinancials>> {
  const creationFunctions: Array<generatorFunction> = [
    createAssetManagementAndInsuranceCompany,
    createCreditInstitutionDualFieldSubmission,
    createCreditInstitutionSingleFieldSubmission,
    createInsuranceCompany,
    createAssetManagementCompany,
    createAllValuesCompany,
    createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(0.26),
    createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(0.29),
    createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(0.292),
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

/**
 * Creates a fixture of a credit institution that uses dual field submission
 *
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCreditInstitutionDualFieldSubmission(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "credit-institution-dual-field-submission";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["CreditInstitution"]);
  input.t.creditInstitutionKpis = {
    interbankLoans: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    tradingPortfolio: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    greenAssetRatio: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  return input;
}

/**
 * Creates a fixture of a credit institution that uses single field submission
 *
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCreditInstitutionSingleFieldSubmission(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "credit-institution-single-field-submission";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["CreditInstitution"]);
  input.t.creditInstitutionKpis = {
    tradingPortfolioAndInterbankLoans: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
    greenAssetRatio: generateDatapoint(randomPercentageValue(), input.t.referencedReports!),
  };
  return input;
}

/**
 * Creates a fixture of a company that is only an insurance company
 *
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createInsuranceCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "insurance-company";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["InsuranceOrReinsurance"]);
  return input;
}

/**
 * Creates a fixture of a company that is only an asset management company
 *
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createAssetManagementCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "asset-management-company";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["AssetManagement"]);
  return input;
}

/**
 * Creates a fixture of a company that is an asset management and insurance company
 *
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createAssetManagementAndInsuranceCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = "asset-management-insurance-company";
  input.t = generateEuTaxonomyDataForFinancialsWithTypes(["AssetManagement", "InsuranceOrReinsurance"]);
  return input;
}

/**
 * Creates a fixture of a company that has values for every field (i.e. no undefined/missing datapoints)
 *
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
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

/**
 * Higher order function which returns a function that creates a fixture of a credit institution, but sets the value
 * "taxonomyEligibleActivity" to the value of the input that is passed to the higher order function.
 *
 * @param eligibleActivityValue The value for the field "eligible activity".
 * @returns a generator function that creates a dataset with the "eligbile activity" value set accordingly
 */
function createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(
  eligibleActivityValue: number
): (input: FixtureData<EuTaxonomyDataForFinancials>) => FixtureData<EuTaxonomyDataForFinancials> {
  return (input) => {
    input.companyInformation.companyName = "eligible-activity-Point-" + eligibleActivityValue.toString();
    input.t = generateEuTaxonomyDataForFinancialsWithTypes(["CreditInstitution"]);
    input.t.eligibilityKpis = {
      CreditInstitution: {
        taxonomyEligibleActivity: {
          value: eligibleActivityValue,
          quality: QualityOptions.Reported,
        },
      },
    };
    return input;
  };
}
