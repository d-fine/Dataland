import { FixtureData } from "./GenerateFakeFixtures";
import { EuTaxonomyDataForFinancials, EuTaxonomyDataForNonFinancials, YesNoNa } from "../../../build/clients/backend";
import { humanizeString } from "../../../src/utils/StringHumanizer";
import { getAssurance, getFiscalYearDeviation, humaniseOrUndefined } from "./CsvUtils";
import { getCsvDataSourceMapping } from "./DataSourceFixtures";
import { generateReferencedReports } from "./DataPointFixtures";
import { randomYesNoNaUndefined, randomYesNoUndefined } from "./YesNoFixtures";
import { faker } from "@faker-js/faker";
import { generateAssuranceData } from "./AssuranceDataFixture";

export function populateSharedValues(input: EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials) {
  input.referencedReports = generateReferencedReports();
  input.fiscalYearDeviation = randomYesNoUndefined();
  input.fiscalYearEnd = faker.date.past().toISOString().split("T")[0];
  input.assurance = generateAssuranceData(input.referencedReports);
  input.scopeOfEntities = randomYesNoNaUndefined();
  input.reportingObligation = randomYesNoUndefined();
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
          ? row.t.referencedReports[reportName].isGroupLevel
          : "",
    },
    {
      label: `Group Level ${humanizeString(reportName)}`,
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        row.t.referencedReports !== undefined &&
        row.t.referencedReports !== null &&
        row.t.referencedReports[reportName] !== undefined &&
        row.t.referencedReports[reportName] !== null
          ? row.t.referencedReports[reportName].reference
          : "",
    },
  ];
}

export function getCsvSharedEuTaxonomyValuesMapping<T>(isfs: number) {
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
    ...getCsvReportMapping("AnualReport"),
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
      label: "NFRD mandatoryg",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        humaniseOrUndefined(row.t.reportingObligation),
    },
    {
      label: "Assurance",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        getAssurance(row.t.assurance?.assurance),
    },
    {
      label: "Assurance provider",
      value: (row: FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>) =>
        humaniseOrUndefined(row.t.reportingObligation),
    },
    ...getCsvDataSourceMapping<FixtureData<EuTaxonomyDataForFinancials | EuTaxonomyDataForNonFinancials>>(
      "Assurance",
      (row) => row.t.assurance?.dataSource
    ),
  ];
}
