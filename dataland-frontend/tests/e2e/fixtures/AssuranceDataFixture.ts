import { faker } from "@faker-js/faker";
import { generateDataSource } from "./DataSourceFixtures";
import { AssuranceData, AssuranceDataAssuranceEnum } from "../../../build/clients/backend";
export function generateAssuranceData(): AssuranceData | undefined {
  const assurance = faker.helpers.arrayElement(Object.values(AssuranceDataAssuranceEnum));
  const provider =
    assurance !== AssuranceDataAssuranceEnum.None && faker.datatype.boolean() ? faker.company.name() : undefined;

  const dataSource =
    assurance !== AssuranceDataAssuranceEnum.None && faker.datatype.boolean() ? generateDataSource() : undefined;

  return {
    assurance: assurance,
    provider: provider,
    dataSource: dataSource,
  };
}
