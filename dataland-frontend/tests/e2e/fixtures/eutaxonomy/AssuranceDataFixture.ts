import { faker } from "@faker-js/faker";
import { generateDataSource } from "@e2e/fixtures/common/DataSourceFixtures";
import { type AssuranceDataPoint, AssuranceDataPointValueEnum } from "@clients/backend";
import { pickOneElement, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates random assurance data
 * @param reports the reports that can be referenced as data sources
 * @returns random assurance data
 */
export function generateAssuranceData(reports: ReferencedDocuments): AssuranceDataPoint {
  const assurance = pickOneElement(Object.values(AssuranceDataPointValueEnum));
  const provider =
    assurance !== AssuranceDataPointValueEnum.None && faker.datatype.boolean() ? faker.company.name() : undefined;

  const dataSource =
    assurance !== AssuranceDataPointValueEnum.None && faker.datatype.boolean()
      ? generateDataSource(reports)
      : { fileReference: "", page: undefined, tagName: undefined };

  return {
    value: assurance,
    provider: provider,
    dataSource: dataSource,
  };
}
