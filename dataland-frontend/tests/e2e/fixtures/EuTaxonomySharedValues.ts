import { FixtureData } from "./GenerateFakeFixtures";
import { EuTaxonomyDataForFinancials, EuTaxonomyDataForNonFinancials } from "../../../build/clients/backend";
import { humanizeString } from "../../../src/utils/StringHumanizer";
import { getAssurance, getFiscalYearDeviation, humaniseOrUndefined } from "./CsvUtils";
import { getCsvDataSourceMapping } from "./DataSourceFixtures";
import { generateReferencedReports } from "./DataPointFixtures";
import { randomYesNoNaUndefined, randomYesNoUndefined } from "./YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { randomDateOrUndefined } from "./DateFixtures";
import { randomNumberOrUndefined } from "./NumberFixtures";

export function populateSharedValues(input: EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials) {
  input.referencedReports = generateReferencedReports();
  input.fiscalYearDeviation = randomYesNoUndefined();
  input.fiscalYearEnd = randomDateOrUndefined();
  input.assurance = generateAssuranceData(input.referencedReports);
  input.scopeOfEntities = randomYesNoNaUndefined();
  input.reportingObligation = randomYesNoUndefined();
  input.numberOfEmployees = randomNumberOrUndefined();
  input.activityLevelReporting = randomYesNoUndefined();
}

function getCsvReportMapping(reportName: string) {
  return [
    {
      label: humanizeString(reportName),
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        row.t.referencedReports !== undefined &&
        row.t.referencedReports !== null &&
        row.t.referencedReports[reportName] !== undefined &&
        row.t.referencedReports[reportName] !== null
          ? row.t.referencedReports[reportName].reference
          : "",
    },
    {
      label: `Group Level ${humanizeString(reportName)}`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        row.t.referencedReports !== undefined &&
        row.t.referencedReports !== null &&
        row.t.referencedReports[reportName] !== undefined &&
        row.t.referencedReports[reportName] !== null
          ? humaniseOrUndefined(row.t.referencedReports[reportName].isGroupLevel)
          : "",
    },
  ];
}

export function getCsvSharedEuTaxonomyValuesMapping(isfs: number) {
  return [
    {
      label: "IS/FS",
      value: () => isfs.toString(),
    },
    {
      label: "Fiscal Year",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        getFiscalYearDeviation(row.t.fiscalYearDeviation),
    },
    {
      label: "Fiscal Year End",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) => row.t.fiscalYearEnd,
    },
    ...getCsvReportMapping("AnnualReport"),
    ...getCsvReportMapping("SustainabilityReport"),
    ...getCsvReportMapping("IntegratedReport"),
    {
      label: "Scope of Entities",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        humaniseOrUndefined(row.t.scopeOfEntities),
    },
    {
      label: "EU Taxonomy Activity Level Reporting",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        humaniseOrUndefined(row.t.activityLevelReporting),
    },
    {
      label: "NFRD mandatory",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        humaniseOrUndefined(row.t.reportingObligation),
    },
    {
      label: "Number Of Employees",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        humaniseOrUndefined(row.t.reportingObligation),
    },
    {
      label: "Assurance",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        getAssurance(row.t.assurance?.assurance),
    },
    {
      label: "Assurance Provider",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        humaniseOrUndefined(row.t.assurance?.provider),
    },
    ...getCsvDataSourceMapping<FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>>(
      "Assurance",
      (row) => row.t.assurance?.dataSource
    ),
  ];
}
