import {
  DataPointBigDecimal,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDetailsPerCashFlowType,
} from "@clients/backend";
import { ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { convertToPercentageString, decimalSeparatorConverter } from "@e2e/fixtures/CsvUtils";
import { getCsvCompanyMapping } from "@e2e/fixtures/CompanyFixtures";
import { generateDatapoint, getCsvDataPointMapping } from "@e2e/fixtures/common/DataPointFixtures";
import {
  generateEuTaxonomyWithBaseFields,
  getCsvSharedEuTaxonomyValuesMapping,
} from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValuesFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { parse } from "json2csv";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates fake data for a single cash-flow type for the eutaxonomy-non-financials framework
 * @param reports a list of reports that can be referenced
 * @returns the generated data
 */
export function generateEuTaxonomyPerCashflowType(reports: ReferencedDocuments): EuTaxonomyDetailsPerCashFlowType {
  return {
    totalAmount: valueOrUndefined(generateDatapoint(valueOrUndefined(randomEuroValue()), reports)),
    alignedAmount: valueOrUndefined(generateDatapoint(valueOrUndefined(randomEuroValue()), reports)),
    alignedPercentage: valueOrUndefined(generateDatapoint(valueOrUndefined(randomPercentageValue()), reports)),
    eligibleAmount: valueOrUndefined(generateDatapoint(valueOrUndefined(randomEuroValue()), reports)),
    eligiblePercentage: valueOrUndefined(generateDatapoint(valueOrUndefined(randomPercentageValue()), reports)),
  };
}

/**
 * Generates a single fixture for the eutaxonomy-non-financials framework
 * @returns the generated fixture
 */
export function generateEuTaxonomyDataForNonFinancials(): EuTaxonomyDataForNonFinancials {
  const returnBase: EuTaxonomyDataForNonFinancials = generateEuTaxonomyWithBaseFields();

  returnBase.opex = generateEuTaxonomyPerCashflowType(assertDefined(returnBase.referencedReports));
  returnBase.capex = generateEuTaxonomyPerCashflowType(assertDefined(returnBase.referencedReports));
  returnBase.revenue = generateEuTaxonomyPerCashflowType(assertDefined(returnBase.referencedReports));

  return returnBase;
}

/**
 * Exports the eutaxonomy-non-financials data to CSV
 * @param companyInformationWithEuTaxonomyDataForNonFinancials the data to export
 * @returns the generated CSV as a string
 */
export function generateCSVDataForNonFinancials(
  companyInformationWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>
): string {
  const options = {
    fields: [
      ...getCsvCompanyMapping<EuTaxonomyDataForNonFinancials>(),
      ...getCsvSharedEuTaxonomyValuesMapping(1),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total Revenue`,
        (row): DataPointBigDecimal | undefined => row.t.revenue?.totalAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total CapEx`,
        (row): DataPointBigDecimal | undefined => row.t.capex?.totalAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total OpEx`,
        (row): DataPointBigDecimal | undefined => row.t.opex?.totalAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible Revenue (%)`,
        (row): DataPointBigDecimal | undefined => row.t.revenue?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible CapEx (%)`,
        (row): DataPointBigDecimal | undefined => row.t.capex?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible OpEx (%)`,
        (row): DataPointBigDecimal | undefined => row.t.opex?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned Revenue (%)`,
        (row): DataPointBigDecimal | undefined => row.t.revenue?.alignedPercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned CapEx (%)`,
        (row): DataPointBigDecimal | undefined => row.t.capex?.alignedPercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned OpEx (%)`,
        (row): DataPointBigDecimal | undefined => row.t.opex?.alignedPercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible Revenue`,
        (row): DataPointBigDecimal | undefined => row.t.revenue?.eligibleAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible CapEx`,
        (row): DataPointBigDecimal | undefined => row.t.capex?.eligibleAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible OpEx`,
        (row): DataPointBigDecimal | undefined => row.t.opex?.eligibleAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned Revenue`,
        (row): DataPointBigDecimal | undefined => row.t.revenue?.alignedAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned CapEx`,
        (row): DataPointBigDecimal | undefined => row.t.capex?.alignedAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned OpEx`,
        (row): DataPointBigDecimal | undefined => row.t.opex?.alignedAmount,
        decimalSeparatorConverter(1000000)
      ),
    ],
    delimiter: ";",
  };
  return parse(companyInformationWithEuTaxonomyDataForNonFinancials, options);
}
