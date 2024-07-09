import { generateFixtureDataset } from '@e2e/fixtures/FixtureUtils';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EuTaxonomyDataForFinancials, QualityOptions } from '@clients/backend';
import { EuFinancialsGenerator, generateEuTaxonomyDataForFinancials } from './EuTaxonomyDataForFinancialsFixtures';
import { generatePercentageValue } from '@e2e/fixtures/common/NumberFixtures';

type generatorFunction = (input: FixtureData<EuTaxonomyDataForFinancials>) => FixtureData<EuTaxonomyDataForFinancials>;

/**
 * Generates prepared fixtures for the eutaxonomy-financials framework
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
    createCompanyWithBrokenFileReference,
    createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(26),
    createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(29),
    createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(29.2),
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
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCreditInstitutionDualFieldSubmission(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  const dataGenerator = new EuFinancialsGenerator(0);
  dataGenerator.reports = input.t.referencedReports!;
  input.companyInformation.companyName = 'credit-institution-dual-field-submission';
  input.t = dataGenerator.generateEuTaxonomyDataForFinancialsWithTypes(['CreditInstitution']);
  input.t.creditInstitutionKpis = {
    interbankLoansInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
    tradingPortfolioInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
    greenAssetRatioInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
  };
  return input;
}

/**
 * Creates a fixture of a credit institution that uses single field submission
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCreditInstitutionSingleFieldSubmission(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  const dataGenerator = new EuFinancialsGenerator(0);
  dataGenerator.reports = input.t.referencedReports!;
  input.companyInformation.companyName = 'credit-institution-single-field-submission';
  input.t = dataGenerator.generateEuTaxonomyDataForFinancialsWithTypes(['CreditInstitution']);
  input.t.creditInstitutionKpis = {
    tradingPortfolioAndInterbankLoansInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
    greenAssetRatioInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
  };
  return input;
}

/**
 * Creates a fixture of a company that is only an insurance company
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createInsuranceCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = 'insurance-company';
  input.t = new EuFinancialsGenerator().generateEuTaxonomyDataForFinancialsWithTypes(['InsuranceOrReinsurance']);
  return input;
}

/**
 * Creates a fixture of a company that is only an asset management company
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createAssetManagementCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = 'asset-management-company';
  input.t = new EuFinancialsGenerator().generateEuTaxonomyDataForFinancialsWithTypes(['AssetManagement']);
  return input;
}

/**
 * Creates a fixture of a company that is an asset management and insurance company
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createAssetManagementAndInsuranceCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input.companyInformation.companyName = 'asset-management-insurance-company';
  input.t = new EuFinancialsGenerator().generateEuTaxonomyDataForFinancialsWithTypes([
    'AssetManagement',
    'InsuranceOrReinsurance',
  ]);
  return input;
}

/**
 * Creates a fixture of a company that has values for every field (i.e. no undefined/missing datapoints)
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createAllValuesCompany(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  const dataGenerator = new EuFinancialsGenerator(0);
  input.companyInformation.companyName = 'company-for-all-types';
  input.t = dataGenerator.generateEuTaxonomyDataForFinancialsWithTypes([
    'InvestmentFirm',
    'AssetManagement',
    'InsuranceOrReinsurance',
    'CreditInstitution',
  ]);
  //Overwrite the section for Credit Institution since the tests require dual and single field submission at once
  input.t.creditInstitutionKpis = {
    interbankLoansInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
    tradingPortfolioInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
    tradingPortfolioAndInterbankLoansInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
    greenAssetRatioInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
  };
  return input;
}
/**
 * Creates a fixture of a company that is an asset management and insurance company
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyWithBrokenFileReference(
  input: FixtureData<EuTaxonomyDataForFinancials>
): FixtureData<EuTaxonomyDataForFinancials> {
  input = createAllValuesCompany(input);
  input.companyInformation.companyName = 'TestForIncompleteReferencedReport';
  input.t.referencedReports = null;
  return input;
}

/**
 * Higher order function which returns a function that creates a fixture of a credit institution, but sets the value
 * "taxonomyEligibleActivity" to the value of the input that is passed to the higher order function.
 * @param eligibleActivityValue The value for the field "eligible activity".
 * @returns a generator function that creates a dataset with the "eligbile activity" value set accordingly
 */
function createGeneratorForCreditInstitutionWithEligibleActivitySetToValue(
  eligibleActivityValue: number
): (input: FixtureData<EuTaxonomyDataForFinancials>) => FixtureData<EuTaxonomyDataForFinancials> {
  return (input) => {
    input.companyInformation.companyName = 'eligible-activity-Point-' + eligibleActivityValue.toString();
    input.t = new EuFinancialsGenerator().generateEuTaxonomyDataForFinancialsWithTypes(['CreditInstitution']);
    input.t.eligibilityKpis = {
      CreditInstitution: {
        taxonomyEligibleActivityInPercent: {
          value: eligibleActivityValue,
          quality: QualityOptions.Reported,
        },
      },
    };
    return input;
  };
}
