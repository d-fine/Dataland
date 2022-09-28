import { faker } from "@faker-js/faker";
import { EuTaxonomyDataForNonFinancials, EuTaxonomyDetailsPerCashFlowType } from "@clients/backend";
import { FixtureData, ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { convertToPercentageString, decimalSeparatorConverter } from "@e2e/fixtures/CsvUtils";

import { getCsvCompanyMapping } from "@e2e/fixtures/CompanyFixtures";
import { generateDatapointOrNotReportedAtRandom, getCsvDataPointMapping } from "@e2e/fixtures/common/DataPointFixtures";
import {
  getCsvSharedEuTaxonomyValuesMapping,
  populateSharedValues,
} from "@e2e/fixtures/eutaxonomy/EuTaxonomySharedValues";
import { randomPercentageValue } from "../../common/NumberFixtures";
const { parse } = require("json2csv");

const maxEuro = 1000000;
const minEuro = 50000;

export function generateEuTaxonomyPerCashflowType(reports: ReferencedReports): EuTaxonomyDetailsPerCashFlowType {
  const total = faker.datatype.float({ min: minEuro, max: maxEuro });
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

  returnBase.opex = generateEuTaxonomyPerCashflowType(returnBase.referencedReports!!);
  returnBase.capex = generateEuTaxonomyPerCashflowType(returnBase.referencedReports!!);
  returnBase.revenue = generateEuTaxonomyPerCashflowType(returnBase.referencedReports!!);

  return returnBase;
}

export function generateCSVDataForNonFinancials(
  companyInformationWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>
) {
  const options = {
    fields: [
      ...getCsvCompanyMapping<EuTaxonomyDataForNonFinancials>(),
      ...getCsvSharedEuTaxonomyValuesMapping(1),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total Revenue`,
        (row) => row.t.revenue?.totalAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total CapEx`,
        (row) => row.t.capex?.totalAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total OpEx`,
        (row) => row.t.opex?.totalAmount,
        decimalSeparatorConverter(1000000)
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible Revenue`,
        (row) => row.t.revenue?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible CapEx`,
        (row) => row.t.capex?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Eligible OpEx`,
        (row) => row.t.opex?.eligiblePercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned Revenue`,
        (row) => row.t.revenue?.alignedPercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned CapEx`,
        (row) => row.t.capex?.alignedPercentage,
        convertToPercentageString
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Aligned OpEx`,
        (row) => row.t.opex?.alignedPercentage,
        convertToPercentageString
      ),
    ],
    delimiter: ";",
  };
  return parse(companyInformationWithEuTaxonomyDataForNonFinancials, options);
}
