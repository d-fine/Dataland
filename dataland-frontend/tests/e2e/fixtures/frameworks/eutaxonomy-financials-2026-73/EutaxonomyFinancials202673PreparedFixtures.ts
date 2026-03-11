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
  const preparedFixtures = [];
  // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

  const manipulatorFunctions: Array<
    (input: FixtureData<EutaxonomyFinancials202673Data>) => FixtureData<EutaxonomyFinancials202673Data>
  > = [createCompanyWithEutaxonomyFinancials202673DataAllFieldsDefined];
  const preparedFixturesBeforeManipulation = generateEutaxonomyFinancials202673Fixtures(manipulatorFunctions.length);

  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }

  return preparedFixtures;
}

/**
 * Creates a company with EU Taxonomy Financials (2026/73) data where all data fields are defined
 * @param input
 */
function createCompanyWithEutaxonomyFinancials202673DataAllFieldsDefined(
  input: FixtureData<EutaxonomyFinancials202673Data>
): FixtureData<EutaxonomyFinancials202673Data> {
  input.companyInformation.companyName = 'All-fields-defined-for-EU-Taxonomy-Financials-202673-Framework-Company';
  input.reportingPeriod = '2026';
  input.t = generateEutaxonomyFinancials202673Data(0);
  return input;
}
