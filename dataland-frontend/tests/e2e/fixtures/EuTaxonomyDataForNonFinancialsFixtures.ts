import { faker } from "@faker-js/faker";
import {
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDetailsPerCashFlowType,
} from "@clients/backend";
import { FixtureData } from "./GenerateFakeFixtures";
import { convertToPercentageString, decimalSeparatorConverter } from "./CsvUtils";

import { getCsvCompanyMapping } from "./CompanyFixtures";
import { generateDatapoint, generateDatapointOrNotReportedAtRandom, getCsvDataPointMapping } from "./DataPointFixtures";
import { randomYesNo } from "./YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { getCsvSharedEuTaxonomyValuesMapping, populateSharedValues } from "./EuTaxonomySharedValues";
import { ReferencedReports } from "./Utils";
const { parse } = require("json2csv");

const maxEuro = 1000000;
const minEuro = 50000;
const resolution = 0.0001;

export function generateEuTaxonomyPerCashflowType(reports: ReferencedReports): EuTaxonomyDetailsPerCashFlowType {
  const total = faker.datatype.float({ min: minEuro, max: maxEuro });
  const eligiblePercentage = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const alignedPercentage = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });

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
      ...getCsvSharedEuTaxonomyValuesMapping<EuTaxonomyDataForNonFinancials>(1),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total Revenue`,
        (row) => row.t.revenue?.totalAmount,
        decimalSeparatorConverter
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total CapEx`,
        (row) => row.t.capex?.totalAmount,
        decimalSeparatorConverter
      ),
      ...getCsvDataPointMapping<FixtureData<EuTaxonomyDataForNonFinancials>>(
        `Total OppEx`,
        (row) => row.t.capex?.totalAmount,
        decimalSeparatorConverter
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
