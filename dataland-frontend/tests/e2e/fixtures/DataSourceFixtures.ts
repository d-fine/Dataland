import { faker } from "@faker-js/faker";
import { CompanyReportReference } from "../../../build/clients/backend";

export function generateDataSource(): CompanyReportReference {
  return {
    page: faker.mersenne.rand(1200, 1),
    report: new URL(`${faker.internet.domainWord()}.pdf`, faker.internet.url()).href,
  };
}
