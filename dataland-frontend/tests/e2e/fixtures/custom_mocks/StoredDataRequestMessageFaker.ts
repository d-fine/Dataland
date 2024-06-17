import { type StoredDataRequestMessageObject } from '@clients/communitymanager';
import { faker } from '@faker-js/faker';
import { generateInt } from '@e2e/fixtures/common/NumberFixtures';
import { generateArray } from '@e2e/fixtures/FixtureUtils';

/**
 * Generates a stored data request message object with contacts and a random message
 * @returns a fake stored data request message object
 */
export function generateStoredDataRequestMessage(): StoredDataRequestMessageObject {
  const minimalNumberOfEmailAddressees = 1;
  return {
    contacts: new Set(
      generateArray(() => faker.internet.email({ provider: 'example.com' }), minimalNumberOfEmailAddressees)
    ),
    message: faker.git.commitMessage(),
    creationTimestamp: generateInt(),
  };
}
