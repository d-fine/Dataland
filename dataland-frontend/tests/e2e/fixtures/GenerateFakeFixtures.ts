import { exportCustomMocks } from '@e2e/fixtures/custom_mocks';
import { exit } from 'process';
import { readdir } from 'fs/promises';
import { setupDeterministicFakerEnvironmentForFramework } from '@e2e/fixtures/ReproducibilityConfiguration';

export const FAKE_FIXTURES_PER_FRAMEWORK = 50;

interface FrameworkFixtureModule {
  default: () => void;
}

/**
 * The main entrypoint of the fake fixture generator
 */
async function main(): Promise<void> {
  const customMockSeed = setupDeterministicFakerEnvironmentForFramework('custom-mocks');
  console.log(`Hash seed for custom mocks is '${customMockSeed}'`);
  exportCustomMocks();

  const frameworkDirectoryContents = (await readdir(__dirname + '/frameworks', { withFileTypes: true })).filter(
    (entry) => entry.isDirectory()
  );

  const frameworkFakeFixturePromises = frameworkDirectoryContents.map(
    async (entry) => (await import('./frameworks/' + entry.name)) as FrameworkFixtureModule
  );

  const frameworkFixtureModules = await Promise.all(frameworkFakeFixturePromises);

  for (let i = 0; i < frameworkFixtureModules.length; i++) {
    const module = frameworkFixtureModules[i];
    const frameworkName = frameworkDirectoryContents[i].name;
    const seed = setupDeterministicFakerEnvironmentForFramework(frameworkName);
    console.log(`Hash seed for framework '${frameworkName}' is '${seed}'`);
    module.default();
  }
}

main().catch((ex) => {
  console.log('Unexpected error during fake-fixture generation');
  console.log(ex);
  exit(1);
});
