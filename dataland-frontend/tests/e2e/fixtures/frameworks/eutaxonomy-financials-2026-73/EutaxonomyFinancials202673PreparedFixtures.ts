import { type FixtureData } from '@sharedUtils/Fixtures';
import { type EutaxonomyFinancials202673Data } from '@clients/backend';
import {
  generateEutaxonomyFinancials202673Data,
  generateEutaxonomyFinancials202673Fixtures,
} from './EutaxonomyFinancials202673DataFixtures';

/**
 * Generates eutaxonomy-financials-2026-73 prepared fixtures by generating random eutaxonomy-financials-2026-73 datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateEutaxonomyFinancials202673PreparedFixtures(): Array<
  FixtureData<EutaxonomyFinancials202673Data>
> {
  const [preparedFixture] = generateEutaxonomyFinancials202673Fixtures(1);

  preparedFixture.companyInformation.companyName =
    'All-fields-defined-for-EU-Taxonomy-Financials-202673-Framework-Company';
  preparedFixture.reportingPeriod = '2026';
  preparedFixture.t = generateEutaxonomyFinancials202673Data(0);

  return [preparedFixture];
}
