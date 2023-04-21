import { DataPoint } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
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
import { randomYesNoNaUndefined, randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateAssuranceData } from "./AssuranceDataFixture";
import { randomPastDateOrUndefined } from "@e2e/fixtures/common/DateFixtures";
import { randomNumber } from "@e2e/fixtures/common/NumberFixtures";
import { randomFiscalYearDeviationOrUndefined } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Fills in random values for fields shared between the eutaxonomy frameworks
 *
 * @param input the framework object to fill in data for
 */
export function populateSharedValues(input: EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials): void {
  input.referencedReports = generateReferencedReports();
  input.fiscalYearDeviation = randomFiscalYearDeviationOrUndefined();
  input.fiscalYearEnd = randomPastDateOrUndefined();
  input.assurance = generateAssuranceData(input.referencedReports);
  input.scopeOfEntities = randomYesNoNaUndefined();
  input.reportingObligation = valueOrUndefined(randomYesNo());
  input.numberOfEmployees = valueOrUndefined(randomNumber(100000));
  input.activityLevelReporting = valueOrUndefined(randomYesNo());
}

/**
 * A helper function that extracts a report by name from an eutaxonomy dataset if it exists
 *
 * @param row the dataset to extract the report form
 * @param reportName the name of the report to look for
 * @returns the company report object if it exists, undefined otherwise.
 */
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

/**
 * Returns the CSV mapping for a type of company report
 *
 * @param reportName the name of the report to generate csv mappings for
 * @returns the generated CSV mapping
 */
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

/**
 * Returns the CSV mapping of fields that are shared between the eutaxonomy frameworks
 *
 * @param isfs the value of the IS/FS column
 * @returns the generated CSV mapping
 */
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
    {
      label: "Reporting Period",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>): string | undefined =>
        row.reportingPeriod,
    },
  ];
}
