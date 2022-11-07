import {
  DataPointBigDecimal,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDetailsPerCashFlowType,
} from "@clients/backend";
import { FixtureData, ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { convertToPercentageString, decimalSeparatorConverter } from "@e2e/fixtures/CsvUtils";
import { getCsvCompanyMapping } from "@e2e/fixtures/CompanyFixtures";
import { generateDatapointOrNotReportedAtRandom, getCsvDataPointMapping } from "@e2e/fixtures/common/DataPointFixtures";
import { getCsvSharedEuTaxonomyValuesMapping, populateSharedValues } from "../EuTaxonomySharedValuesFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
// eslint-disable-next-line @typescript-eslint/no-var-requires,@typescript-eslint/no-unsafe-assignment
const { parse } = require("json2csv");

export function generateEuTaxonomyPerCashflowType(reports: ReferencedReports): EuTaxonomyDetailsPerCashFlowType {
  const total = randomEuroValue();
  const eligiblePercentage = randomPercentageValue();
  const alignedPercentage = randomPercentageValue();

  return {
    totalAmount: generateDatapointOrNotReportedAtRandom(total, reports),
    alignedPercentage: generateDatapointOrNotReportedAtRandom(alignedPercentage, reports),
    eligiblePercentage: generateDatapointOrNotReportedAtRandom(eligiblePercentage, reports),
  };
}

export function generateEuTaxonomyDataForNonFinancials(): EuTaxonomyDataForNonFinancials {
  const returnBase: EuTaxonomyDataForNonFinancials = {};
  populateSharedValues(returnBase);

  returnBase.opex = generateEuTaxonomyPerCashflowType(returnBase.referencedReports!);
  returnBase.capex = generateEuTaxonomyPerCashflowType(returnBase.referencedReports!);
  returnBase.revenue = generateEuTaxonomyPerCashflowType(returnBase.referencedReports!);

  return returnBase;
}

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
        `Eligible Revenue`,
        (row): DataPointBigDecimal | undefined => row.t.revenue?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible CapEx`,
        (row): DataPointBigDecimal | undefined => row.t.capex?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible OpEx`,
        (row): DataPointBigDecimal | undefined => row.t.opex?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned Revenue`,
        (row): DataPointBigDecimal | undefined => row.t.revenue?.alignedPercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned CapEx`,
        (row): DataPointBigDecimal | undefined => row.t.capex?.alignedPercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned OpEx`,
        (row): DataPointBigDecimal | undefined => row.t.opex?.alignedPercentage,
        convertToPercentageString
      ),
    ],
    delimiter: ";",
  };
  // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-return
  return parse(companyInformationWithEuTaxonomyDataForNonFinancials, options);
}
