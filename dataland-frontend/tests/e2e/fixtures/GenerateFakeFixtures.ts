import { exportCustomMocks } from "@e2e/fixtures/custom_mocks";
import { exit } from "process";
import { readdir } from "fs/promises";
import { faker } from "@faker-js/faker";

export const FAKE_FIXTURES_PER_FRAMEWORK = 50;
export const FAKER_BASE_SEED = 0;

interface FrameworkFixtureModule {
  default: () => void;
}

/**
 * Deterministically converts a string to a number using a standard hashing function (same as java string hash)
 * @param str the string to hash
 * @returns the java hashCode of the string
 */
function stringHashCode(str: string): number {
  return str.split("").reduce((prevHash, currVal) => ((prevHash << 5) - prevHash + currVal.charCodeAt(0)) | 0, 0);
}

/**
 * The main entrypoint of the fake fixture generator
 */
async function main(): Promise<void> {
  faker.seed(FAKER_BASE_SEED);
  faker.setDefaultRefDate(new Date("2024-01-24")); // Dataland launch date ;)

  exportCustomMocks();

  const frameworkDirectoryContents = (await readdir(__dirname + "/frameworks", { withFileTypes: true })).filter(
    (entry) => entry.isDirectory(),
  );

  const frameworkFakeFixturePromises = frameworkDirectoryContents.map(
    async (entry) => (await import("./frameworks/" + entry.name)) as FrameworkFixtureModule,
  );

  const frameworkFixtureModules = await Promise.all(frameworkFakeFixturePromises);

  for (let i = 0; i < frameworkFixtureModules.length; i++) {
    const hashDelta = stringHashCode(frameworkDirectoryContents[i].name);
    const module = frameworkFixtureModules[i];
    faker.seed(FAKER_BASE_SEED + hashDelta);
    module.default();
  }
}

main().catch((ex) => {
  console.log("Unexpected error during fake-fixture generation");
  console.log(ex);
  exit(1);
});
