import { FixtureData, DataPoint } from "@e2e/fixtures/FixtureUtils";
import {
  CompanyReport,
  CompanyReportReference,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForNonFinancials,
} from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import { getAssurance, getFiscalYearDeviation, humanizeOrUndefined } from "@e2e/fixtures/CsvUtils";
import { getCsvDataSourceMapping } from "@e2e/fixtures/common/DataSourceFixtures";
import { generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import { randomYesNoNaUndefined, randomYesNoUndefined } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { randomPastDateOrUndefined } from "@e2e/fixtures/common/DateFixtures";
import { randomNumberOrUndefined } from "@e2e/fixtures/common/NumberFixtures";

export function populateSharedValues(input: EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials): void {
  input.referencedReports = generateReferencedReports();
  input.fiscalYearDeviation = randomYesNoUndefined();
  input.fiscalYearEnd = randomPastDateOrUndefined();
  input.assurance = generateAssuranceData(input.referencedReports);
  input.scopeOfEntities = randomYesNoNaUndefined();
  input.reportingObligation = randomYesNoUndefined();
  input.numberOfEmployees = randomNumberOrUndefined(100000);
  input.activityLevelReporting = randomYesNoUndefined();
}

function getReportIfExists(
  row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>,
  reportName: string
): CompanyReport | undefined {
  return row.t.referencedReports !== undefined &&
    row.t.referencedReports !== null &&
    row.t.referencedReports[reportName] !== undefined &&
    row.t.referencedReports[reportName] !== null
    ? row.t.referencedReports[reportName]
    : undefined;
}

function getCsvReportMapping(
  reportName: string
): Array<DataPoint<FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>, string>> {
  return [
    {
      label: humanizeString(reportName),
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        getReportIfExists(row, reportName)?.reference,
    },
    {
      label: `Group Level ${humanizeString(reportName)}`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        humanizeOrUndefined(getReportIfExists(row, reportName)?.isGroupLevel),
    },
    {
      label: `${humanizeString(reportName)} Currency`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        getReportIfExists(row, reportName)?.currency,
    },
    {
      label: `${humanizeString(reportName)} Date`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined => {
        const reportDate = getReportIfExists(row, reportName)?.reportDate;
        return reportDate !== undefined ? new Date(reportDate).toISOString().split("T")[0] : "";
      },
    },
  ];
}

export function getCsvSharedEuTaxonomyValuesMapping(
  isfs: number
): Array<DataPoint<FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>, string | number>> {
  return [
    {
      label: "IS/FS",
      value: (): string => isfs.toString(),
    },
    {
      label: "Fiscal Year",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        getFiscalYearDeviation(row.t.fiscalYearDeviation),
    },
    {
      label: "Fiscal Year End",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        row.t.fiscalYearEnd,
    },
    ...getCsvReportMapping("AnnualReport"),
    ...getCsvReportMapping("SustainabilityReport"),
    ...getCsvReportMapping("IntegratedReport"),
    ...getCsvReportMapping("ESEFReport"),
    {
      label: "Scope of Entities",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        humanizeOrUndefined(row.t.scopeOfEntities),
    },
    {
      label: "EU Taxonomy Activity Level Reporting",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        humanizeOrUndefined(row.t.activityLevelReporting),
    },
    {
      label: "NFRD mandatory",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        humanizeOrUndefined(row.t.reportingObligation),
    },
    {
      label: "Number Of Employees",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): number | undefined =>
        row.t.numberOfEmployees,
    },
    {
      label: "Assurance",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        getAssurance(row.t.assurance?.assurance),
    },
    {
      label: "Assurance Provider",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        row.t.assurance?.provider,
    },
    ...getCsvDataSourceMapping<FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>>(
      "Assurance",
      (row): CompanyReportReference | undefined => row.t.assurance?.dataSource
    ),
  ];
}
