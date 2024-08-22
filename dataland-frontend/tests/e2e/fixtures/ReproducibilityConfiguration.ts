import { faker } from '@faker-js/faker';

export const FAKER_BASE_SEED = 0;
export const HASH_MULTIPLIER = 31;
/**
 * Deterministically converts a string to a number using a standard hashing function (same as java string hash)
 * @param str the string to hash
 * @returns the java hashCode of the string
 */
function stringHashCode(str: string): number {
  return str.split('').reduce((acc, toIntegrate) => ((acc << 5) - acc + toIntegrate.charCodeAt(0)) | 0, 0);
}

/**
 * Combines multiple hashcodes similar to how Objects.hash works in java
 * @param codes the hash codes to combine
 * @returns the combined hash code
 */
function combineHashCodes(...codes: number[]): number {
  let hash = 1;
  for (const code of codes) {
    hash = HASH_MULTIPLIER * hash + code;
  }
  return hash;
}

/**
 * Calculates the random number generator seed to use for a specific framework
 * @param folderName the name of the framework folder
 * @returns the generated hashCode
 */
function getRngSeedForFramework(folderName: string): number {
  const folderNameHash = stringHashCode(folderName);
  return combineHashCodes(FAKER_BASE_SEED, folderNameHash);
}

/**
 * Configures a deterministic fake-fixture generation environment using faker.
 * The seed is generated using the framework name
 * @param folderName the name of the framework folder
 * @returns the employed faker seed
 */
export function setupDeterministicFakerEnvironmentForFramework(folderName: string): number {
  const seed = getRngSeedForFramework(folderName);
  faker.seed(seed);
  faker.setDefaultRefDate(new Date('2024-01-24')); // Dataland launch date ;)
  return seed;
}
