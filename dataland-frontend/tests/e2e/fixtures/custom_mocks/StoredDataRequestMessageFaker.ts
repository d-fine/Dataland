import { type StoredDataRequestMessageObject } from "@clients/communitymanager";
import { faker } from "@faker-js/faker";
import { generateInt } from "@e2e/fixtures/common/NumberFixtures";
import { generateArray } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a stored data request message object with contacts and a random message
 * @returns a fake stored data request message object
 */
export function generateStoredDataRequestMessage(): StoredDataRequestMessageObject {
  return {
    contacts: new Set(generateArray(() => faker.internet.email(), 1)),
    message: faker.git.commitMessage(),
    creationTimestamp: generateInt(),
  };
}
