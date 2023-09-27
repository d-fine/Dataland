import { faker } from "@faker-js/faker";
import { generateDataSource } from "@e2e/fixtures/common/DataSourceFixtures";
import { type AssuranceData, AssuranceDataAssuranceEnum } from "@clients/backend";
import { pickOneElement, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates random assurance data
 * @param reports the reports that can be referenced as data sources
 * @param setMissingValuesToNull controls if missing values should be undefined or null
 * @returns random assurance data
 */
export function generateAssuranceData(reports: ReferencedDocuments, setMissingValuesToNull: boolean): AssuranceData {
  const missingValue = setMissingValuesToNull ? null : undefined;
  const assurance = pickOneElement(Object.values(AssuranceDataAssuranceEnum));
  const provider =
    assurance !== AssuranceDataAssuranceEnum.None && faker.datatype.boolean() ? faker.company.name() : missingValue;

  const dataSource =
    assurance !== AssuranceDataAssuranceEnum.None && faker.datatype.boolean()
      ? generateDataSource(reports)
      : { report: "", page: missingValue, tagName: missingValue };

  return {
    assurance: assurance,
    provider: provider,
    dataSource: dataSource,
  };
}
