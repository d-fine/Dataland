import { exportCustomMocks } from "@e2e/fixtures/custom_mocks";
import { exit } from "process";
import { readdir } from "fs/promises";

export const FAKE_FIXTURES_PER_FRAMEWORK = 50;

interface FrameworkFixtureModule {
  default: () => void;
}

/**
 * The main entrypoint of the fake fixture generator
 */
async function main(): Promise<void> {
  exportCustomMocks();

  const frameworkDirectoryContents = await readdir(__dirname + "/frameworks", { withFileTypes: true });

  const frameworkFakeFixturePromises = frameworkDirectoryContents
    .filter((entry) => entry.isDirectory())
    .map(async (entry) => {
      const module = (await import("./frameworks/" + entry.name)) as FrameworkFixtureModule;
      module.default();
    });

  await Promise.all(frameworkFakeFixturePromises);
}

main().catch((ex) => {
  console.log("Unexpected error during fake-fixture generation");
  console.log(ex);
  exit(1);
});
