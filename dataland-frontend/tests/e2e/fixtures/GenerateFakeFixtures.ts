import { exportCustomMocks } from "@e2e/fixtures/custom_mocks";
import { exit } from "process";
import { readdir } from "fs/promises";
import { faker } from "@faker-js/faker";

export const FAKE_FIXTURES_PER_FRAMEWORK = 50;
export const FAKER_BASE_SEED = 1;

interface FrameworkFixtureModule {
  default: () => void;
}

/**
 * The main entrypoint of the fake fixture generator
 */
async function main(): Promise<void> {
  faker.seed(FAKER_BASE_SEED);
  faker.setDefaultRefDate(new Date("2024-01-24")); // Dataland launch date ;)

  exportCustomMocks();

  const frameworkDirectoryContents = await readdir(__dirname + "/frameworks", { withFileTypes: true });

  const frameworkFakeFixturePromises = frameworkDirectoryContents
    .filter((entry) => entry.isDirectory())
    .map(async (entry) => (await import("./frameworks/" + entry.name)) as FrameworkFixtureModule);

  const frameworkFixtureModules = await Promise.all(frameworkFakeFixturePromises);

  for (const module of frameworkFixtureModules) {
    faker.seed(FAKER_BASE_SEED);
    module.default();
  }
}

main().catch((ex) => {
  console.log("Unexpected error during fake-fixture generation");
  console.log(ex);
  exit(1);
});
