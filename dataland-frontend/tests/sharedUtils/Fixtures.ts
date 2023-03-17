import { CompanyInformation } from "@clients/backend";

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
  reportingPeriod: string;
}


export function getPreparedFixture<T>(name: string, preparedFixtures: FixtureData<T>[]): FixtureData<T> {
  const preparedFixture = preparedFixtures.find((it): boolean => it.companyInformation.companyName == name)!;
  if (!preparedFixture) {
    throw new ReferenceError(
      "Variable preparedFixture is undefined because the provided company name could not be found in the prepared fixtures."
    );
  } else {
    return preparedFixture;
  }
}
