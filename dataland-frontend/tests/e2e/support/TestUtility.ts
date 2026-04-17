import { type Suite } from 'mocha';

export interface ExecutionConfig {
  executionEnvironments: Array<ExecutionEnvironment>;
  onlyExecuteOnDatabaseReset?: boolean;
  onlyExecuteWhenEurodatIsLive?: boolean;
}
export type ExecutionEnvironment = 'developmentLocal' | 'ci' | 'developmentCd' | 'previewCd';

/**
 * This higher-level function can be used to control in which environments a given test suite is executed
 * @param name the name of the test suite
 * @param execConfig controls in which environments the test suite is executed
 * @param fn the test suite
 * @returns a new test suite that is only executed when the current environment matches one in the execution config
 */
export function describeIf(name: string, execConfig: ExecutionConfig, fn: (this: Suite) => void): Suite {
  const executionEnvironment = Cypress.expose('EXECUTION_ENVIRONMENT') as ExecutionEnvironment;
  const isDatabaseReset = Cypress.expose('RESET_DATABASE') as boolean;
  const ignoreExternalStorage = Cypress.expose('IGNORE_EXTERNAL_STORAGE') as boolean;

  if (!execConfig.executionEnvironments.includes(executionEnvironment)) {
    return describe(`${name} - Disabled`, () => {
      it(`Has been disabled because the execution environment ${executionEnvironment} has not been allowed`, () => {
        // Stub-Test just so its displayed why test suit wasn't executed
      });
    });
  }

  if (
    execConfig.onlyExecuteOnDatabaseReset &&
    !isDatabaseReset &&
    executionEnvironment != 'ci' &&
    executionEnvironment != 'developmentLocal'
  ) {
    return describe(`${name} - Disabled`, () => {
      it(`Has been disabled because the tests are only run when the databases are reset`, () => {
        // Stub-Test just so its displayed why test suit wasn't executed
      });
    });
  }
  if (execConfig.onlyExecuteWhenEurodatIsLive && ignoreExternalStorage) {
    return describe(`${name} - Disabled`, () => {
      it(`Has been disabled because the tests are only run when eurodat is live`, () => {
        // Stub-Test just so its displayed why test suit wasn't executed
      });
    });
  }

  return describe(name, fn);
}
