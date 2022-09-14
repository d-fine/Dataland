import { faker } from "@faker-js/faker";
import {
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForNonFinancialsAttestationEnum,
  EuTaxonomyDataForNonFinancialsReportingObligationEnum,
} from "../../../build/clients/backend";
import { FixtureData } from "./GenerateFakeFixtures";
import { convertToPercentageString, decimalSeparatorConverter, getAttestation } from "./CsvUtils";

import { getCsvCompanyMapping } from "./CompanyFixtures";
const { parse } = require("json2csv");

const maxEuro = 1000000;
const minEuro = 50000;
const resolution = 0.0001;

export function generateEuTaxonomyDataForNonFinancials(): EuTaxonomyDataForNonFinancials {
  const attestation = faker.helpers.arrayElement(Object.values(EuTaxonomyDataForNonFinancialsAttestationEnum));
  const reportingObligation = faker.helpers.arrayElement(
    Object.values(EuTaxonomyDataForNonFinancialsReportingObligationEnum)
  );
  const capexTotal = faker.datatype.float({ min: minEuro, max: maxEuro });
  const capexEligible = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const capexAligned = faker.datatype.float({
    min: 0,
    max: capexEligible,
    precision: resolution,
  });
  const opexTotal = faker.datatype.float({ min: minEuro, max: maxEuro });
  const opexEligible = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const opexAligned = faker.datatype.float({
    min: 0,
    max: opexEligible,
    precision: resolution,
  });
  const revenueTotal = faker.datatype.float({ min: minEuro, max: maxEuro });
  const revenueEligible = faker.datatype.float({
    min: 0,
    max: 1,
    precision: resolution,
  });
  const revenueAligned = faker.datatype.float({
    min: 0,
    max: revenueEligible,
    precision: resolution,
  });

  return {
    capex: {
      totalAmount: capexTotal,
      alignedPercentage: capexAligned,
      eligiblePercentage: capexEligible,
    },
    opex: {
      totalAmount: opexTotal,
      alignedPercentage: opexAligned,
      eligiblePercentage: opexEligible,
    },
    revenue: {
      totalAmount: revenueTotal,
      alignedPercentage: revenueAligned,
      eligiblePercentage: revenueEligible,
    },
    reportingObligation: reportingObligation,
    attestation: attestation,
  };
}

export function generateCSVDataForNonFinancials(
  companyInformationWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>
) {
  const options = {
    fields: [
      ...getCsvCompanyMapping<EuTaxonomyDataForNonFinancials>(),
      {
        label: "Total Revenue EURmm",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          decimalSeparatorConverter(row.t.revenue?.totalAmount),
      },
      {
        label: "Total CapEx EURmm",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          decimalSeparatorConverter(row.t.capex?.totalAmount),
      },
      {
        label: "Total OpEx EURmm",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) => decimalSeparatorConverter(row.t.opex?.totalAmount),
      },
      {
        label: "Eligible Revenue",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          convertToPercentageString(row.t.revenue?.eligiblePercentage),
      },
      {
        label: "Eligible CapEx",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          convertToPercentageString(row.t.capex?.eligiblePercentage),
      },
      {
        label: "Eligible OpEx",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          convertToPercentageString(row.t.opex?.eligiblePercentage),
      },
      {
        label: "Aligned Revenue",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          convertToPercentageString(row.t.revenue?.alignedPercentage),
      },
      {
        label: "Aligned CapEx",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          convertToPercentageString(row.t.capex?.alignedPercentage),
      },
      {
        label: "Aligned OpEx",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) =>
          convertToPercentageString(row.t.opex?.alignedPercentage),
      },
      { label: "IS/FS", value: "companyType", default: "IS" },
      {
        label: "NFRD mandatory",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) => row.t.reportingObligation,
      },
      {
        label: "Assurance",
        value: (row: FixtureData<EuTaxonomyDataForNonFinancials>) => {
          return getAttestation(row.t.attestation);
        },
      },
    ],
    delimiter: ";",
  };
  return parse(companyInformationWithEuTaxonomyDataForNonFinancials, options);
}
