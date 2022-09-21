import { faker } from "@faker-js/faker";
import { generateDataSource } from "./DataSourceFixtures";
import { AssuranceData, AssuranceDataAssuranceEnum } from "@clients/backend";
import { ReferencedReports } from "./Utils";
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
