import { faker } from '@faker-js/faker';
import { generateDataSource } from '@e2e/fixtures/common/DataSourceFixtures';
import { type AssuranceDataPoint, AssuranceDataPointValueEnum } from '@clients/backend';
import { pickOneElement, type ReferencedDocuments } from '@e2e/fixtures/FixtureUtils';
import { valueOrNull } from '@e2e/utils/FakeFixtureUtils';

/**
 * Generates random assurance data
 * @param reports the reports that can be referenced as data sources
 * @param nullProbability the probability (as number between 0 and 1) for a missing assurance provider
 * @returns random assurance data
 */
export function generateAssuranceDatapoint(reports: ReferencedDocuments, nullProbability: number): AssuranceDataPoint {
  const isAssuranceProviderExisting = faker.datatype.boolean(1 - nullProbability);

  const assuranceValuesForExistingProvider = Object.values(AssuranceDataPointValueEnum).filter(
    (value) => value !== AssuranceDataPointValueEnum.None
  );

  const assurance = isAssuranceProviderExisting
    ? pickOneElement(assuranceValuesForExistingProvider)
    : AssuranceDataPointValueEnum.None;

  const provider = isAssuranceProviderExisting ? valueOrNull(faker.company.name(), nullProbability) : null;

  const dataSource = isAssuranceProviderExisting ? valueOrNull(generateDataSource(reports), nullProbability) : null;

  return {
    value: assurance,
    provider: provider,
    dataSource: dataSource,
  };
}
