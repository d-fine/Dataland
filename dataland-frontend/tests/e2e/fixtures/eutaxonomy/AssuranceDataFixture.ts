import { faker } from "@faker-js/faker";
import { generateDataSource } from "@e2e/fixtures/common/DataSourceFixtures";
import { AssuranceData, AssuranceDataAssuranceEnum } from "@clients/backend";
import { ReferencedReports } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates random assurance data
 * @param reports the reports that can be referenced as data sources
 * @returns random assurance data
 */
export function generateAssuranceData(reports: ReferencedReports): AssuranceData | undefined {
  const assurance = faker.helpers.arrayElement(Object.values(AssuranceDataAssuranceEnum));
  const provider =
    assurance !== AssuranceDataAssuranceEnum.None && faker.datatype.boolean() ? faker.company.name() : undefined;

  const dataSource =
    assurance !== AssuranceDataAssuranceEnum.None && faker.datatype.boolean() ? generateDataSource(reports) : undefined;

  return {
    assurance: assurance,
    provider: provider,
    dataSource: dataSource,
  };
}
