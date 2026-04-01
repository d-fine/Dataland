import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyNonFinancials202673Data } from '@clients/backend';
import {
  generateEutaxonomyNonFinancials202673Data,
  generateEutaxonomyNonFinancials202673Fixtures,
} from './EutaxonomyNonFinancials202673DataFixtures';

/**
 * Generates eutaxonomy-non-financials-2026-73 prepared fixtures by generating random eutaxonomy-non-financials-2026-73 datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyNonFinancials202673PreparedFixtures(): Array<
  FixtureData<EutaxonomyNonFinancials202673Data>
> {
  const [preparedFixture] = generateEutaxonomyNonFinancials202673Fixtures(1);

  preparedFixture.companyInformation.companyName =
    'All-fields-defined-for-EU-Taxonomy-Non-Financials-202673-Framework-Company';
  preparedFixture.reportingPeriod = '2026';
  preparedFixture.t = generateEutaxonomyNonFinancials202673Data(0);

  return [preparedFixture];
}
