import { CompanyInformation } from "@clients/backend";

export interface FixtureData<T> {
  companyInformation: CompanyInformation;
  t: T;
  reportingPeriod: string;
}

/**
 * Generic function to retrieve the first prepared fixture whose company name equals the provided search string
 *
 * @param name Search string to look for in the prepared fixtures
 * @param preparedFixtures The parsed array of prepared fixtures
 * @returns the first prepared fixture whose name equals the provided search string
 */
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
